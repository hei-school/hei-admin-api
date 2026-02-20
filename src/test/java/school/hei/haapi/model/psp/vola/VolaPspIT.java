package school.hei.haapi.model.psp.vola;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.model.psp.PspType.ORANGE_MONEY;
import static school.hei.haapi.model.psp.vola.api.gen.client.model.Payment.VerificationStatusEnum.SUCCEEDED;

import org.junit.jupiter.api.Test;
import school.hei.haapi.model.psp.PspType;
import school.hei.haapi.model.psp.vola.api.VolaClient;
import school.hei.haapi.model.psp.vola.api.VolaPsp;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentId;

public class VolaPspIT {
  private final String TEST_EMAIL = "tiavina.3@mail.hei.school";
  private final String TEST_PSP_ID = "MP250917.1604.D33118";
  private final PspType TEST_MOBILE_MONEY_TYPE = ORANGE_MONEY;

  @Test
  void read_succeeded_payment() {

    var baseUrl = System.getenv("VOLA_API_URL");
    var apiKey = System.getenv("VOLA_API_KEY");
    var volaPsp = new VolaPsp(new VolaClient(baseUrl, apiKey));

    var verifiedPayment =
        volaPsp.getPayment(
            PaymentId.builder()
                .pspPaymentId(TEST_PSP_ID)
                .pspType(TEST_MOBILE_MONEY_TYPE)
                .payerEmail(TEST_EMAIL)
                .build());

    assertEquals(SUCCEEDED, verifiedPayment.getVerificationStatus());
    assertEquals(700L, verifiedPayment.getPspPayment().getAmount().longValue());
  }
}
