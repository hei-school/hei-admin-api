package school.hei.haapi.service.utils;

import java.time.Year;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.PromotionRepository;

@AllArgsConstructor
@Component
public class IsStudentRepeatingYear implements Function<User, Boolean> {
  private final PromotionRepository promotionRepository;

  @Override
  public Boolean apply(User user) {
    LinkedHashSet<school.hei.haapi.model.Promotion> studentPromotions =
        getAllStudentPromotions(user);

    if (studentPromotions.isEmpty()) {
      return false;
    }
    school.hei.haapi.model.Promotion mostRecentPromotion = studentPromotions.getFirst();
    int promotionStartYear =
        mostRecentPromotion.getStartDatetime().atZone(ZoneId.of("UTC+3")).getYear();
    int currentYear = Year.now().getValue();

    return didRepeatYear(user)
        && (promotionStartYear == currentYear || promotionStartYear + 1 == currentYear)
        && promotionStartYear + 1 <= currentYear;
  }

  private boolean didRepeatYear(User user) {
    int normalNumberOfPromotion = 1;
    return getAllStudentPromotions(user).size() > normalNumberOfPromotion;
  }

  private LinkedHashSet<Promotion> getAllStudentPromotions(User user) {
    return promotionRepository.findAllPromotionsByStudentId(user.getId());
  }
}
