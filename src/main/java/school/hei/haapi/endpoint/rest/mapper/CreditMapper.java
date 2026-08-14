package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Credit;
import school.hei.haapi.endpoint.rest.model.CreditMovement;
import school.hei.haapi.endpoint.rest.model.CreditTransaction;

@Component
@AllArgsConstructor
public class CreditMapper {
  private final UserMapper userMapper;
  private final FeeMapper feeMapper;

  public Credit toRest(school.hei.haapi.model.Credit credit) {
    return new Credit()
        .id(credit.getId())
        .student(userMapper.toIdentifier(credit.getStudent()))
        .amount(credit.getAmount());
  }

  public CreditTransaction toRest(school.hei.haapi.model.CreditTransaction creditTransaction) {
    return new CreditTransaction()
        .transactionId(creditTransaction.getId())
        .amount(creditTransaction.getAmount())
        .fee(feeMapper.toRestFee(creditTransaction.getFee()))
        .credit(toRest(creditTransaction.getCredit()))
        .movement(CreditMovement.valueOf(creditTransaction.getCreditMovement().toString()));
  }

  public List<CreditTransaction> toCreditTransactionRest(
      List<school.hei.haapi.model.CreditTransaction> creditTransactions) {
    return creditTransactions.stream().map(this::toRest).toList();
  }
}
