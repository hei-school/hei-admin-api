package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.ComplaintStatus.REJECTED;
import static school.hei.haapi.model.ComplaintStatus.RESOLVED;
import static school.hei.haapi.model.User.Role.MANAGER;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintRoutingService;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.event.ComplaintEmailNotificationRequestedService;

class ComplaintEmailNotificationRequestedServiceTest extends FacadeITMockedThirdParties {
  @Autowired private ComplaintEmailNotificationRequestedService subject;
  @Autowired private UserRepository userRepository;
  @Autowired private ComplaintRoutingService complaintRoutingService;
  @Autowired private ComplaintService complaintService;
  @MockBean private Mailer mailerMock;
  @MockBean private BefianaClient befianaClientMock;
  @MockBean private EventProducer<ComplaintEmailNotificationRequested> emailEventProducerMock;
  @MockBean private EventProducer<ComplaintSmsNotificationRequested> smsEventProducerMock;

  @Test
  @Transactional
  void notifies_auto_routed_manager_ok() {
    var student = userRepository.save(someStudent("email-student-1"));
    var manager = userRepository.save(someUser("email-manager-1", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);

    subject.accept(
        ComplaintEmailNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId(manager.getId())
            .build());

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailerMock, times(1)).accept(emailCaptor.capture());
    assertEquals("[Réclamation] Nouvelle réclamation à traiter", emailCaptor.getValue().subject());
  }

  @Test
  @Transactional
  void notifies_manually_assigned_manager_ok() {
    var student = userRepository.save(someStudent("email-student-2"));
    var manager = userRepository.save(someUser("email-manager-2", MANAGER));
    var otherManager = userRepository.save(someUser("email-manager-3", MANAGER));
    complaintRoutingService.create(ComplaintType.FINANCIAL, manager);
    var complaint =
        complaintService.create(ComplaintType.FINANCIAL, "Titre", "Description", student);
    complaintService.addAssignees(complaint.getId(), List.of(otherManager.getId()), manager);

    subject.accept(
        ComplaintEmailNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId(otherManager.getId())
            .build());

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailerMock, times(1)).accept(emailCaptor.capture());
    assertEquals(
        "[Réclamation] Une réclamation vous a été transmise", emailCaptor.getValue().subject());
  }

  @Test
  @Transactional
  void notifies_student_on_rejection_with_comment_ok() {
    var student = userRepository.save(someStudent("email-student-3"));
    var manager = userRepository.save(someUser("email-manager-4", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.changeStatus(complaint.getId(), REJECTED, "Non recevable", manager);

    subject.accept(
        ComplaintEmailNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId(student.getId())
            .build());

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailerMock, times(1)).accept(emailCaptor.capture());
    assertEquals("[Réclamation] Votre réclamation a été rejetée", emailCaptor.getValue().subject());
  }

  @Test
  @Transactional
  void notifies_student_on_resolution_ok() {
    var student = userRepository.save(someStudent("email-student-4"));
    var manager = userRepository.save(someUser("email-manager-5", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.changeStatus(complaint.getId(), IN_PROGRESS, null, manager);
    complaintService.changeStatus(complaint.getId(), RESOLVED, null, manager);

    subject.accept(
        ComplaintEmailNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId(student.getId())
            .build());

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailerMock, times(1)).accept(emailCaptor.capture());
    assertEquals("[Réclamation] Votre réclamation a été résolue", emailCaptor.getValue().subject());
  }

  @Test
  @Transactional
  void throws_when_recipient_not_found_ok() {
    var student = userRepository.save(someStudent("email-student-5"));
    var manager = userRepository.save(someUser("email-manager-6", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    var event =
        ComplaintEmailNotificationRequested.builder()
            .complaintId(complaint.getId())
            .userId("unknown-user-id")
            .build();

    assertThrows(NotFoundException.class, () -> subject.accept(event));
  }
}
