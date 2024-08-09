package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.MVOLA;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateMpbs;
import school.hei.haapi.endpoint.rest.model.Mpbs;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class MpbsIT extends MockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  public void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_read_student_mobile_money_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Mpbs actual = api.getMpbs(STUDENT1_ID, FEE1_ID);

    assertEquals(expectedMpbs1(), actual);
  }

  @Test
  void student_read_own_mobile_money_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    Mpbs actual = api.getMpbs(STUDENT1_ID, FEE1_ID);

    assertEquals(expectedMpbs1(), actual);
  }

  @Test
  void student_read_others_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsForbiddenException(() -> api.getMpbs(STUDENT2_ID, FEE2_ID));
  }

  @Test
  void student_create_mobile_payment_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    Mpbs actual = api.createMpbs(STUDENT1_ID, FEE2_ID, createableMpbs1());

    assertEquals(createableMpbs1().getStudentId(), actual.getStudentId());
    assertEquals(createableMpbs1().getPspId(), actual.getPspId());
    assertEquals(createableMpbs1().getFeeId(), actual.getFeeId());
    assertEquals(createableMpbs1().getPspType(), actual.getPspType());
  }

  public static Mpbs expectedMpbs1() {
    return new Mpbs()
        .pspId("psp2_id")
        .studentId(STUDENT1_ID)
        .feeId(FEE1_ID)
        .pspType(MVOLA)
        .amount(8000)
        .successfullyVerifiedOn(Instant.parse("2021-11-08T08:25:24.00Z"))
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .status(PENDING);
  }

  public static CreateMpbs createableMpbs1() {
    return new CreateMpbs().studentId(STUDENT1_ID).feeId(FEE2_ID).pspType(MVOLA).pspId("psp1_id");
  }

  public static CreateMpbs createableMpbsFromFeeIdWithStudent1(String feeId) {
    return new CreateMpbs()
        .studentId(STUDENT1_ID)
        .feeId(feeId)
        .pspType(ORANGE_MONEY)
        .pspId("psp_test");
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
