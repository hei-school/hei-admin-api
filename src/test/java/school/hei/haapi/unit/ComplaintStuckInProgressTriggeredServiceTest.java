package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.User.Role.MANAGER;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintStuckInProgressTriggered;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.repository.ComplaintRepository;
import school.hei.haapi.service.ComplaintRoutingService;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.befiana.BefianaClient;
import school.hei.haapi.service.event.ComplaintStuckInProgressTriggeredService;

class ComplaintStuckInProgressTriggeredServiceTest extends FacadeITMockedThirdParties {
  @Autowired private ComplaintStuckInProgressTriggeredService subject;
  @Autowired private school.hei.haapi.repository.UserRepository userRepository;
  @Autowired private ComplaintRoutingService complaintRoutingService;
  @Autowired private ComplaintService complaintService;
  @Autowired private ComplaintRepository complaintRepository;
  @PersistenceContext private EntityManager entityManager;
  @MockBean private Mailer mailerMock;
  @MockBean private BefianaClient befianaClientMock;
  @MockBean private EventProducer<ComplaintEmailNotificationRequested> emailEventProducerMock;
  @MockBean private EventProducer<ComplaintSmsNotificationRequested> smsEventProducerMock;

  @Test
  @Transactional
  void reminds_student_for_complaint_stuck_in_progress_ok() {
    var student = someStudent("stuck-student");
    student.setPhone("+261340000000");
    student = userRepository.save(student);
    var manager = userRepository.save(someUser("stuck-manager", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.changeStatus(complaint.getId(), IN_PROGRESS, null, manager);
    backdateLastInProgressHistoryEntry(complaint.getId(), Instant.now().minus(4, ChronoUnit.DAYS));

    subject.accept(new ComplaintStuckInProgressTriggered());

    verify(mailerMock, times(1)).accept(any());
    verify(befianaClientMock, times(1)).sendSingle(anyString(), anyString(), anyString());
    var reminded = complaintRepository.findById(complaint.getId()).orElseThrow();
    assertNotNull(reminded.getReminderSentAt());
  }

  @Test
  void does_not_remind_recently_in_progress_complaint_ok() {
    var student = userRepository.save(someStudent("fresh-student"));
    var manager = userRepository.save(someUser("fresh-manager", MANAGER));
    complaintRoutingService.create(ComplaintType.FINANCIAL, manager);
    var complaint =
        complaintService.create(ComplaintType.FINANCIAL, "Titre", "Description", student);
    complaintService.changeStatus(complaint.getId(), IN_PROGRESS, null, manager);

    subject.accept(new ComplaintStuckInProgressTriggered());

    verify(mailerMock, never()).accept(any());
    verify(befianaClientMock, never()).sendSingle(anyString(), anyString(), anyString());
  }

  private void backdateLastInProgressHistoryEntry(String complaintId, Instant datetime) {
    entityManager
        .createNativeQuery(
            "update complaint_status_history set datetime = ?1 where complaint_id = ?2 and"
                + " status = 'IN_PROGRESS'")
        .setParameter(1, datetime)
        .setParameter(2, complaintId)
        .executeUpdate();
  }
}
