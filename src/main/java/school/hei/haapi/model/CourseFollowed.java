package school.hei.haapi.model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.haapi.endpoint.rest.model.CourseStatus;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"course_followed\"")
@Builder
public class CourseFollowed {
  @Id
  private String id;

  @ManyToOne
  @MapsId("id")
  @JoinColumn(name = "student_id")
  private User student;

  @ManyToOne
  @MapsId("courseId")
  @JoinColumn(name = "course_id")
  private Course course;

  private CourseStatus status;
}