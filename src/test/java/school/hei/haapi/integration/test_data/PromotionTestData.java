package school.hei.haapi.integration.test_data;

import static school.hei.haapi.endpoint.rest.model.CycleEnum.BACHELOR;

import school.hei.haapi.endpoint.rest.model.CrupdatePromotion;

public class PromotionTestData {
  public static CrupdatePromotion promotion26() {
    var crupdatePromotion = new CrupdatePromotion();
    crupdatePromotion.setId("promotion2026");
    crupdatePromotion.setRef("N");
    crupdatePromotion.setName("Promotion 2025-2026");
    crupdatePromotion.setCycleLevel(BACHELOR);
    return crupdatePromotion;
  }
}
