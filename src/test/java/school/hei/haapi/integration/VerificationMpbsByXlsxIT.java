package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.SUCCESS;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.MpbsMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Mpbs;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.MpbsStatusHistoryRepository;
import school.hei.haapi.service.MpbsVerificationService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class VerificationMpbsByXlsxIT extends FacadeITMockedThirdParties {
  public static final String MPBS_FEE4_ID = "mpbs3_id";
  public static final String MPBS_FEE4_REF = "MP241210.0817.B36568";
  public static final String FEE8_ID = "fee8_id";
  @Autowired MpbsVerificationService verificationService;
  @Autowired MpbsMapper mpbsMapper;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private MpbsStatusHistoryRepository mpbsStatusHistoryRepository;

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void verify_mpbs_via_xls() throws ApiException, IOException {
    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);

    // Check if the Mpbs exists
    Mpbs fee3BeforeVerification = api.getMpbs(STUDENT1_ID, FEE8_ID).getFirst();

    assertEquals(PENDING, fee3BeforeVerification.getStatus());
    assertEquals(MPBS_FEE4_REF, fee3BeforeVerification.getPspId());

    // Initialize mpbs status history
    api.crupdateMpbs(
        STUDENT1_ID,
        FEE8_ID,
        new CrupdateMpbs()
            .id(MPBS_FEE4_ID)
            .feeId(FEE8_ID)
            .studentId(STUDENT1_ID)
            .pspType(ORANGE_MONEY)
            .pspId(MPBS_FEE4_REF));

    // Upload xls file
    List<Mpbs> mpbsVerified =
        verificationService.computeFromXls(getMockedFile("test-mpbs", ".xls")).stream()
            .map(mpbsMapper::toRest)
            .toList();

    Mpbs actualMpbs = mpbsVerified.getFirst();

    // Check mpbs status and stored status history
    var mpbsStatusHistories = mpbsStatusHistoryRepository.findAllByMpbs_PspId(MPBS_FEE4_REF);
    assertEquals(SUCCESS, actualMpbs.getStatus());
    assertEquals(MPBS_FEE4_REF, actualMpbs.getPspId());
    assertEquals(1, mpbsStatusHistories.size());
    assertEquals(PENDING, mpbsStatusHistories.getFirst().getStatus());

    // Check if the fee is paid
    Fee actualFee = api.getStudentFeeById(STUDENT1_ID, FEE8_ID);
    assertEquals(0, actualFee.getRemainingAmount());
    assertEquals(PAID, actualFee.getStatus());
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
