package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.CASH;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.PaymentTestData.aPayment;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class PaginationIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;
  @Autowired private PaymentRepository paymentRepository;

  private User studentAxel;
  private User managerHasina;
  private User teacherToky;

  private final List<User> students = new ArrayList<>();
  private final List<Fee> axelFees = new ArrayList<>();
  private final List<Payment> axelPayments = new ArrayList<>();

  private final String refPrefix =
      "PAG" + randomUUID().toString().replace("-", "").substring(0, 10);

  private String pageableRef(int rank) {
    return refPrefix + rank;
  }

  private String studentToken;
  private String managerToken;
  private String teacherToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel().toBuilder().ref(pageableRef(1)).build());
    students.add(studentAxel);
    for (int i = 2; i <= 8; i++) {
      students.add(userRepository.save(axel().toBuilder().ref(pageableRef(i)).build()));
    }
    managerHasina = userRepository.save(hasina());
    teacherToky = userRepository.save(toky());

    for (int i = 0; i < 6; i++) {
      axelFees.add(
          feeRepository.save(
              createPendingFee(
                  studentAxel,
                  5000,
                  Instant.parse("2022-12-08T08:25:24.00Z").plusSeconds(i * 86400L))));
    }

    var firstFee = axelFees.getFirst();
    for (int i = 0; i < 3; i++) {
      axelPayments.add(
          paymentRepository.save(
              aPayment(
                  firstFee,
                  CASH,
                  100,
                  "Comment",
                  Instant.parse("2022-11-08T08:25:24.00Z").plusSeconds(i * 3600L))));
    }
  }

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);
    setUpS3Service(fileService, managerHasina);
    setUpS3Service(fileService, teacherToky);

    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    paymentRepository.deleteAll(axelPayments);
    feeRepository.deleteAll(axelFees);
    axelPayments.clear();
    axelFees.clear();
    userRepository.deleteAll(students);
    students.clear();
    userRepository.deleteAll(List.of(managerHasina, teacherToky));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void student_pages_are_ordered_by_reference() throws ApiException {
    var api = new UsersApi(anApiClient(teacherToken));
    int pageSize = 4;

    var page1 =
        api.getStudents(1, pageSize, refPrefix, null, null, null, null, null, null, null, null);
    var page2 =
        api.getStudents(2, pageSize, refPrefix, null, null, null, null, null, null, null, null);
    var page10000 =
        api.getStudents(10000, pageSize, refPrefix, null, null, null, null, null, null, null, null);

    assertEquals(pageSize, page1.size());
    assertEquals(pageSize, page2.size());
    assertEquals(0, page10000.size());
    assertTrue(isBefore(page1.getFirst().getRef(), page1.get(1).getRef()));
    assertTrue(isBefore(page1.getLast().getRef(), page2.getFirst().getRef()));
  }

  @Test
  void fees_pages_are_ordered_by_due_datetime_desc() throws ApiException {
    var api = new PayingApi(anApiClient(studentToken));
    int pageSize = 2;

    var page1 = api.getFeesByStudentId(studentAxel.getId(), 1, pageSize, null);
    var page2 = api.getFeesByStudentId(studentAxel.getId(), 2, pageSize, null);
    var page3 = api.getFeesByStudentId(studentAxel.getId(), 3, pageSize, null);

    assertEquals(pageSize, page1.size());
    assertEquals(pageSize, page2.size());
    assertEquals(pageSize, page3.size());
    assertTrue(isAfter(page1.getFirst().getDueDatetime(), page1.get(1).getDueDatetime()));
    assertTrue(isAfter(page1.get(1).getDueDatetime(), page2.getFirst().getDueDatetime()));
  }

  @Test
  void payments_pages_are_ordered_by_creation_datetime_desc() throws ApiException {
    var api = new PayingApi(anApiClient(managerToken));
    var firstFeeId = axelFees.getFirst().getId();
    int pageSize = 2;

    var page1 = api.getStudentPayments(studentAxel.getId(), firstFeeId, 1, pageSize);
    var page2 = api.getStudentPayments(studentAxel.getId(), firstFeeId, 2, pageSize);
    var page3 = api.getStudentPayments(studentAxel.getId(), firstFeeId, 3, pageSize);

    assertEquals(pageSize, page1.size());
    assertEquals(1, page2.size());
    assertEquals(0, page3.size());
    assertTrue(isAfter(page1.getFirst().getCreationDatetime(), page1.get(1).getCreationDatetime()));
    assertTrue(isAfter(page1.get(1).getCreationDatetime(), page2.getFirst().getCreationDatetime()));
  }

  @Test
  void page_parameters_are_validated() {
    var api = new UsersApi(anApiClient(teacherToken));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page value must be >= 1\"}",
        () -> api.getStudents(0, 20, null, null, null, null, null, null, null, null, null));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size must be <500\"}",
        () -> api.getStudents(1, 1000, null, null, null, null, null, null, null, null, null));
  }

  private static boolean isBefore(String a, String b) {
    return a.compareTo(b) < 0;
  }

  private static boolean isAfter(Instant a, Instant b) {
    return a.compareTo(b) > 0;
  }
}
