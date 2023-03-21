package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseTemplate {
  private String course_id;
  private CourseTemplate.status status;
  public enum status{
    LINKED,
    UNLINKED
  }
}
