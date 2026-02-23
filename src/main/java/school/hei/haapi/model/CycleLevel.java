package school.hei.haapi.model;

import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import school.hei.haapi.endpoint.rest.model.StudentLevel;

@Getter
@AllArgsConstructor
public enum CycleLevel {
  BACHELOR(List.of(L1, L2, L3)),
  MASTER(List.of(M1, M2));

  private final List<StudentLevel> levels;
}
