package school.hei.haapi.service.utils;

import java.time.Instant;
import java.util.List;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;

public final class StudentLevelUtils {
  private StudentLevelUtils() {}

  public static List<StudentLevel> levelsUpToCurrentOf(User student) {
    var currentPromotion = student.findCurrentGroup().map(Group::getPromotion).orElse(null);
    if (currentPromotion == null) {
      return List.of();
    }
    StudentLevel currentLevel;
    try {
      currentLevel = currentPromotion.getLevelAt(Instant.now());
    } catch (PromotionLevelOutOfRangeException e) {
      return List.of();
    }
    var cycleLevels = currentPromotion.getCycleLevel().getLevels();
    var currentIndex = cycleLevels.indexOf(currentLevel);
    if (currentIndex < 0) {
      return List.of();
    }
    return cycleLevels.subList(0, currentIndex + 1);
  }
}
