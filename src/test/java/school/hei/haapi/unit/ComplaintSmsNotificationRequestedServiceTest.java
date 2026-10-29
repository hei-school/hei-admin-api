package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.User.Role.MANAGER;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintRoutingService;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.befiana.BefianaException;
import school.hei.haapi.service.event.ComplaintSmsNotificationRequestedService;

class ComplaintSmsNotificationRequestedServiceTest extends FacadeITMockedThirdParties {
  @Autowired private ComplaintSmsNotificationRequestedService subject;
  @Autowired private UserRepository userRepository;
  @Autowired private ComplaintRoutingService complaintRoutingService;
  @Autowired private ComplaintService complaintService;
  @PersistenceContext private EntityManager entityManager;
  @MockBean private Mailer mailerMock;
  @MockBean private BefianaClient befianaClientMock;
  @MockBean private EventProducer<ComplaintEmailNotificationRequested> emailEventProducerMock;
  @MockBean private EventProducer<ComplaintSmsNotificationRequested> smsEventProducerMock;

  @Test
  @Transactional
  void sends_last_status_comment_to_student_ok() {
    var student = userRepository.save(someStudent("sms-student-1"));
    var manager = userRepository.save(someUser("sms-manager-1", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.changeStatus(complaint.getId(), IN_PROGRESS, null, manager);
    entityManager.clear();

    subject.accept(
        ComplaintSmsNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId(student.getId())
            .build());

    verify(befianaClientMock, times(1))
        .sendSingle(
            eq(student.getPhone()),
            eq("Votre réclamation est en cours de traitement."),
            anyString());
  }

  @Test
  @Transactional
  void sends_generic_text_to_non_student_recipient_ok() {
    var student = userRepository.save(someStudent("sms-student-2"));
    var manager = userRepository.save(someUser("sms-manager-2", MANAGER));
    complaintRoutingService.create(ComplaintType.FINANCIAL, manager);
    var complaint =
        complaintService.create(ComplaintType.FINANCIAL, "Titre", "Description", student);

    subject.accept(
        ComplaintSmsNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId(manager.getId())
            .build());

    verify(befianaClientMock, times(1))
        .sendSingle(
            eq(manager.getPhone()),
            eq("Une réclamation vous a été transmise sur HEI Admin."),
            anyString());
  }

  @Test
  @Transactional
  void swallows_befiana_exception_ok() {
    var student = userRepository.save(someStudent("sms-student-3"));
    var manager = userRepository.save(someUser("sms-manager-3", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    when(befianaClientMock.sendSingle(any(), any(), any()))
        .thenThrow(new BefianaException("boom", 500, new RuntimeException()));

    assertDoesNotThrow(
        () ->
            subject.accept(
                ComplaintSmsNotificationRequested.builder()
                    .complaintId(complaint.getId())
                    .userId(manager.getId())
                    .build()));
  }

  @Test
  @Transactional
  void throws_when_recipient_not_found_ok() {
    var student = userRepository.save(someStudent("sms-student-4"));
    var manager = userRepository.save(someUser("sms-manager-4", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);

    assertThrows(
        NotFoundException.class,
        () ->
            subject.accept(
                ComplaintSmsNotificationRequested.builder()
                    .complaintId(complaint.getId())
                    .userId("unknown-user-id")
                    .build()));
  }
}
