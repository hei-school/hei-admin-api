package school.hei.haapi.integration.testData;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.endpoint.rest.model.CycleEnum.BACHELOR;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import school.hei.haapi.endpoint.rest.model.CrupdatePromotion;
import school.hei.haapi.model.CycleLevel;
import school.hei.haapi.model.Promotion;

public class PromotionTestData {
  public static CrupdatePromotion promotion26() {
    var crupdatePromotion = new CrupdatePromotion();
    crupdatePromotion.setId("promotion2026");
    crupdatePromotion.setRef("N");
    crupdatePromotion.setName("Promotion 2025-2026");
    crupdatePromotion.setCycleLevel(BACHELOR);
    return crupdatePromotion;
  }

  public static Promotion aPromotion(String name, String ref) {
    return Promotion.builder()
        .id(randomUUID().toString())
        .name(name)
        .ref(ref)
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        // relative to now: the level is derived from the years elapsed since the start, so a fixed
        // date silently walks out of the cycle range as time passes
        .startDatetime(ZonedDateTime.now().minusYears(1).toInstant())
        .cycleLevel(CycleLevel.BACHELOR)
        .groups(new ArrayList<>())
        .build();
  }

  public static CrupdatePromotion aCrupdatePromotion(String name, String ref) {
    return new CrupdatePromotion()
        .id(randomUUID().toString())
        .ref(ref)
        .name(name)
        .cycleLevel(BACHELOR);
  }
}
