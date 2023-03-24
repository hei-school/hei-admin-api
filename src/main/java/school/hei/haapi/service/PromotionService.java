package school.hei.haapi.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.repository.PromotionRepository;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
@AllArgsConstructor
public class PromotionService {
  private final PromotionRepository promotionRepository;

  public static LocalDate instantToLocalDate(Instant entranceDatetime) {
    return entranceDatetime.atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public static String definePromotionName(Instant entranceDatetime) {
    int year = instantToLocalDate(entranceDatetime).getYear();
    int lastTwoDigits = (year % 100);
    return "promotion" + lastTwoDigits;
  }

  public static LocalDate definePromotionBeginning(Instant entranceDatetime) {
    int year = entranceDatetime.atZone(ZoneOffset.UTC).getYear();
    return LocalDate.of(year, Month.JULY, 1);
  }

  public static LocalDate definePromotionEnd(Instant entranceDatetime) {
    int year = entranceDatetime.atZone(ZoneOffset.UTC).getYear();
    return LocalDate.of(year + 1, Month.AUGUST, 31);
  }

  public static String definePromotionRange(Instant entranceDatetime) {
    int year = instantToLocalDate(entranceDatetime).getYear();
    return year + "-" + (year + 1);
  }

  public List<Promotion> getPromotions() {
    return promotionRepository.findAll();
  }

  public Promotion getPromotionById(String promotionId) {
    return promotionRepository.getPromotionById(promotionId);
  }

  @Transactional(isolation = SERIALIZABLE)
  public Promotion createPromotion(Promotion toCreate) {
    return promotionRepository.save(toCreate);
  }

  public Promotion getByRange(String range) {
    return promotionRepository.getPromotionByPromotionRange(range);
  }

  public boolean existsByRange(String range) {
    return promotionRepository.existsPromotionByPromotionRange(range);
  }
}
