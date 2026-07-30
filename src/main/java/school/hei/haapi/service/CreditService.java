package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Credit;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Transaction;
import school.hei.haapi.repository.CreditRepository;
import school.hei.haapi.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class CreditService {
  private final CreditRepository creditRepository;
  private final TransactionRepository transactionRepository;

  public Optional<Credit> getCreditByStudentId(String studentId) {
    return Optional.of(creditRepository.findCreditByStudent_Id(studentId));
  }

  public List<Transaction> getCreditTransactionsByStudentId(String studentId, PageFromOne page, BoundedPageSize pageSize) {
      var pageable =
              PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
      var credit = getCreditByStudentId(studentId).get();
    return transactionRepository.findTransactionsByCredit_Id(credit.getId(), pageable);
  }

  public List<Credit> saveAll(List<Credit> credits){
      return creditRepository.saveAll(credits);
  }

  public List<Transaction> saveTransactions(List<Transaction> transactions){
      return transactionRepository.saveAll(transactions);
  }
}
