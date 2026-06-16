package school.hei.haapi.repository;

import jakarta.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;
import school.hei.haapi.model.User.Role;
import school.hei.haapi.model.dto.StatisticsDto;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);

  List<User> findAllByStatus(User.Status status);

  List<User> findAllByRoleAndStatus(Role role, User.Status status);

  @Query(
      value =
          """
          WITH student_group_flow AS (
              SELECT
                  gf.group_id,
                  gf.student_id
              FROM
                  group_flow gf
              WHERE
                  gf.group_id = ?1
              GROUP BY
                  gf.group_id,
                  gf.student_id
              HAVING
                  --join count
                  SUM(CASE WHEN gf.group_flow_type = 'JOIN' THEN 1 ELSE 0 END) >
                  --leave count
                  SUM(CASE WHEN gf.group_flow_type = 'LEAVE' THEN 1 ELSE 0 END)
          )
          SELECT
              u.*
          FROM
              "user" u
                   INNER JOIN
              student_group_flow sgf
              ON
                  sgf.student_id = u.id
          where u.status <> 'DISABLED'
          and (?2 is null or u.first_name ILIKE CONCAT('%', ?2, '%'))
          """,
      countQuery =
          """
          WITH student_group_flow AS (
              SELECT
                  gf.group_id,
                  gf.student_id
              FROM
                  group_flow gf
              WHERE
                  gf.group_id = ?1
              GROUP BY
                  gf.group_id,
                  gf.student_id
              HAVING
                  --join count
                  SUM(CASE WHEN gf.group_flow_type = 'JOIN' THEN 1 ELSE 0 END) >
                  --leave count
                  SUM(CASE WHEN gf.group_flow_type = 'LEAVE' THEN 1 ELSE 0 END)
          )
          SELECT
              count(u.*)
          FROM
              "user" u
                   INNER JOIN
              student_group_flow sgf
              ON
                  sgf.student_id = u.id
          where u.status <> 'DISABLED'
          and (?2 is null or u.first_name ILIKE CONCAT('%', ?2, '%'))
          """,
      nativeQuery = true)
  Page<User> findStudentGroupsWithFilter(
      String groupId, String studentFirstname, Pageable pageable);

  /** Use UserManagerDao::findByCriteria instead */
  @Deprecated
  @Query(
      nativeQuery = true,
      value =
          """
          WITH student_group_flow AS (
              SELECT
                  gf.group_id,
                  gf.student_id
              FROM
                  group_flow gf
              WHERE
                  gf.group_id IN ?1
              GROUP BY
                  gf.group_id,
                  gf.student_id
              HAVING
                  --join count
                  SUM(CASE WHEN gf.group_flow_type = 'JOIN' THEN 1 ELSE 0 END) >
                  --leave count
                  SUM(CASE WHEN gf.group_flow_type = 'LEAVE' THEN 1 ELSE 0 END)
          )
          SELECT
              u.*
          FROM
              "user" u
          	        INNER JOIN
              student_group_flow sgf
              ON
                  sgf.student_id = u.id
          where u.status <> 'DISABLED'
          """)
  List<User> findAllRemainingStudentsByGroupIds(Collection<String> groupIds, Pageable pageable);

  @Query(
      nativeQuery = true,
      value =
          """
	WITH student_group_flow AS (
     SELECT
         gf.student_id
     FROM
         group_flow gf
         INNER JOIN "group" g ON g.id = gf.group_id  -- Join to get promotion info
     WHERE
         g.promotion_id = ?1  -- Filter by promotion_id from the Promotion table
     GROUP BY
         gf.student_id
     HAVING
         -- Join count > Leave count
         SUM(CASE WHEN gf.group_flow_type = 'JOIN' THEN 1 ELSE 0 END) >
         SUM(CASE WHEN gf.group_flow_type = 'LEAVE' THEN 1 ELSE 0 END)
 )
 SELECT
     u.*
 FROM
     "user" u
     INNER JOIN student_group_flow sgf ON sgf.student_id = u.id
 WHERE
     u.status <> 'DISABLED';
""")
  List<User> findAllStudentsByPromotionId(String promotionId);

  @Query(
      nativeQuery = true,
      value =
          """
		SELECT * FROM "user" u WHERE u."role" = 'STUDENT' and u.status <> 'DISABLED'
""")
  List<User> findAllStudentNotDisabled();

  long countBySexAndRole(User.Sex sex, Role role);

  long countByRole(Role role);

  long countBySexAndRoleAndStatus(User.Sex sex, Role role, User.Status status);

  List<User> findAllByRefIn(List<String> refs);

  @Query(
      "select u from User u join Fee f on u.id = f.student.id where f.status = 'LATE' and"
          + " f.isDeleted = false")
  List<User> getStudentsWithLateFees();

  Optional<User> findByRef(@NotBlank(message = "Reference is mandatory") String ref);

  List<User> findAllByRoleInAndIdIn(Collection<Role> roles, Collection<String> ids);

  @Query(
      """
      SELECT new school.hei.haapi.model.dto.StatisticsDto(
          new school.hei.haapi.model.dto.StatisticsDetailsDto(
              COALESCE(SUM(CASE WHEN u.sex = 'F' AND u.status = 'DISABLED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'F' AND u.status = 'ENABLED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'F' AND u.status = 'SUSPENDED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'F' AND u.status = 'ALUMNI' THEN 1 ELSE 0 END ), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'F' THEN 1 ELSE 0 END), 0)
          ),
          new school.hei.haapi.model.dto.StatisticsDetailsDto(
              COALESCE(SUM(CASE WHEN u.sex = 'M' AND u.status = 'DISABLED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'M' AND u.status = 'ENABLED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'M' AND u.status = 'SUSPENDED' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'M' AND u.status = 'ALUMNI' THEN 1 ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN u.sex = 'M' THEN 1 ELSE 0 END), 0)
          ),
          (select count(g) from Group g),
          count(u)
      ) from User u where u.role = 'STUDENT'
      """)
  StatisticsDto getStudentsStatistics();

  @Query(
      """
      SELECT u FROM User u
      WHERE(
             :search IS NULL
          OR :search = ''
          OR LOWER(u.ref) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
        )
      """)
  List<User> searchUsers(@Param("search") String search);

  @Query(
      """
      SELECT DISTINCT u
      FROM User u
      JOIN FETCH u.groupFlows gf
      JOIN FETCH gf.group g
      JOIN FETCH g.promotion p
      WHERE u.role = 'STUDENT'
        AND u.status <> 'DISABLED'
        AND (:promotionId IS NULL OR p.id = :promotionId)
      """)
  List<User> findAllStudentNotDisabledWithGroupFlow(String promotionId);
}
