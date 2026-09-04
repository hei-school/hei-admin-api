package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Credit;
import school.hei.haapi.model.CreditMovement;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CreditRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.TransactionRepository;
import school.hei.haapi.service.CreditService;

class CreditServiceTest {
  CreditRepository creditRepositoryMock = mock();
  TransactionRepository transactionRepositoryMock = mock();
  PaymentRepository paymentRepositoryMock = mock();
  CreditService subject =
      new CreditService(creditRepositoryMock, transactionRepositoryMock, paymentRepositoryMock);

  @Test
  void transfer_overpayment_creates_transaction_when_none_exists() {
    var student = User.builder().id("studentId").build();
    var fee = Fee.builder().id("feeId").student(student).remainingAmount(-2000).build();
    var credit = Credit.builder().id("creditId").student(student).amount(0).build();
    when(creditRepositoryMock.findCreditByStudent_Id("studentId")).thenReturn(Optional.of(credit));
    when(transactionRepositoryMock.existsByFee_IdAndCreditMovement("feeId", CreditMovement.CREDIT))
        .thenReturn(false);
    when(creditRepositoryMock.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    subject.transferFeeOverpaymentToCredit(fee, student);

    verify(transactionRepositoryMock, times(1))
        .existsByFee_IdAndCreditMovement("feeId", CreditMovement.CREDIT);
    verify(creditRepositoryMock, times(1)).saveAll(anyList());
    // Overpayment is 2000: credit must be increased and a transaction persisted.
    assertEquals(0, fee.getRemainingAmount());
  }

  @Test
  void transfer_overpayment_is_skipped_when_a_transaction_already_exists_for_the_fee() {
    var student = User.builder().id("studentId").build();
    var fee = Fee.builder().id("feeId").student(student).remainingAmount(-2000).build();
    when(transactionRepositoryMock.existsByFee_IdAndCreditMovement("feeId", CreditMovement.CREDIT))
        .thenReturn(true);

    subject.transferFeeOverpaymentToCredit(fee, student);

    verify(creditRepositoryMock, never()).findCreditByStudent_Id(any());
    verify(creditRepositoryMock, never()).saveAll(anyList());
    assertEquals(0, fee.getRemainingAmount());
  }

  @Test
  void transfer_overpayment_does_nothing_when_there_is_no_surplus() {
    var student = User.builder().id("studentId").build();
    var fee = Fee.builder().id("feeId").student(student).remainingAmount(0).build();

    subject.transferFeeOverpaymentToCredit(fee, student);

    verify(transactionRepositoryMock, never()).existsByFee_IdAndCreditMovement(any(), any());
    verify(creditRepositoryMock, never()).saveAll(anyList());
  }
}
