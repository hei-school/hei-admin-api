package school.hei.haapi.model;

import java.io.Serializable;
import java.util.Objects;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.repository.types.PostgresEnumType;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"student_course\"")
@Getter
@Setter
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourse implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User userId;

  @ManyToOne
  @JoinColumn(name = "course_id", referencedColumnName = "id")
  private Course courseId;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private CourseStatus status;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StudentCourse that = (StudentCourse) o;
    return Objects.equals(id, that.id)
        && Objects.equals(userId, that.userId)
        && Objects.equals(courseId, that.courseId)
        && status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, courseId, status);
  }
}
