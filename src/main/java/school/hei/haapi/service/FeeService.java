package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.*;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

    private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
    private final FeeRepository feeRepository;
    private final FeeValidator feeValidator;

    private final EventProducer eventProducer;
    @Autowired
    private final DelayPenaltyService delayPenaltyService;

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

    private Fee updateFeeStatus(Fee initialFee) {
        if (initialFee.getRemainingAmount() == 0) {
            initialFee.setStatus(PAID);
        } else if (Instant.now().isAfter(initialFee.getDueDatetime())) {
            initialFee.setStatus(LATE);
        }
        return initialFee;
    }

    public void applyDelayPenaltyToFees(DelayPenalty delayPenalty) {
        List<Fee> feeList = feeRepository.getFeesByStatus(DEFAULT_STATUS);
        feeList.addAll(feeRepository.getFeesByStatus(UNPAID));
        feeList.forEach((Fee fee) -> {
            if (fee.getRemainingAmount() > 0
                    && fee.getDueDatetime().isAfter(Instant.now())) {
                fee.setStatus(school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE);
                if (Instant.now().isAfter(fee.getDueDatetime()
                        .plus(Duration.of(delayPenalty.getGrace_delay(), ChronoUnit.DAYS)))) {
                    if (Instant.now()
                            .isBefore(fee.getDueDatetime()
                                    .plus(Duration.of(delayPenalty.getGrace_delay(), ChronoUnit.DAYS))
                                    .plus(Duration.of(delayPenalty.getApplicability_delay_after_grace(), ChronoUnit.DAYS)))) {
                        int newRemainingAmount = fee.getRemainingAmount()
                                + (delayPenalty.getInterest_percent() / 100) * fee.getRemainingAmount();
                        fee.setRemainingAmount(newRemainingAmount);
                    }
                }
            }
        });
    }

    @Scheduled(cron = "0 0 8 1/1 * ?")
    public void sheduledApplicationForFee() {
        applyDelayPenaltyToFees(delayPenaltyService.getCurrentDelay());
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
