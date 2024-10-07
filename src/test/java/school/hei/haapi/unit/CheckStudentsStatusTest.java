package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE4_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE5_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;

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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.model.User;
import school.hei.haapi.service.PaymentService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.UpdateStudentsStatusService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaymentServiceUnitTest.ContextInitializer.class)
@AutoConfigureMockMvc
@Transactional
public class CheckStudentsStatusTest extends MockedThirdParties {
  @Autowired private UpdateStudentsStatusService updateStudentsStatusService;
  @Autowired private PaymentService paymentService;
  @Autowired private UserService userService;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  private static User student2() {
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

  private static User student3() {
    User student3 = new User();
    student3.setId("student3_id");
    student3.setFirstName("Three");
    student3.setLastName("Student");
    student3.setEmail("test+student3@hei.school");
    student3.setRef("STD21003");
    student3.setStatus(ENABLED);
    student3.setSex(F);
    student3.setBirthDate(LocalDate.parse("2000-01-02"));
    student3.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student3.setPhone("0322411124");
    student3.setAddress("Adr 2");
    return student3;
  }

  @Test
  @DirtiesContext
  void update_students_status_ok() {
    User student2 = student2();
    assertEquals(ENABLED, student2.getStatus());

    // here, we check if the enabled student has paid all their fees
    updateStudentsStatusService.suspendStudentsWithUnpaidOrLateFee();
    User suspendedStudent2 = userService.findById(student2.getId());
    assertEquals(SUSPENDED, suspendedStudent2.getStatus());

    paymentService.computeRemainingAmount(FEE4_ID, 5000);
    paymentService.computeRemainingAmount(FEE5_ID, 5000);

    // here, we check if the suspended student has paid all their fees
    updateStudentsStatusService.checkSuspendedStudentsStatus();
    User suspendedStudentChecked = userService.findById(student2.getId());
    assertEquals(ENABLED, suspendedStudentChecked.getStatus());
  }
}
