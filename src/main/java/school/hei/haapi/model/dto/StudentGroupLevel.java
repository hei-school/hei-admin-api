package school.hei.haapi.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Group;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StudentGroupLevel {
  private Group group;
  private List<StudentLevel> studentLevels;
}
