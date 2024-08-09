package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.ManagerIT.manager1;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.conf.TestUtils.teacher1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Payment;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class PaginationIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient eventBridgeClientMock;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static Payment payment2() {
    return new Payment()
        .id("payment2_id")
        .feeId(FEE1_ID)
        .type(Payment.TypeEnum.CASH)
        .amount(3000)
        .comment("Comment")
        .creationDatetime(Instant.parse("2022-11-09T08:25:25.00Z"));
  }

  @BeforeEach
  public void setUp() throws ApiException {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(eventBridgeClientMock);
    setUpS3Service(fileService, student1());
    setUpS3Service(fileService, manager1());
    setUpS3Service(fileService, teacher1());
  }

  private void someCreatableStudentList(int nbOfNewStudents) throws ApiException {
    List<CrupdateStudent> newStudents = new ArrayList<>();
    for (int i = 0; i < nbOfNewStudents; i++) {
      newStudents.add(StudentIT.someCreatableStudent());
    }
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    api.createOrUpdateStudents(newStudents);
  }

  @Test
  void student_pages_are_ordered_by_reference() throws ApiException {
    // TODO: fix why the pagination doesn't work properly
    someCreatableStudentList(7);
    int pageSize = 4;
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    UsersApi api = new UsersApi(teacher1Client);

    final List<Student> page1 =
        api.getStudents(2, pageSize, null, null, null, null, null, null, null, null, null);
    final List<Student> page2 =
        api.getStudents(3, pageSize, null, null, null, null, null, null, null, null, null);
    final List<Student> page3 =
        api.getStudents(4, pageSize, null, null, null, null, null, null, null, null, null);
    final List<Student> page4 =
        api.getStudents(5, pageSize, null, null, null, null, null, null, null, null, null);
    final List<Student> page100 =
        api.getStudents(100, pageSize, null, null, null, null, null, null, null, null, null);

    assertEquals(pageSize, page1.size());
    assertEquals(pageSize, page2.size());
    assertEquals(4, page3.size());
    assertEquals(1, page4.size());
    assertEquals(0, page100.size());
    // students are ordered by ref
    assertTrue(isBefore(page1.getFirst().getRef(), page1.get(2).getRef()));
    assertTrue(isBefore(page1.get(2).getRef(), page2.get(0).getRef()));
    assertTrue(isBefore(page2.getFirst().getRef(), page2.get(2).getRef()));
  }

  @Test
  void fees_pages_are_ordered_by_due_datetime_desc() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);
    int pageSize = 2;

    List<Fee> page1 = api.getStudentFees(STUDENT1_ID, 1, pageSize, null);
    List<Fee> page2 = api.getStudentFees(STUDENT1_ID, 2, pageSize, null);
    List<Fee> page3 = api.getStudentFees(STUDENT1_ID, 3, pageSize, null);

    assertEquals(pageSize, page1.size());
    assertEquals(2, page2.size());
    assertEquals(2, page3.size());
    assertTrue(isAfter(page1.getFirst().getDueDatetime(), page1.get(1).getDueDatetime()));
    assertTrue(isAfter(page1.getFirst().getDueDatetime(), page2.getFirst().getDueDatetime()));
  }

  @Test
  void payments_pages_are_ordered_by_due_datetime_desc() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    int pageSize = 2;

    List<Payment> page1 = api.getStudentPayments(STUDENT1_ID, FEE1_ID, 1, pageSize);
    List<Payment> page2 = api.getStudentPayments(STUDENT1_ID, FEE1_ID, 2, pageSize);
    List<Payment> page3 = api.getStudentPayments(STUDENT1_ID, FEE1_ID, 3, pageSize);

    assertEquals(pageSize, page1.size());
    assertEquals(1, page2.size());
    assertEquals(0, page3.size());
    assertTrue(isAfter(page1.getFirst().getCreationDatetime(), page1.get(1).getCreationDatetime()));
    assertTrue(isAfter(page1.get(1).getCreationDatetime(), page2.getFirst().getCreationDatetime()));
  }

  private boolean isBefore(String a, String b) {
    return a.compareTo(b) < 0;
  }

  private boolean isAfter(Instant a, Instant b) {
    return a.compareTo(b) > 0;
  }

  @Test
  void page_parameters_are_validated() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);

    UsersApi api = new UsersApi(teacher1Client);
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page value must be >= 1\"}",
        () -> api.getStudents(0, 20, null, null, null, null, null, null, null, null, null));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"page size must be <500\"}",
        () -> api.getStudents(1, 1000, null, null, null, null, null, null, null, null, null));
  }
}
