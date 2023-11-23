package school.hei.haapi.model.notEntity;

import java.io.Serializable;
import java.util.List;
import lombok.*;
import school.hei.haapi.model.Group;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroup implements Serializable {
  private Group group;
  private List<String> studentsToAdd;
}
