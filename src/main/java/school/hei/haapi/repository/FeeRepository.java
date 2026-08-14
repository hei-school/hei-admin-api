package school.hei.haapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.model.Fee;

@Repository
public interface FeeRepository extends JpaRepository<Fee, String> {
  Fee getByStudentIdAndId(String studentId, String feeId);

  boolean existsByStudentIdAndFeeTemplateId(String studentId, String feeTemplateId);

  List<Fee> findAllByStatus(FeeStatusEnum status);

  List<Fee> getFeesByStudentIdAndStatusOrderByDueDatetimeDesc(
      String studentId, FeeStatusEnum status, Pageable pageable);

  @Query(
      "select f from Fee f where f.status = 'UNPAID' "
          + "and f.student.status <> 'DISABLED'"
          + "and f.remainingAmount > 0 "
          + "and f.dueDatetime < :now")
  List<Fee> getUnpaidFees(@Param(value = "now") Instant now);

  @Query(
      "select f from Fee f where f.status = 'UNPAID' AND EXTRACT(month from f.dueDatetime) ="
          + " :month")
  List<Fee> getUnpaidFeesForTheMonthSpecified(Integer month);

  @Query(
      value =
          """
            SELECT
                f
            FROM
                Fee f
            JOIN
                User u ON f.student.id = u.id
            WHERE
                f.student.id = :studentId
            ORDER BY
              CASE
                WHEN f.status = 'PENDING' THEN 1
                WHEN f.status = 'LATE' THEN 2
                WHEN f.status = 'UNPAID' THEN 3
                WHEN f.status = 'PAID' THEN 4
              END ASC,
              f.dueDatetime DESC,
              f.id
          """)
  List<Fee> findAllByStudentIdSortByStatusAndDueDatetimeDescAndId(
      String studentId, Pageable pageable);

  @Query(
      """
      select f from Fee f
      left join User u on f.student = u
      where f.dueDatetime < :toCompare
      and u.id = :studentId
      and f.status = :status
      and f.remainingAmount > 0
      """)
  List<Fee> getStudentFeesUnpaidOrLateFrom(
      @Param(value = "toCompare") Instant toCompare,
      @Param("studentId") String studentId,
      @Param("status") FeeStatusEnum status);

  @Query(
      "SELECT DISTINCT f FROM Fee f "
          + "JOIN f.student u "
          + "JOIN f.statusHistories fsh "
          + "WHERE u.status != 'DISABLED' "
          + "AND fsh.datetime BETWEEN :dayStart AND :dayEnd "
          + "and f.isDeleted = false")
  List<Fee> findDistinctByStatusHistoriesDatetimeBetween(Instant dayStart, Instant dayEnd);

  @Query(
      "select f from Fee f "
          + "join f.student u "
          + "left join f.statusHistories fsh "
          + "where f.dueDatetime between :from and :to "
          + "and u.status != 'DISABLED' "
          + "and f.isDeleted = false "
          + "and (fsh is NULL or fsh.datetime = ("
          + "  select max(fsh2.datetime) from FeeStatusHistory fsh2 "
          + "  where fsh2.fee.id = f.id"
          + ") )"
          + "order by fsh.datetime desc")
  List<Fee> findAllByDueDatetimeBetween(Instant from, Instant to);

  List<Fee> findFeesByStudent_Id(String studentId);

  @Query(
      """
    select f from Fee f where f.id in :ids
""")
  List<Fee> findAllByIds(@Param("ids") List<String> ids);
}
