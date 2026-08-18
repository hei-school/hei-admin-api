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
    var validatedBy = payment == null ? null : payment.getValidatedBy();
    return new CreditTransaction()
        .transactionId(creditTransaction.getId())
        .amount(creditTransaction.getAmount())
        .fee(feeMapper.toRestFee(creditTransaction.getFee()))
        .credit(toRest(creditTransaction.getCredit()))
        .movement(CreditMovement.valueOf(creditTransaction.getCreditMovement().toString()))
        .validatedByRef(validatedBy == null ? null : validatedBy.getRef())
        .validatedByFirstName(validatedBy == null ? null : validatedBy.getFirstName())
        .validatedByLastName(validatedBy == null ? null : validatedBy.getLastName());
  }

  public List<CreditTransaction> toCreditTransactionRest(
      List<school.hei.haapi.model.CreditTransaction> creditTransactions) {
    return creditTransactions.stream().map(this::toRest).toList();
  }
}
