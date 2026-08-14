package school.hei.haapi.service;

import static java.time.Instant.now;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CREDIT;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.MOBILE_MONEY;
import static school.hei.haapi.model.PaymentStatus.VALIDATE;
import static school.hei.haapi.service.utils.InstantUtils.UTC3;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.PaidFeeByMpbsNotificationBody;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.PaymentNumberSequence;
import school.hei.haapi.model.PaymentStatus;
import school.hei.haapi.model.dto.PaymentDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.mpbs.Mpbs;
import school.hei.haapi.model.validator.PaymentValidator;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {
  private final PaymentNumberSequenceService sequenceService;
  private final FeeRepository feeRepository;
  private final PaymentRepository paymentRepository;
  private final FeeService feeService;
  private final PaymentValidator paymentValidator;
  private final EventProducer eventProducer;
  private final FeeStatusHistoryService feeStatusHistoryService;
  private final CreditService creditService;

  public Payment deleteFeePaymentById(String paymentId) {
    Payment deletedPayment = getById(paymentId);
    Fee associatedFee = deletedPayment.getFee();

    if (deletedPayment.isDeleted()) {
      throw new BadRequestException("Payment with id #" + paymentId + " already deleted");
    }

    resetRemainingAmountBetweenDelete(associatedFee, deletedPayment.getAmount());
    paymentRepository.deleteById(paymentId);
    return deletedPayment;
  }

  private void resetRemainingAmountBetweenDelete(Fee associatedFee, int amount) {
    Instant now = now();
    associatedFee.setRemainingAmount(associatedFee.getRemainingAmount() + amount);

    if (associatedFee.getDueDatetime().isBefore(now) && associatedFee.getRemainingAmount() != 0) {
      associatedFee.updateStatus(LATE);
    }
    if (associatedFee.getDueDatetime().isAfter(now) && associatedFee.getRemainingAmount() != 0) {
      associatedFee.updateStatus(UNPAID);
    }
    feeStatusHistoryService.saveFeeStatus(associatedFee.getStatus(), associatedFee);
    feeRepository.save(associatedFee);
  }

  public Payment getById(String paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new NotFoundException("Payment with id: " + paymentId + " not found"));
  }

  public List<Payment> getByIds(List<String> paymentIds) {
    return paymentRepository.findByIds(paymentIds);
  }

  public List<Payment> getByStudentIdAndFeeId(
      String studentId, String feeId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return paymentRepository.getByStudentIdAndFeeIdWithPagination(studentId, feeId, pageable);
  }

  public List<Payment> getByFeeIdOrderByCreationDatetimeAsc(String feeId) {
    return paymentRepository.findAllByFee_IdOrderByCreationDatetimeAsc(feeId);
  }

  @Transactional
  public Payment savePaymentFromMpbs(Mpbs verifiedMpbs, int amount) {
    Fee correspondingFee = verifiedMpbs.getFee();
    Payment paymentFromMpbs =
        Payment.builder()
            .type(MOBILE_MONEY)
            .fee(correspondingFee)
            .amount(amount)
            .creationDatetime(now())
            .comment(correspondingFee.getComment())
            .build();
    eventProducer.accept(List.of(PaidFeeByMpbsNotificationBody.from(paymentFromMpbs)));
    return paymentRepository.save(paymentFromMpbs);
  }

  @Transactional
  public List<Payment> saveAll(List<Payment> toCreate) {
    paymentValidator.accept(toCreate);
    var creditsPayments =
        toCreate.stream()
            .filter(
                payment ->
                    !CREDIT.equals(payment.getType()) || VALIDATE.equals(payment.getStatus()))
            .toList();
    creditsPayments.forEach(
        payment -> {
          feeService.computeRemainingAmount(payment.getFee().getId(), payment.getAmount());
          creditService.subtractStudentCreditByPayment(payment);
        });
    return paymentRepository.saveAll(toCreate);
  }

  @Transactional
  public Payment updateSequence(PaymentDto paymentDto) {
    Payment payment = getById(paymentDto.getId());
    if (payment.getSequence() == null) {
      LocalDate localPaymentDate = payment.getCreationDatetime().atZone(UTC3).toLocalDate();
      PaymentNumberSequence localPaymentSequence =
          sequenceService.getNextSequence(localPaymentDate);
      payment.setSequence(localPaymentSequence);
      return paymentRepository.save(payment);
    } else return payment;
  }

  public List<Payment> getAllPaymentBetween(Instant from, Instant to) {
    return paymentRepository.getAllByCreationDatetimeBetweenOrderByCreationDatetimeAsc(from, to);
  }

  public List<Payment> getCreditPaymentsByStatus(
      PaymentStatus status, PageFromOne page, BoundedPageSize pageSize) {
    var pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return paymentRepository.findPaymentsByStatusAndType(status, CREDIT, pageable);
  }
}
