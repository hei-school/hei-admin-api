package school.hei.haapi.model;

import static javax.persistence.GenerationType.IDENTITY;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import school.hei.haapi.endpoint.rest.model.AttendanceMovementType;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;

@Entity
@Table(name = "\"attendance\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentAttendance implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private Instant createdAt;

  private boolean isLate;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private AttendanceMovementType attendanceMovementType;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private PlaceEnum place;

  @ManyToOne
  @JoinColumn(name = "course_session_id")
  private CourseSession courseSession;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;
}
