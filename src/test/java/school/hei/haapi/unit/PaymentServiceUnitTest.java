package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.service.FeeService;
import school.hei.haapi.service.PaymentService;
import school.hei.haapi.service.UserService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaymentServiceUnitTest.ContextInitializer.class)
@AutoConfigureMockMvc
@Transactional
public class PaymentServiceUnitTest extends MockedThirdParties {
  @Autowired private PaymentService paymentService;
  @Autowired private FeeService feeService;
  @Autowired private UserService userService;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  public static Fee associatedFee1() {
    Fee associatedFee = new Fee();
    associatedFee.setId("fee3_id");
    associatedFee.setStudent(student1());
    associatedFee.setType(TUITION);
    associatedFee.setComment("Comment");
    associatedFee.setRemainingAmount(5000);
    associatedFee.setTotalAmount(5000);
    associatedFee.setStatus(LATE);
    associatedFee.setCreationDatetime(Instant.parse("2022-12-08T08:25:24.00Z"));
    associatedFee.setDueDatetime(Instant.parse("2023-02-08T08:30:24.00Z"));
    associatedFee.setUpdatedAt(Instant.parse("2021-12-09T08:25:24.00Z"));
    return associatedFee;
  }

  public static User student1() {
    User student1 = new User();
    student1.setId("student1_id");
    student1.setFirstName("Ryan");
    student1.setLastName("Andria");
    student1.setEmail("test+ryan@hei.school");
    student1.setRef("STD21001");
    student1.setStatus(ENABLED);
    student1.setSex(M);
    student1.setBirthDate(LocalDate.parse("2000-01-01"));
    student1.setEntranceDatetime(Instant.now());
    student1.setPhone("0123456789");
    student1.setAddress("Example Address");
    return student1;
  }

  public static Fee associatedFee2() {
    Fee associatedFee = new Fee();
    associatedFee.setId("fee4_id");
    associatedFee.setStudent(student2());
    associatedFee.setType(TUITION);
    associatedFee.setComment("Comment");
    associatedFee.setRemainingAmount(5000);
    associatedFee.setTotalAmount(5000);
    associatedFee.setStatus(LATE);
    associatedFee.setCreationDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    associatedFee.setDueDatetime(Instant.parse("2023-02-08T08:30:24.00Z"));
    associatedFee.setUpdatedAt(Instant.parse("2021-12-09T08:25:25.00Z"));
    return associatedFee;
  }

  public static User student2() {
    User student2 = new User();
    student2.setId("student2_id");
    student2.setFirstName("Two");
    student2.setLastName("Student");
    student2.setEmail("test+student2@hei.school");
    student2.setRef("STD21002");
    student2.setStatus(ENABLED);
    student2.setSex(F);
    student2.setBirthDate(LocalDate.parse("2000-01-02"));
    student2.setEntranceDatetime(Instant.now());
    student2.setPhone("0322411124");
    student2.setAddress("Adr 2");
    return student2;
  }

  @Test
  void compute_remaining_amount_ok() {
    Fee associatedFee = associatedFee1();
    User student1 = student1();

    paymentService.computeRemainingAmount(associatedFee.getId(), 5000);

    Fee updatedFee = feeService.getById(associatedFee.getId());
    User updatedStudent = userService.findById(student1.getId());

    assertEquals(0, updatedFee.getRemainingAmount());
    assertEquals(PAID, updatedFee.getStatus());
    assertEquals(ENABLED, updatedStudent.getStatus());
  }

  static class ContextInitializer extends AbstractContextInitializer {
    public static final int SERVER_PORT = anAvailableRandomPort();

    @Override
    public int getServerPort() {
      return SERVER_PORT;
    }
  }
}
