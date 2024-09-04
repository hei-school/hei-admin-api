package school.hei.haapi.service.utils;

import java.util.LinkedHashSet;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.service.PromotionService;

@AllArgsConstructor
@Component
public class IsStudentRepeatingYear implements Function<User, Boolean> {
  private final PromotionService promotionService;

  @Override
  public Boolean apply(User user) {
    LinkedHashSet<school.hei.haapi.model.Promotion> studentPromotions =
        getAllStudentPromotions(user);

    if (studentPromotions.isEmpty()) {
      return false;
    }

    return didRepeatYear(user);
  }

  private boolean didRepeatYear(User user) {
    return getAllStudentPromotions(user).size() > 1;
  }

  private LinkedHashSet<Promotion> getAllStudentPromotions(User user) {
    return promotionService.getAllStudentPromotions(user.getId());
  }
}
