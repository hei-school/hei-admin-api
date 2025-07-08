package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.FAILED;
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
import static school.hei.haapi.model.User.Role.STUDENT;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.MpbsMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateMpbs;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Mpbs;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.MpbsStatusHistoryRepository;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.MpbsVerificationService;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class VerificationMpbsByXlsxIT extends FacadeITMockedThirdParties {
  public static final String MPBS_FEE4_ID = "mpbs3_id";
  public static final String MPBS_FEE4_REF = "MP241210.0817.B36568";
  public static final String FEE8_ID = "fee8_id";
  @Autowired MpbsVerificationService subject;
  @Autowired MpbsMapper mpbsMapper;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private MpbsStatusHistoryRepository mpbsStatusHistoryRepository;
  @Autowired private FeeService feeService;
  @Autowired private MpbsService mpbsService;
  @Autowired private UserService userService;

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void verify_mpbs_via_xls() throws ApiException, IOException {
    var someStudent = userService.saveAll(List.of(User.builder().role(STUDENT).build())).getFirst();
    var someFee =
        feeService
            .saveAll(List.of(school.hei.haapi.model.Fee.builder().student(someStudent).build()))
            .getFirst();
    var someMpbs =
        mpbsService.saveMpbs(
            school.hei.haapi.model.Mpbs.Mpbs.builder()
                .fee(someFee)
                .student(someStudent)
                .pspId(MPBS_FEE4_REF)
                .build());

    ApiClient managerClient = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(managerClient);

    // Check if the Mpbs exists
    Mpbs fee3BeforeVerification = api.getMpbs(someStudent.getId(), someFee.getId()).getFirst();

    assertEquals(PENDING, fee3BeforeVerification.getStatus());
    assertEquals(MPBS_FEE4_REF, fee3BeforeVerification.getPspId());

    // Initialize mpbs status history
    api.crupdateMpbs(
        someStudent.getId(),
        someFee.getId(),
        new CrupdateMpbs()
            .id(someMpbs.getId())
            .feeId(someFee.getId())
            .studentId(someStudent.getId())
            .pspType(ORANGE_MONEY)
            .pspId(MPBS_FEE4_REF));

    // Upload xls file
    List<Mpbs> mpbsVerified =
        subject
            .computeFromXls(getMockedFile("test-mpbs", ".xls"))
            .stream() // Todo: data need to match somehow XD
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

  @Test
  void xlsx_correctly_extracted() {
    MpbsRepository mpbsRepository = mock();
    MobilePaymentService mobilePaymentService = mock();
    MpbsVerificationService subjectMocked =
        new MpbsVerificationService(
            mock(), mpbsRepository, mobilePaymentService, mock(), mock(), mock(), mock(), mock());

    var transactions =
        List.of(
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241213.0844.B33334")
                .pspTransactionAmount(330000)
                .status(FAILED)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241209.1404.B96583")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241210.0817.B36568")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241210.1028.D46037")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241210.1147.A49685")
                .pspTransactionAmount(265000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241210.1241.C53158")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241211.2027.A49333")
                .pspTransactionAmount(265000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241211.2315.C57348")
                .pspTransactionAmount(265000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241212.0655.D65919")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241212.0959.D75969")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241212.1733.C01770")
                .pspTransactionAmount(330000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241212.1804.A03686")
                .pspTransactionAmount(265000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241212.1810.C04098")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241213.1107.A42802")
                .pspTransactionAmount(330000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241214.0858.A99067")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241214.1114.D09555")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241214.1337.D17845")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build(),
            MobileTransactionDetails.builder()
                .pspTransactionRef("MP241215.1137.D71706")
                .pspTransactionAmount(288000)
                .status(SUCCESS)
                .build());
    var fakePendingSavedMpbs =
        transactions.stream()
            .map(
                t ->
                    (school.hei.haapi.model.Mpbs.Mpbs)
                        school.hei.haapi.model.Mpbs.Mpbs.builder()
                            .pspId(t.getPspTransactionRef())
                            .build())
            .toList();
    when(mpbsRepository.findByPspIdIn(anyList())).thenReturn(List.of());
    when(mpbsRepository.findAllByStatus(PENDING)).thenReturn(fakePendingSavedMpbs);

    assertDoesNotThrow(() -> subjectMocked.computeFromXls(getMockedFile("test-mpbs", ".xls")));

    ArgumentCaptor<List<MobileTransactionDetails>> argumentCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(mobilePaymentService, times(1)).saveAll(argumentCaptor.capture());
    List<MobileTransactionDetails> captured = argumentCaptor.getAllValues().getFirst();
    captured.forEach(
        mobileTransactionDetails -> {
          mobileTransactionDetails.setId(null);
          // Todo: verify if transaction date match with the content
          mobileTransactionDetails.setPspDatetimeTransactionCreation(null);
          mobileTransactionDetails.setPspOwnDatetimeVerification(null);
        });
    assertEquals(transactions.size(), captured.size());
    assertEquals(transactions, captured);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
