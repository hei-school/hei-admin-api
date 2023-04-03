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
import school.hei.haapi.service.utils.FeePenaltyUtils;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.UNPAID;

@Service
@AllArgsConstructor
@Slf4j
public class FeeService {

    private static final school.hei.haapi.endpoint.rest.model.Fee.StatusEnum DEFAULT_STATUS = LATE;
    private final FeeRepository feeRepository;
    private final FeeValidator feeValidator;
    private final EventProducer eventProducer;
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
        if (initialFee.getStatus() == UNPAID && initialFee.getRemainingAmount() == 0 && initialFee.getInterest() == 0) {
            initialFee.setStatus(PAID);
        } else if (initialFee.getStatus() == UNPAID && Instant.now().isAfter(initialFee.getDueDatetime())) {
            initialFee.setStatus(LATE);
        }
        return initialFee;
    }


    @Scheduled(cron = "0 0 * * * *")
    public List<Fee> updateFeesStatusToLate() {
        List<Fee> unpaidFees = feeRepository.getUnpaidFees();
        return unpaidFees.stream().map(fee -> {
            log.info("Fee with id." + fee.getId() + " is going to be updated from UNPAID to LATE");
            return updateFeeStatus(fee);
        }).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateFees() {
        DelayPenalty delayPenalty = delayPenaltyService.getCurrentDelayPenalty();
        List<Fee> fees = feeRepository.getUnpaidFees();
        feeRepository.saveAll(FeePenaltyUtils.penalizeFees(fees, delayPenalty));
    }

    private TypedLateFeeVerified toTypedEvent(Fee fee) {
        return new TypedLateFeeVerified(
                LateFeeVerified.builder()
                        .type(fee.getType())
                        .student(fee.getStudent())
                        .comment(fee.getComment())
                        .remainingAmount((int) fee.getRemainingAmount())
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
