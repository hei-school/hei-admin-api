package school.hei.haapi.model.psp.vola;

import static org.junit.Assert.assertEquals;
import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.VolaPsp;

class VolaPspIT extends FacadeITMockedThirdParties {
  private final VolaPsp volaPsp;

  public VolaPspIT(
      @Value("${vola.api.url}") String volaApiUrl, @Value("${vola.api.key}") String volaApiKey) {
    VolaClient volaclient = new VolaClient(volaApiUrl, volaApiKey);
    this.volaPsp = new VolaPsp(volaclient);
  }

  // Will be used in more tests later
  private final String TEST_EMAIL = "tiavina.3@mail.hei.school";
  private final String TEST_PSP_ID = "MP250917.1604.D33118";
  private final PspType TEST_MOBILE_MONEY_TYPE = ORANGE_MONEY;

  @Test
  void read_succeeded_payment() {

    var verifiedPayment = volaPsp.get(TEST_MOBILE_MONEY_TYPE, TEST_PSP_ID, TEST_EMAIL);

    assertEquals(SUCCEEDED, verifiedPayment.getVerificationStatus());
    assertEquals(700L, verifiedPayment.getPspPayment().getAmount().longValue());
  }
}
