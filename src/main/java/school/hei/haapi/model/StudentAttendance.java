package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import school.hei.haapi.endpoint.rest.model.AttendanceMovementType;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;

@Entity
@Table(name = "\"attendance\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class StudentAttendance implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private Instant createdAt;

  private boolean isLate;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private AttendanceMovementType attendanceMovementType;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private PlaceEnum place;

  @ManyToOne
  @JoinColumn(name = "course_session_id")
  private CourseSession courseSession;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  public boolean isAfter(Instant toCompare) {
    return this.createdAt.isAfter(toCompare);
  }

  public long lateOf(Instant toDefine) {
    long lateOf = 0;
    if (this.createdAt != null && toDefine != null && this.createdAt.isAfter(toDefine)) {
      Duration duration = Duration.between(this.createdAt, toDefine);
      lateOf = Math.abs(duration.toMinutes());
    }
    return lateOf;
  }
}
