package school.hei.haapi.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.service.aws.S3Service;
import school.hei.haapi.service.event.S3Conf;

class HealthControllerIT extends FacadeIT {

  @Autowired HealthController healthController;

  @MockBean
  private S3Service s3Service;

  @MockBean private S3Conf s3Conf;

  private Student student1() {
    Student student = new Student();
    student.setId("student1_id");
    student.setFirstName("Ryan");
    student.setLastName("Andria");
    student.setEmail("test+ryan@hei.school");
    student.setRef("STD21001");
    student.setPhone("0322411123");
    student.setStatus(EnableStatus.ENABLED);
    student.setSex(Sex.M);
    student.setBirthDate(LocalDate.parse("2000-01-01"));
    student.setEntranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"));
    student.setAddress("Adr 1");
    student.setNic("");
    student.setSpecializationField(COMMON_CORE);
    student.setBirthPlace("");
    return student;
  }

  @BeforeEach
  public void setUp() throws ApiException {
    setUpS3Service(s3Service, student1());
  }

  @Test
  void ping() {
    assertEquals("pong", healthController.ping());
  }

  @Test
  void can_read_from_dummy_table() {
    var dummyTableEntries = healthController.dummyTable();
    assertEquals(1, dummyTableEntries.size());
    assertEquals("dummy-table-id-1", dummyTableEntries.get(0).getId());
  }
}
