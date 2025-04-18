package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.SUSPENDED;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.EL;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.NOT_WORKING;
import static school.hei.haapi.integration.conf.utils.MockObjects.createStudent2;
import static school.hei.haapi.integration.conf.utils.MockObjects.student2;
import static school.hei.haapi.integration.conf.utils.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.utils.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.utils.TestUtils.coordinatesWithNullValues;
import static school.hei.haapi.integration.conf.utils.TestUtils.coordinatesWithValues;
import static school.hei.haapi.integration.conf.utils.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.utils.TestUtils.setUpEventBridge;
import static school.hei.haapi.integration.conf.utils.TestUtils.someCreatableStudent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.utils.TestUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class DirtyStudentIT extends FacadeITMockedThirdParties {
  @MockBean private EventBridgeClient mockEventBridgeClient;

  @BeforeEach
  void setUp() {
    setUpCognito(cognitoComponentMock);
    setUpEventBridge(mockEventBridgeClient);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void manager_update_student_to_suspended() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    List<Student> actual =
        api.createOrUpdateStudents(List.of(createStudent2().status(SUSPENDED)), null);
    Student updated = actual.getFirst();
    List<Student> suspended =
        api.getStudents(1, 10, null, null, null, null, SUSPENDED, null, null, null, null);

    assertTrue(suspended.contains(updated));
    assertEquals(1, actual.size());
  }

  @Test
  void manager_update_student_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);

    CrupdateStudent student2ToUpdate = createStudent2();
    student2ToUpdate.setAddress("updated address");
    student2ToUpdate.setNic("updated nic");
    student2ToUpdate.setBirthPlace("updated birthplace");
    student2ToUpdate.setCoordinates(coordinatesWithValues());
    student2ToUpdate.setSpecializationField(EL);
    student2ToUpdate.setHighSchoolOrigin("Lycée Saint Gabriel Mahajanga");

    Student updatedStudent2 = student2();
    updatedStudent2.setBirthPlace("updated birthplace");
    updatedStudent2.setNic("updated nic");
    updatedStudent2.setSpecializationField(EL);
    updatedStudent2.setAddress("updated address");
    updatedStudent2.setCoordinates(coordinatesWithValues());
    updatedStudent2.setHighSchoolOrigin("Lycée Saint Gabriel Mahajanga");

    Student actualUpdated = api.updateStudent(STUDENT2_ID, student2ToUpdate);

    assertEquals(updatedStudent2, actualUpdated);
  }

  @Test
  void manager_write_suspended_student() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    CrupdateStudent creatableSuspendedStudent =
        new CrupdateStudent()
            .firstName("Suspended")
            .lastName("Two")
            .email("test+suspended2@hei.school")
            .ref("STD29004")
            .status(SUSPENDED)
            .sex(F)
            .birthDate(LocalDate.parse("2000-12-02"))
            .entranceDatetime(Instant.parse("2021-11-09T08:26:24.00Z"))
            .phone("0322411124")
            .address("Adr 3")
            .coordinates(coordinatesWithNullValues());

    List<Student> actual = api.createOrUpdateStudents(List.of(creatableSuspendedStudent), null);
    Student created = actual.get(0);
    List<Student> suspended =
        api.getStudents(1, 10, null, "Suspended", null, null, SUSPENDED, null, null, null, null);

    assertTrue(suspended.contains(created));
    assertEquals(1, actual.size());
  }

  @Test
  void manager_write_update_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    UsersApi api = new UsersApi(manager1Client);
    List<Student> toCreate =
        api.createOrUpdateStudents(List.of(someCreatableStudent(), someCreatableStudent()), null);

    Student created0 = toCreate.getFirst();
    CrupdateStudent toUpdate0 =
        new CrupdateStudent()
            .birthDate(created0.getBirthDate())
            .id(created0.getId())
            .entranceDatetime(created0.getEntranceDatetime())
            .phone(created0.getPhone())
            .nic(created0.getNic())
            .birthPlace(created0.getBirthPlace())
            .email(created0.getEmail())
            .address(created0.getAddress())
            .firstName(created0.getFirstName())
            .lastName(created0.getLastName())
            .sex(created0.getSex())
            .ref(created0.getRef())
            .coordinates(coordinatesWithNullValues())
            .specializationField(created0.getSpecializationField())
            .status(created0.getStatus());
    toUpdate0.setLastName("A new name zero");

    Student created1 = toCreate.get(1);
    CrupdateStudent toUpdate1 =
        new CrupdateStudent()
            .birthDate(created1.getBirthDate())
            .id(created1.getId())
            .entranceDatetime(created1.getEntranceDatetime())
            .phone(created1.getPhone())
            .nic(created1.getNic())
            .birthPlace(created1.getBirthPlace())
            .email(created1.getEmail())
            .address(created1.getAddress())
            .firstName(created1.getFirstName())
            .lastName(created1.getLastName())
            .sex(created1.getSex())
            .ref(created1.getRef())
            .coordinates(coordinatesWithNullValues())
            .specializationField(created1.getSpecializationField())
            .status(created1.getStatus());
    toUpdate1.setLastName("A new name one");

    Student updated0 =
        new Student()
            .birthDate(toUpdate0.getBirthDate())
            .id(toUpdate0.getId())
            .entranceDatetime(toUpdate0.getEntranceDatetime())
            .phone(toUpdate0.getPhone())
            .nic(toUpdate0.getNic())
            .birthPlace(toUpdate0.getBirthPlace())
            .email(toUpdate0.getEmail())
            .address(toUpdate0.getAddress())
            .firstName(toUpdate0.getFirstName())
            .lastName("A new name zero")
            .sex(toUpdate0.getSex())
            .ref(toUpdate0.getRef())
            .coordinates(coordinatesWithNullValues())
            .specializationField(toUpdate0.getSpecializationField())
            .workStudyStatus(NOT_WORKING)
            .status(toUpdate0.getStatus())
            .groups(List.of())
            .isRepeatingYear(false);

    Student updated1 =
        new Student()
            .birthDate(toUpdate1.getBirthDate())
            .id(toUpdate1.getId())
            .entranceDatetime(toUpdate1.getEntranceDatetime())
            .phone(toUpdate1.getPhone())
            .nic(toUpdate1.getNic())
            .birthPlace(toUpdate1.getBirthPlace())
            .email(toUpdate1.getEmail())
            .address(toUpdate1.getAddress())
            .firstName(toUpdate1.getFirstName())
            .lastName("A new name one")
            .sex(toUpdate1.getSex())
            .ref(toUpdate1.getRef())
            .specializationField(toUpdate1.getSpecializationField())
            .coordinates(coordinatesWithNullValues())
            .workStudyStatus(NOT_WORKING)
            .status(toUpdate1.getStatus())
            .groups(List.of())
            .isRepeatingYear(false);

    List<Student> updated = api.createOrUpdateStudents(List.of(toUpdate0, toUpdate1), null);

    assertEquals(2, updated.size());
    assertTrue(updated.contains(updated0));
    assertTrue(updated.contains(updated1));
  }
}
