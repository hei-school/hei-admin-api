package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.integration.conf.FakeDataProvider.someLateFee;
import static school.hei.haapi.integration.conf.FakeDataProvider.someMpbs;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognitoAndCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.FakeDataFactory.SomeUserFactory;
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

  private User student;
  private User studentSuspend;
  private Fee feeSuspendStudent;

  @BeforeEach
  void setUp() {
    setUpCognitoAndCasdoor(casdoorAuthServiceMock, cognitoComponentMock, certificateLoaderMock);
    setUpS3Service(fileService, TestUtils.student1());
    setUpEventBridge(eventBridgeClientMock);

    student = userRepository.save(new SomeUserFactory().build());
    studentSuspend =
        userRepository.save(new SomeUserFactory().status(User.Status.SUSPENDED).build());
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
  void compute_user_status_after_paying_fee_ok() {
    var userWithUnpaidFees = studentSuspend;
    var userWithoutUnpaidFees = student;

    subject.computeUserStatusAfterPayingFee(userWithUnpaidFees);
    subject.computeUserStatusAfterPayingFee(userWithoutUnpaidFees);
    User updatedUserWithUnpaidFees = userService.getById(userWithUnpaidFees.getId());
    User updatedUserWithoutUnpaidFees = userService.getById(userWithoutUnpaidFees.getId());

    assertEquals(User.Status.SUSPENDED, updatedUserWithUnpaidFees.getStatus());
    assertEquals(User.Status.ENABLED, updatedUserWithoutUnpaidFees.getStatus());

    // here student2 has paid all their fees late
    subject.computeRemainingAmount(
        feeSuspendStudent.getId(), feeSuspendStudent.getRemainingAmount());
    subject.computeUserStatusAfterPayingFee(userWithUnpaidFees);
    User userPaidAllLateFees = userService.getById(userWithUnpaidFees.getId());

    assertEquals(User.Status.ENABLED, userPaidAllLateFees.getStatus());
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
