package school.hei.haapi.model.notEntity;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Group {

  private String id;
  private String ref;
  private String name;
}
