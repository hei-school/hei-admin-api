package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Credit;
import school.hei.haapi.endpoint.rest.model.CreditMovement;
import school.hei.haapi.endpoint.rest.model.CreditTransaction;
import school.hei.haapi.model.exception.NotFoundException;

@Component
@AllArgsConstructor
public class CreditMapper {
  private final UserMapper userMapper;
  private final FeeMapper feeMapper;
  private final PaymentMapper paymentMapper;

  public Credit toRest(school.hei.haapi.model.Credit credit) {
    if (credit == null) {
      throw new NotFoundException("Student doesn't have credit yet.");
    }
    return new Credit()
        .id(credit.getId())
        .student(userMapper.toIdentifier(credit.getStudent()))
        .amount(credit.getAmount());
  }

  public CreditTransaction toRest(school.hei.haapi.model.CreditTransaction creditTransaction) {
    var payment = creditTransaction.getPayment();
    return new CreditTransaction()
        .transactionId(creditTransaction.getId())
        .amount(creditTransaction.getAmount())
        .fee(feeMapper.toRestFee(creditTransaction.getFee()))
        .payment(payment == null ? null : paymentMapper.toRestPayment(payment))
        .credit(toRest(creditTransaction.getCredit()))
        .movement(CreditMovement.valueOf(creditTransaction.getCreditMovement().toString()));
  }

  public List<CreditTransaction> toCreditTransactionRest(
      List<school.hei.haapi.model.CreditTransaction> creditTransactions) {
    return creditTransactions.stream().map(this::toRest).toList();
  }
}
