package school.hei.haapi.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.service.utils.AcademicYear;
import school.hei.haapi.service.utils.SchoolYearSupplier;

@Slf4j
@Component
@AllArgsConstructor
public class StudentLevelResolver {
  private final PromotionService promotionService;
  private final SchoolYearSupplier schoolYearSupplier;

  public Optional<StudentLevel> findLevelAt(User student, Instant instant) {
    try {
      var promotions = new ArrayList<>(promotionService.getAllStudentPromotions(student.getId()));
      for (var i = promotions.size() - 1; i >= 0; i--) {
        var level = promotions.get(i).findLevelAt(instant);
        if (level.isPresent()) {
          return level;
        }
      }
      log.info(
          "No level for student {} at {}, promotions: {}",
          student.getRef(),
          instant,
          promotions.stream().map(Promotion::getRef).toList());
    } catch (RuntimeException e) {
      log.warn("Cannot compute level of student {}: {}", student.getRef(), e.getMessage());
    }
    return Optional.empty();
  }

  public Optional<StudentLevel> findLevelOf(User student, AcademicYear academicYear) {
    return findLevelAt(student, academicYear.levelInstant());
  }

  public Optional<StudentLevel> findCurrentLevel(User student) {
    return findLevelOf(student, AcademicYear.parse(schoolYearSupplier.get()));
  }

  /**
   * A student who went out after its Licence keeps its badge, without expiration: it has no level
   * any more because its cycle is over. A student in L3 still gets a badge for the year.
   */
  public boolean keepsBadgeAfter(User student, AcademicYear academicYear) {
    var levelInstant = academicYear.levelInstant();
    return findLevelAt(student, levelInstant).isEmpty()
        && promotionService.getAllStudentPromotions(student.getId()).stream()
            .anyMatch(promotion -> promotion.isOverAt(levelInstant));
  }
}
