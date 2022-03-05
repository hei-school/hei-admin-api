package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Payment;
import school.hei.haapi.repository.PaymentRepository;

@Service
@AllArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;

  public List<Payment> getByStudentIdAndFeeId(String studentId, String feeId) {
    return paymentRepository.getByFeeId(feeId);
  }

  public List<Payment> getByStudentId(String studentId) {
    return paymentRepository.getByStudentId(studentId);
  }

  public List<Payment> saveAll(String studentId, String feeId, List<Payment> toCreate) {
    return paymentRepository.saveAll(toCreate);
  }
}
