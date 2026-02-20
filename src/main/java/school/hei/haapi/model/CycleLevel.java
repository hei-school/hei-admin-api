package school.hei.haapi.model;

import java.util.List;
import lombok.Getter;
import school.hei.haapi.endpoint.rest.model.StudentLevel;

@Getter
public enum CycleLevel {
  BACHELOR(List.of(StudentLevel.L1, StudentLevel.L2, StudentLevel.L3)),
  MASTER(List.of(StudentLevel.M1, StudentLevel.M2));

  private final List<StudentLevel> levels;

  CycleLevel(List<StudentLevel> levels) {
    this.levels = levels;
  }
}
