package school.hei.haapi.service;

import static java.time.Instant.now;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CREDIT;
import static school.hei.haapi.model.CreditMovement.DEPOSIT;
import static school.hei.haapi.model.CreditMovement.WITHDRAWAL;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Credit;
import school.hei.haapi.model.CreditMovement;
import school.hei.haapi.model.CreditTransaction;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CreditRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.TransactionRepository;

@Service
@AllArgsConstructor
public class CreditService {
  private final CreditRepository creditRepository;
  private final TransactionRepository transactionRepository;
  private final PaymentRepository paymentRepository;

  public Optional<Credit> getCreditByStudentId(String studentId) {
    var credit = creditRepository.findCreditByStudent_Id(studentId);
    credit.ifPresent(
        value ->
            value.setAmount(
                value.getAmount() - getPendingCreditPaymentsAmountByStudentId(studentId)));
    return credit;
  }

  private int getPendingCreditPaymentsAmountByStudentId(String studentId) {
    return paymentRepository.sumPendingCreditPaymentsAmountByStudentId(studentId);
  }

  public List<CreditTransaction> getCreditTransactionsByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize) {
    var pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    var credit = getCreditByStudentId(studentId);
    if (credit.isEmpty()) {
      throw new BadRequestException("The student doesn't have a credit");
    }
    return transactionRepository.findTransactionsByCredit_Id(credit.get().getId(), pageable);
  }

  public List<Credit> saveAll(List<Credit> credits) {
    return creditRepository.saveAll(credits);
  }

  public List<CreditTransaction> saveCreditTransactions(List<CreditTransaction> transactions) {
    return transactionRepository.saveAll(transactions);
  }

  public void depositArchivedFee(Fee fee) {
    if (fee.isArchived()) {
      throw new BadRequestException("Fee can't archived two times");
    }

    applyTransaction(getOrCreateCredit(fee.getStudent()), fee, fee.getTotalAmount(), DEPOSIT);
  }

  public void subtractStudentCreditByPayment(Payment payment) {
    if (!isPaidByCredit(payment)) {
      return;
    }
    applyTransaction(
        getCreditByStudentId(payment.getFee().getStudent().getId()).orElseThrow(),
        payment.getFee(),
        payment.getAmount(),
        WITHDRAWAL);
  }

  public void transferFeeOverpaymentToCredit(Fee fee, User student) {
    var overpayment = -fee.getRemainingAmount();
    if (overpayment <= 0) {
      return;
    }
    applyTransaction(getOrCreateCredit(student), fee, overpayment, DEPOSIT);
    fee.setRemainingAmount(0);
  }

  private boolean isPaidByCredit(Payment payment) {
    return CREDIT.equals(payment.getType());
  }

  private Credit getOrCreateCredit(User student) {
    return getCreditByStudentId(student.getId())
        .orElseGet(
            () -> Credit.builder().student(student).amount(0).creationDatetime(now()).build());
  }

  private void applyTransaction(Credit credit, Fee fee, int amount, CreditMovement movement) {
    if (DEPOSIT.equals(movement)) {
      credit.setAmount(credit.getAmount() + amount);
    } else {
      credit.setAmount(credit.getAmount() - amount);
    }
    var savedCredit = saveAll(List.of(credit)).getFirst();
    var transaction =
        CreditTransaction.builder()
            .credit(savedCredit)
            .fee(fee)
            .amount(amount)
            .creditMovement(movement)
            .creationDatetime(now())
            .build();

    saveCreditTransactions(List.of(transaction));
  }
}
