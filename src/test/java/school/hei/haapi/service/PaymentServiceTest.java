package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.integration.MpbsIT.creatableMpbsFrom;
import static school.hei.haapi.integration.StudentIT.someCreatableStudent;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.creatableFee1;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = PaymentServiceTest.ContextInitializer.class)
@AutoConfigureMockMvc
class PaymentServiceTest extends MockedThirdParties {
  private static final Logger log = LoggerFactory.getLogger(PaymentServiceTest.class);
  @Autowired private PaymentService subject;
  @Autowired private MpbsService mpbsService;
  @MockBean private EventBridgeClient eventBridgeClientMock;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
    setUpEventBridge(eventBridgeClientMock);
  }

  @Test
  void user_status_is_computed_after_paying_fee_by_mpbs() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi usersApi = new UsersApi(manager1Client);
    PayingApi payingApi = new PayingApi(manager1Client);
    /*
     * STEP 1 - BEGIN
     * Create test student,
     * assert student is enabled
     * create a fee for the student
     *  */
    var studentToCreate = someCreatableStudent();
    var createdStudent = usersApi.createOrUpdateStudents(List.of(studentToCreate)).getFirst();
    assertEquals(ENABLED, createdStudent.getStatus());
    String currentStudentId = createdStudent.getId();

    var createdFee =
        payingApi.createStudentFees(currentStudentId, List.of(creatableFee1())).getFirst();
    var createdMpbs =
        payingApi.createMpbs(
            createdFee.getStudentId(),
            createdFee.getId(),
            creatableMpbsFrom(
                createdFee.getStudentId(), createdFee.getId(), /*what is psp test?*/ "psp_test"));
    /*
     * STEP 1 - END
     *  */

    /*
     * STEP 2 - BEGIN
     * Suspend previously created student
     */
    log.info("createdfee = {}", createdFee);
    log.info("created mpbs = {}", createdMpbs);
    studentToCreate.setId(currentStudentId);
    studentToCreate.setStatus(SUSPENDED);

    var suspendedCreatedStudent =
        usersApi.createOrUpdateStudents(List.of(studentToCreate)).getFirst();

    assertEquals(SUSPENDED, suspendedCreatedStudent.getStatus());
    /*
     * STEP 2 - END
     */
    /*
     * STEP 3 - BEGIN
     * Pay created fee, expect it to update previously created student status to ENABLED
     */
    var domainCreatedMpbs = mpbsService.getByPspId(createdMpbs.getPspId());
    var createdPayment = subject.savePaymentsViaMpbs(domainCreatedMpbs, 5000);
    log.info("created payment = {}", createdPayment);

    var actualStudent = usersApi.getStudentById(currentStudentId);
    assertEquals(ENABLED, actualStudent.getStatus());
    /*
     * STEP 3 - END
     */
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
