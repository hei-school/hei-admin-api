package school.hei.haapi.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import school.hei.haapi.endpoint.rest.model.CourseStatus;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"student_course\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StudentCourse implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;
  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User student;
  @OneToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private CourseStatus status;
}
