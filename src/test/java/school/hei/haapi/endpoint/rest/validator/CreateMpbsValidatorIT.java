package school.hei.haapi.endpoint.rest.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.exception.ApiException;

public class CreateMpbsValidatorIT extends FacadeITMockedThirdParties {
  @Autowired private CreateMpbsValidator subject;

  @BeforeEach
  void setUp() {
    setUpS3Service(fileService, axel());
  }

  @Test
  void assert_psp_id_is_given() {
    var exception =
        assertThrows(
            ApiException.class, () -> subject.accept("student1_id", "fee1_id", pspIdMissed()));
    var actualMessage = exception.getMessage();
    var expectedMessage = "Psp id is mandatory";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void assert_psp_id_is_right() {
    var exception =
        assertThrows(
            ApiException.class, () -> subject.accept("student1_id", "fee1_id", pspIdWasWrong()));
    var actualMessage = exception.getMessage();
    var expectedMessage = "Psp id = must be 20 characters and must begin by MP";

    assertTrue(actualMessage.contains(expectedMessage));
  }

  private CrupdateMpbs pspIdMissed() {
    return new CrupdateMpbs().pspType(ORANGE_MONEY).feeId("fee1_id").studentId("student1_id");
  }

  private CrupdateMpbs pspIdWasWrong() {
    return new CrupdateMpbs()
        .pspType(ORANGE_MONEY)
        .feeId("fee1_id")
        .studentId("student1_id")
        .pspId("ieoaifnipoa");
  }
}
