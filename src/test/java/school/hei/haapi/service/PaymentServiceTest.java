package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.FakeDataProvider.someLateFee;
import static school.hei.haapi.integration.conf.FakeDataProvider.someMpbs;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognitoAndCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Sex.M;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaymentServiceTest.ContextInitializer.class)
@AutoConfigureMockMvc
class PaymentServiceTest extends MockedThirdParties {
  @Autowired private PaymentService subject;
  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeRepository feeRepository;

  private User studentSuspend;
  private Fee feeSuspendStudent;

  @BeforeEach
  void setUp() {
    setUpCognitoAndCasdoor(casdoorAuthServiceMock, cognitoComponentMock, certificateLoaderMock);
    setUpS3Service(fileService, TestUtils.student1());
    setUpEventBridge(eventBridgeClientMock);

    studentSuspend = userRepository.save(someStudent(User.Status.SUSPENDED));
    feeSuspendStudent = feeRepository.save(someLateFee(studentSuspend, 1_000_000));
  }

  @Test
  void user_status_is_stay_suspend_after_paying_fee_with_mpbs_without_sufficient_amount()
      throws ApiException {
    var usersApi = new UsersApi(anApiClient(MANAGER1_TOKEN));
    var mpbs = someMpbs(feeSuspendStudent, 1);

    subject.savePaymentFromMpbs(mpbs, mpbs.getAmount());
    subject.computeRemainingAmount(feeSuspendStudent.getId(), mpbs.getAmount());

    var actualStudent = usersApi.getStudentById(studentSuspend.getId());
    assertEquals(SUSPENDED, actualStudent.getStatus());
  }

  @Test
  void user_status_is_computed_after_paying_fee_by_mpbs() throws ApiException {
    var usersApi = new UsersApi(anApiClient(MANAGER1_TOKEN));
    var mpbs = someMpbs(feeSuspendStudent);

    subject.savePaymentFromMpbs(mpbs, mpbs.getAmount());
    subject.computeRemainingAmount(feeSuspendStudent.getId(), mpbs.getAmount());

    var actualStudent = usersApi.getStudentById(studentSuspend.getId());
    assertEquals(ENABLED, actualStudent.getStatus());
  }

  @Test
  @DirtiesContext
  void compute_user_status_after_paying_fee_ok() {
    User userWithUnpaidFees = student2();
    User userWithoutUnpaidFees = student3();

    subject.computeUserStatusAfterPayingFee(userWithUnpaidFees);
    subject.computeUserStatusAfterPayingFee(userWithoutUnpaidFees);
    User updatedUserWithUnpaidFees = userService.getById(userWithUnpaidFees.getId());
    User updatedUserWithoutUnpaidFees = userService.getById(userWithoutUnpaidFees.getId());

    assertEquals(User.Status.SUSPENDED, updatedUserWithUnpaidFees.getStatus());
    assertEquals(User.Status.ENABLED, updatedUserWithoutUnpaidFees.getStatus());

    // here student2 has paid all their fees late
    subject.computeRemainingAmount(student2UnpaidFee1().getId(), 5000);
    subject.computeRemainingAmount(student2UnpaidFee2().getId(), 5000);
    subject.computeUserStatusAfterPayingFee(userWithUnpaidFees);
    User userPaidAllLateFees = userService.getById(userWithUnpaidFees.getId());

    assertEquals(User.Status.ENABLED, userPaidAllLateFees.getStatus());
  }

  public static Fee student2UnpaidFee1() {
    return Fee.builder()
        .id("fee4_id")
        .student(student2())
        .type(TUITION)
        .comment("Comment")
        .remainingAmount(5000)
        .totalAmount(5000)
        .status(LATE)
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .dueDatetime(Instant.parse("2023-02-08T08:30:24.00Z"))
        .updatedAt(Instant.parse("2021-12-09T08:25:25.00Z"))
        .build();
  }

  public static Fee student2UnpaidFee2() {
    return Fee.builder()
        .id("fee5_id")
        .student(student2())
        .type(HARDWARE)
        .comment("Comment")
        .remainingAmount(5000)
        .totalAmount(5000)
        .status(LATE)
        .creationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .dueDatetime(Instant.parse("2023-02-08T08:30:24.00Z"))
        .updatedAt(Instant.parse("2021-12-08T08:25:25.00Z"))
        .build();
  }

  public static User student1() {
    User student1 = new User();
    student1.setId("student1_id");
    student1.setFirstName("Ryan");
    student1.setLastName("Andria");
    student1.setEmail("test+ryan@hei.school");
    student1.setRef("STD21001");
    student1.setStatus(User.Status.ENABLED);
    student1.setSex(M);
    student1.setBirthDate(LocalDate.parse("2000-01-01"));
    student1.setEntranceDatetime(Instant.now());
    student1.setPhone("0123456789");
    student1.setAddress("Example Address");
    return student1;
  }

  public static User student2() {
    User student2 = new User();
    student2.setId("student2_id");
    student2.setFirstName("Two");
    student2.setLastName("Student");
    student2.setEmail("test+student2@hei.school");
    student2.setRef("STD21002");
    student2.setStatus(User.Status.ENABLED);
    student2.setSex(F);
    student2.setBirthDate(LocalDate.parse("2000-01-02"));
    student2.setEntranceDatetime(Instant.now());
    student2.setPhone("0322411124");
    student2.setAddress("Adr 2");
    return student2;
  }

  public static User student3() {
    User student2 = new User();
    student2.setId("student3_id");
    student2.setFirstName("Three");
    student2.setLastName("Student");
    student2.setEmail("test+student3@hei.school");
    student2.setRef("STD21003");
    student2.setStatus(User.Status.ENABLED);
    student2.setSex(F);
    student2.setBirthDate(LocalDate.parse("2000-01-02"));
    student2.setEntranceDatetime(Instant.now());
    student2.setPhone("0322411124");
    student2.setAddress("Adr 2");
    return student2;
  }

  private static ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, PaymentServiceTest.ContextInitializer.SERVER_PORT);
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
