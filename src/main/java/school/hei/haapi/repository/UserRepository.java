package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  User getByEmail(String email);

  List<User> findAllByStatus(User.Status status);

  List<User> findAllByRoleAndStatus(User.Role role, User.Status status);

  @Modifying
  @Query("update User u set u.status = :status where u.id = :user_id")
  void updateUserStatusById(@Param("status") User.Status status, @Param("user_id") String id);

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
                                  and (?2 is null or u.ref = ?2)
                                  and (?3 is null or u.first_name = ?3)
                                  and (?4 is null or u.last_name = ?4)
                                  """)
	Optional<List<User>> findStudentGroupsWithFilter(String groupId, String studentRef, String studentFirstName, String studentLastName);


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
						""")
  Optional<List<User>> findAllRemainingStudentsByGroupId(String groupId);

  @Query(
      nativeQuery = true,
      value =
          """
		SELECT * FROM "user" u WHERE u."role" = 'STUDENT' and u.status <> 'DISABLED'
""")
  List<User> findAllStudentNotDisabled();

  Integer countBySexAndRole(User.Sex sex, User.Role role);

  Integer countByRole(User.Role role);

  Integer countBySexAndRoleAndStatus(User.Sex sex, User.Role role, User.Status status);

  Integer countByRoleAndStatus(User.Role role, User.Status status);
}
