package school.hei.haapi.model;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import school.hei.haapi.endpoint.rest.model.CourseStatus;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"course_followed\"")
@Builder
public class CourseFollowed {
  @Id
  private String idCourseFollowed;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne
  @JoinColumn(name = "course_id")
  private Course course;

  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  private CourseStatus status;
}