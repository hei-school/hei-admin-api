package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TypedLateFeeVerified;
import school.hei.haapi.endpoint.event.model.gen.LateFeeVerified;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.DelayPenalty;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.FeeValidator;
import school.hei.haapi.repository.FeeRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.service.utils.DateTimeUtils.instantToGregorianCalendar;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

    private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
    public static final double ONE_HUNDRED_PERCENT = 1 / 100d;
    private final FeeRepository feeRepository;
    private final FeeValidator feeValidator;
    private final DelayPenaltyService delayPenaltyService;

    private final EventProducer eventProducer;

    public Fee getById(String id) {
        return updateFeeStatus(feeRepository.getById(id));
    }

    public Fee getByStudentIdAndFeeId(String studentId, String feeId) {
        return updateFeeStatus(feeRepository.getByStudentIdAndId(studentId, feeId));
    }

    @Transactional
    public List<Fee> saveAll(List<Fee> fees) {
        feeValidator.accept(fees);
        return feeRepository.saveAll(fees);
    }

    public List<Fee> getFees(
            PageFromOne page, BoundedPageSize pageSize,
            school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
        Pageable pageable =
                PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "dueDatetime"));
        if (status != null) {
            return feeRepository.getFeesByStatus(status, pageable);
        }
        return feeRepository.getFeesByStatus(DEFAULT_STATUS, pageable);
    }

    public List<Fee> getFeesByStudentId(
            String studentId, PageFromOne page, BoundedPageSize pageSize,
            school.hei.haapi.endpoint.rest.model.Fee.StatusEnum status) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "dueDatetime"));
        if (status != null) {
            return feeRepository.getFeesByStudentIdAndStatus(studentId, status, pageable);
        }
        return feeRepository.getByStudentId(studentId, pageable);
    }

    /**
     * Apply delay penalty each day at 8AM for each late fees
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void scheduleApplyDelayPenalty() {
        Instant now = Instant.now();
        GregorianCalendar today = instantToGregorianCalendar(now);

        DelayPenalty delayPenalty = delayPenaltyService.getDelayPenalty();
        List<Fee> feesByStatus = feeRepository.getFeesByStatus(LATE);
        List<Fee> feesWithDelayPenaltyApplied = new ArrayList<>();
        Instant tomorrowNextScheduling = now.plus(1, ChronoUnit.DAYS);

        for (var fee : feesByStatus) {
            Instant lastDelayPenaltySchedulingApplied = fee.getLastDelayPenaltySchedulingApplied();
            GregorianCalendar currentFeeDelayPenaltyLastAppliedDate = instantToGregorianCalendar(lastDelayPenaltySchedulingApplied);
            boolean delayPenaltyWasAlreadyAppliedToday = currentFeeDelayPenaltyLastAppliedDate.equals(today);

            if (!delayPenaltyWasAlreadyAppliedToday) {
                fee.setNextDelayPenaltyScheduling(tomorrowNextScheduling);
                Fee feeWithDelayPenaltyApplied = applyOneDayDelayPenalty(fee, delayPenalty);
                feesWithDelayPenaltyApplied.add(feeWithDelayPenaltyApplied);
            }
        }

        feeRepository.saveAll(feesWithDelayPenaltyApplied);
    }

    /**
     * Apply daily interest rate on a fee and return it
     */
    private Fee applyOneDayDelayPenalty(Fee fee, DelayPenalty delayPenalty) {
        Instant currentInstant = Instant.now();
        boolean paymentLimitDateExceed = currentInstant.isAfter(fee.getDueDatetime());
        DelayPenalty delayPenaltyGlobalConf = delayPenaltyService.getDelayPenalty();

        // convert the graceDelay into Instant starting from the due datetime of the payment of the fee.
        int graceDelay = delayPenaltyGlobalConf.getGraceDelay();
        Instant graceDelayDueDate = fee.getDueDatetime().plus(graceDelay, ChronoUnit.DAYS);

        int applicabilityDelayAfterGrace = delayPenaltyGlobalConf.getApplicabilityDelayAfterGrace();
        Instant applicabilityDelayAfterGraceDate = fee.getDueDatetime().plus(applicabilityDelayAfterGrace, ChronoUnit.DAYS);

        boolean didNotExceedApplicabilityDelay = currentInstant.isAfter(graceDelayDueDate.plus(1, ChronoUnit.DAYS)) &&
                currentInstant.isBefore(applicabilityDelayAfterGraceDate);


        if (paymentLimitDateExceed &&
                currentInstant.isAfter(graceDelayDueDate) && didNotExceedApplicabilityDelay) {
            int toPay = (int) (fee.getTotalAmount() + fee.getTotalAmount() * delayPenalty.getInterestPercent() * ONE_HUNDRED_PERCENT);
            fee.setRemainingAmount(toPay);
            fee.setTotalAmount(toPay);
        }

        return fee;
    }

    private Fee updateFeeStatus(Fee initialFee) {
        if (initialFee.getRemainingAmount() == 0) {
            initialFee.setStatus(PAID);
        } else if (Instant.now().isAfter(initialFee.getDueDatetime())) {
            initialFee.setStatus(LATE);
        }
        return initialFee;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void updateFeesStatusToLate() {
        List<Fee> unpaidFees = feeRepository.getUnpaidFees();
        unpaidFees.forEach(fee -> {
            updateFeeStatus(fee);
            log.info("Fee with id." + fee.getId() + " is going to be updated from UNPAID to LATE");
        });
        feeRepository.saveAll(unpaidFees);
    }

    private TypedLateFeeVerified toTypedEvent(Fee fee) {
        return new TypedLateFeeVerified(
                LateFeeVerified.builder()
                        .type(fee.getType())
                        .student(fee.getStudent())
                        .comment(fee.getComment())
                        .remainingAmount(fee.getRemainingAmount())
                        .dueDatetime(fee.getDueDatetime())
                        .build()
        );
    }

    /*
     * An email will be sent to user with late fees
     * every morning at 8am (UTC+3)
     * */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendLateFeesEmail() {
        List<Fee> lateFees = feeRepository.getFeesByStatus(LATE);
        lateFees.forEach(
                fee -> {
                    eventProducer.accept(List.of(toTypedEvent(fee)));
                    log.info("Late Fee with id." + fee.getId() + " is sent to Queue");
                }
        );
    }

}
