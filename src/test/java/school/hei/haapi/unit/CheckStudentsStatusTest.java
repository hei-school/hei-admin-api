package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.conf.TestUtils.FEE4_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE5_ID;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
import school.hei.haapi.repository.UserRepository;
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
  @Autowired private UserRepository userRepository;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
  }

  private static User student1() {
    User student1 = new User();
    student1.setId("student1_id");
    student1.setFirstName("Ryan");
    student1.setLastName("Andria");
    student1.setEmail("test+ryan@hei.school");
    student1.setRef("STD21001");
    student1.setStatus(ENABLED);
    student1.setSex(M);
    student1.setBirthDate(LocalDate.parse("2000-01-01"));
    student1.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student1.setPhone("0322411123");
    student1.setAddress("Adr 1");
    student1.setLatitude(-123.123);
    student1.setLongitude(123.0);
    student1.setHighSchoolOrigin("Lycée Andohalo");
    return student1;
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
    student2.setEntranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"));
    student2.setPhone("0322411124");
    student2.setAddress("Adr 2");
    student2.setLatitude(255.255);
    student2.setLongitude(-255.255);
    student2.setHighSchoolOrigin("Lycée Andohalo");
    return student2;
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

  @Test
  void get_all_students_with_unpaid_or_late_fee_ok() {
    List<User> studentsWithUnpaidOrLateFee = userRepository.getStudentsWithUnpaidOrLateFee();

    assertEquals(2, studentsWithUnpaidOrLateFee.size());
    assertTrue(studentsWithUnpaidOrLateFee.contains(student1()));
    assertTrue(studentsWithUnpaidOrLateFee.contains(student2()));
  }
}
