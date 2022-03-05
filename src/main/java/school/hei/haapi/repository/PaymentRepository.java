package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
  List<Payment> getByFeeId(String feeId);

  @Query(value = "select p.* from payment p join fee f on f.id = p.fee_id"
      + " where f.user_id = :student_id",
      nativeQuery = true)
  List<Payment> getByStudentId(@Param("student_id") String studentId);
}
