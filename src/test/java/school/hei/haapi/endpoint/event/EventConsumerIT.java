package school.hei.haapi.endpoint.event;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.event.gen.UuidCreated;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.repository.DummyUuidRepository;
import school.hei.haapi.service.aws.S3Service;
import school.hei.haapi.service.event.S3Conf;

class EventConsumerIT extends FacadeIT {

  @Autowired EventConsumer subject;
  @Autowired DummyUuidRepository dummyUuidRepository;
  @MockBean private S3Service s3Service;

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
  void setUp() {
    setUpS3Service(s3Service, student1());
  }

  @Test
  void uuid_created_is_persisted() throws InterruptedException, JsonProcessingException {
    var uuid = randomUUID().toString();
    var uuidCreated = UuidCreated.builder().uuid(uuid).build();
    var om = new ObjectMapper();
    var payloadReceived = om.readValue(om.writeValueAsString(uuidCreated), UuidCreated.class);

    subject.accept(
        List.of(
            new EventConsumer.AcknowledgeableTypedEvent(
                new EventConsumer.TypedEvent(
                    "school.hei.haapi.endpoint.event.gen.UuidCreated", payloadReceived),
                () -> {})));

    Thread.sleep(2_000);
    var saved = dummyUuidRepository.findById(uuid).orElseThrow();
    assertEquals(uuid, saved.getId());
  }
}
