package school.hei.haapi.integration;

import static java.time.Instant.now;
import static java.time.Month.APRIL;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.MVOLA;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.endpoint.rest.model.MpbsStatus.PENDING;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.FakeDataProvider.someMpbs;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognitoAndCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognitoAndCasdoorUser;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.MpbsMapper;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.FakeDataFactory.SomeUserFactory;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.MpbsRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
public class MpbsIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private MpbsRepository mpbsRepository;
  @Autowired private MpbsMapper mpbsMapper;

  private User student;
  private school.hei.haapi.model.Fee fee;
  private school.hei.haapi.model.mpbs.Mpbs mpbs;
  private static final String studentToken = "studentToken";

  @BeforeEach
  void setUp() {
    setUpCognitoAndCasdoor(casdoorAuthServiceMock, cognitoComponentMock, certificateLoaderMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());

    student = userRepository.save(new SomeUserFactory().build());
    fee = feeRepository.save(FakeDataProvider.somePendingFee(student));
    mpbs = mpbsRepository.save(someMpbs(fee));
    setUpCognitoAndCasdoorUser(casdoorAuthServiceMock, cognitoComponentMock, studentToken, student);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void manager_read_student_mobile_money_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Mpbs actual = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();

    assertEquals(expectedMpbs1(), actual);
  }

  @Test
  void student_read_own_mobile_money_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    Mpbs actual = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();

    assertEquals(expectedMpbs1(), actual);
  }

  @Test
  void monitor_read_own_followed_student_mobile_money_ok() throws ApiException {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);
    PayingApi api = new PayingApi(monitor1Client);

    Mpbs actual = api.getMpbs(STUDENT1_ID, FEE1_ID).getFirst();

    assertEquals(expectedMpbs1(), actual);
  }

  @Test
  void student_read_others_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsForbiddenException(() -> api.getMpbs(STUDENT2_ID, FEE2_ID));
  }

  @Test
  void monitor_read_others_student_mobile_money_ko() {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);
    PayingApi api = new PayingApi(monitor1Client);

    assertThrowsForbiddenException(() -> api.getMpbs(STUDENT2_ID, FEE2_ID));
  }

  @Test
  void student_update_mobile_payment_ok() throws ApiException {
    var api = new PayingApi(anApiClient(studentToken));

    var storedMpbs = api.getMpbs(student.getId(), fee.getId()).getFirst();
    assertEquals(mpbsMapper.toRest(mpbs), storedMpbs);

    var mpbsInUpdate = api.crupdateMpbs(student.getId(), fee.getId(), updatableMpbs1());
    var updated = mpbsMapper.toRest(mpbs);
    updated.setPspId("MP240726.1541.D88426");
    updated.setPspType(ORANGE_MONEY);
    assertEquals(updated.getStudentId(), mpbsInUpdate.getStudentId());
    assertEquals(updated.getPspId(), mpbsInUpdate.getPspId());
    assertEquals(updated.getFeeId(), mpbsInUpdate.getFeeId());
    assertEquals(updated.getPspType(), mpbsInUpdate.getPspType());

    // Assert that one fee has mpbs
    Mpbs actual1 = api.getMpbs(student.getId(), fee.getId()).getFirst();
    actual1.setCreationDatetime(actual1.getCreationDatetime().truncatedTo(MINUTES));
    mpbsInUpdate.setCreationDatetime(mpbsInUpdate.getCreationDatetime().truncatedTo(MINUTES));
    assertEquals(actual1, mpbsInUpdate);

    // Assert that when we get fees it not throws error 500
    var studentFee = api.getStudentFees(student.getId(), 1, 10, null);
    assertEquals(1, studentFee.size());
  }

  @Test
  void student_create_mobile_payment_ok() throws ApiException {
    var api = new PayingApi(anApiClient(studentToken));
    var manager1Api = new PayingApi(anApiClient(MANAGER1_TOKEN));

    var actualFee =
        manager1Api
            .createStudentFees(
                student.getId(),
                List.of(
                    new CreateFee()
                        .totalAmount(5000)
                        .dueDatetime(Instant.parse("2030-11-08T08:25:24.00Z"))
                        .category(UNKNOWN)
                        .frequency(FeeFrequency.UNKNOWN)
                        .type(TUITION)
                        .creationDatetime(now())
                        .comment("test")))
            .getFirst();
    assertEquals(student.getId(), actualFee.getStudentId());

    var crupdateMpbs = createableMpbsFromFeeIdWithStudent(actualFee.getId());
    var actualMpbs = api.crupdateMpbs(student.getId(), actualFee.getId(), crupdateMpbs);

    assertEquals(crupdateMpbs.getStudentId(), actualMpbs.getStudentId());
    assertEquals(crupdateMpbs.getPspId(), actualMpbs.getPspId());
    assertEquals(crupdateMpbs.getPspType(), actualMpbs.getPspType());

    var updatedFee = api.getStudentFeeById(student.getId(), actualFee.getId());

    assertEquals(FeeStatusEnum.PENDING, updatedFee.getStatus());
  }

  @Test
  void student_create_mobile_payments_ok() throws ApiException {
    var apiClient = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(apiClient);

    var savedStudent = createStudentForMobilePayments();
    var savedStudentFee = createFeeForMobilePayments(savedStudent);
    var toInsertUserMpbs1 = createRandomMpbs(savedStudent.getId(), savedStudentFee.getId());
    var toInsertUserMpbs2 = createRandomMpbs(savedStudent.getId(), savedStudentFee.getId());

    payingApi.crupdateMpbs(savedStudent.getId(), savedStudentFee.getId(), toInsertUserMpbs1);
    payingApi.crupdateMpbs(savedStudent.getId(), savedStudentFee.getId(), toInsertUserMpbs2);

    Fee studentFee = payingApi.getStudentFeeById(savedStudent.getId(), savedStudentFee.getId());
    assertEquals(2, studentFee.getMpbs().size());
  }

  private CrupdateMpbs createRandomMpbs(String studentId, String feeId) {
    var random = new Random();
    String pspId =
        String.format(
            "MP%06d.%04d.D%05d",
            random.nextInt(1_000_000), random.nextInt(10000), random.nextInt(100_000));
    return new CrupdateMpbs().studentId(studentId).feeId(feeId).pspId(pspId).pspType(ORANGE_MONEY);
  }

  private Fee createFeeForMobilePayments(User student) throws ApiException {
    var apiClient = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(apiClient);

    var toCreateStudentFee =
        new CreateFee()
            .type(TUITION)
            .totalAmount(5000)
            .category(UNKNOWN)
            .frequency(FeeFrequency.UNKNOWN)
            .comment("Comment")
            .dueDatetime(now());

    return payingApi.createStudentFees(student.getId(), List.of(toCreateStudentFee)).getFirst();
  }

  private User createStudentForMobilePayments() {
    var randomStudent =
        User.builder()
            .email("test_student_create_mobile_payments@test.com")
            .firstName("Test")
            .lastName("Payment_multiple_mpbs")
            .address("Address")
            .birthDate(LocalDate.of(2004, APRIL, 20))
            .phone("+261 00 00 000 00")
            .ref("STD-mpbs-multiple")
            .sex(M)
            .entranceDatetime(now())
            .birthPlace("Birthplace")
            .highSchoolOrigin("High School Origin")
            .status(ENABLED)
            .role(STUDENT)
            .build();

    return userService.saveAll(List.of(randomStudent)).getFirst();
  }

  public CrupdateMpbs updatableMpbs1() {
    return new CrupdateMpbs()
        .id(mpbs.getId())
        .studentId(student.getId())
        .feeId(fee.getId())
        .pspId("MP240726.1541.D88426")
        .pspType(ORANGE_MONEY);
  }

  public static Mpbs expectedMpbs1() {
    return new Mpbs()
        .id("mpbs1_id")
        .pspId("psp2_id")
        .studentId(STUDENT1_ID)
        .feeId(FEE1_ID)
        .pspType(MVOLA)
        .amount(8000)
        .successfullyVerifiedOn(Instant.parse("2021-11-08T08:25:24.00Z"))
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .status(PENDING);
  }

  public CrupdateMpbs createableMpbsFromFeeIdWithStudent(String feeId) {
    return createableMpbsFromFeeIdForStudent(student.getId(), feeId);
  }

  public static CrupdateMpbs createableMpbsFromFeeIdForStudent(String studentId, String feeId) {
    return new CrupdateMpbs()
        .studentId(studentId)
        .feeId(feeId)
        .pspType(ORANGE_MONEY)
        .pspId("MP240726.1541.D88425");
  }
}
