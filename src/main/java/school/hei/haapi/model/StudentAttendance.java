package school.hei.haapi.model;

import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

import java.io.Serializable;
import java.time.Duration;
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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import school.hei.haapi.endpoint.rest.model.AttendanceMovementType;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.repository.types.PostgresEnumType;

@Entity
@Table(name = "\"attendance\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
@SQLDelete(sql = "update \"attendance\" set is_deleted = true where id=?")
@Where(clause = "is_deleted=false")
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
  @OnDelete(action = CASCADE)
  private CourseSession courseSession;

  @ManyToOne
  @JoinColumn(name = "student_id")
  @OnDelete(action = CASCADE)
  private User student;

  private boolean isDeleted;

  public boolean isLateFrom(Instant toCompare) {
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
