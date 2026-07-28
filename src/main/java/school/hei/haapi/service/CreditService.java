package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Credit;
import school.hei.haapi.model.Transaction;
import school.hei.haapi.repository.CreditRepository;

@Service
@AllArgsConstructor
public class CreditService {
  private final CreditRepository creditRepository;

  public Credit getCreditByStudentId(String studentId) {
    return creditRepository.findCreditByStudent_Id(studentId);
  }

  public List<Transaction> getCreditTransactionsByStudentId(String studentId) {
    return null;
  }
}
