package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Credit;
import school.hei.haapi.model.Transaction;
import school.hei.haapi.repository.CreditRepository;
import school.hei.haapi.repository.TransactionRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CreditService {
  private final CreditRepository creditRepository;
  private final TransactionRepository transactionRepository;

  public Credit getCreditByStudentId(String studentId) {
    return creditRepository.findCreditByStudent_Id(studentId);
  }

  public List<Transaction> getCreditTransactionsByStudentId(String studentId) {
      var credit = getCreditByStudentId(studentId);
    return transactionRepository.findTransactionsByCredit_Id(credit.getId());
  }
}
