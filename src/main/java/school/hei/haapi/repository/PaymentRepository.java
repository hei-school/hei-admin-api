package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
  @Query(
      value =
          "select p from Payment p join Fee f on f.id = p.fee.id where f.student.id = :student_id"
              + " and f.id = :fee_id order by p.creationDatetime desc")
  List<Payment> getByStudentIdAndFeeIdWithPagination(
      @Param("student_id") String studentId, @Param("fee_id") String feeId, Pageable pageable);

  List<Payment> findAllByFee_IdOrderByCreationDatetimeAsc(String feeId);

  @Query(
      value =
          "select p from Payment p join Fee f on f.id = p.fee.id"
              + " where f.student.id = :student_id and f.id = :fee_id")
  List<Payment> getByStudentIdAndFeeId(
      @Param("student_id") String studentId, @Param("fee_id") String feeId);

  List<Payment> getAllByCreationDatetimeBetweenOrderByCreationDatetimeAsc(Instant from, Instant to);

  @Query(
      """
      select p
      from Payment p
      where (:status is null or p.status = :status)
          and p.type = CREDIT
      """)
  List<Payment> findPaymentsByStatus(@Param("status") PaymentStatus status, Pageable pageable);
}
