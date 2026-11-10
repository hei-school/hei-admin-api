package school.hei.haapi.service.utils;

import java.time.Instant;
import java.util.List;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

public final class StudentLevelUtils {
  private StudentLevelUtils() {}

  public static List<StudentLevel> levelsUpToCurrentOf(User student) {
    var currentPromotion = student.findCurrentGroup().map(Group::getPromotion).orElse(null);
    if (currentPromotion == null) {
      return List.of();
    }
    return currentPromotion
        .findLevelAt(Instant.now())
        .map(currentPromotion::getLevelsUpToAndIncluding)
        .orElse(List.of());
  }
}
