package school.hei.haapi.repository.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.ComplaintStatus.RESOLVED;
import static school.hei.haapi.model.User.Role.MANAGER;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintRoutingService;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.befiana.BefianaClient;

class ComplaintStatusHistoryDaoTest extends FacadeITMockedThirdParties {
  @Autowired private ComplaintStatusHistoryDao subject;
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
  void filters_by_complaint_id_ok() {
    var student = userRepository.save(someStudent("history-student-1"));
    var manager = userRepository.save(someUser("history-manager-1", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaintA =
        complaintService.create(ComplaintType.ACADEMIC, "Titre A", "Description", student);
    var complaintB =
        complaintService.create(ComplaintType.ACADEMIC, "Titre B", "Description", student);

    var found = subject.findByCriteria(complaintA.getId(), null, null, Pageable.unpaged());

    assertTrue(found.stream().allMatch(h -> h.getComplaint().getId().equals(complaintA.getId())));
    assertTrue(found.stream().noneMatch(h -> h.getComplaint().getId().equals(complaintB.getId())));
  }

  @Test
  @Transactional
  void filters_by_date_range_ok() {
    var student = userRepository.save(someStudent("history-student-2"));
    var manager = userRepository.save(someUser("history-manager-2", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    backdateCreatedHistoryEntry(complaint.getId(), Instant.now().minus(30, ChronoUnit.DAYS));
    complaintService.changeStatus(complaint.getId(), IN_PROGRESS, null, manager);

    var foundFromYesterday =
        subject.findByCriteria(
            complaint.getId(), Instant.now().minus(1, ChronoUnit.DAYS), null, Pageable.unpaged());
    var foundUpToYesterday =
        subject.findByCriteria(
            complaint.getId(), null, Instant.now().minus(1, ChronoUnit.DAYS), Pageable.unpaged());

    assertTrue(foundFromYesterday.stream().anyMatch(h -> h.getStatus() == IN_PROGRESS));
    assertTrue(foundUpToYesterday.stream().noneMatch(h -> h.getStatus() == IN_PROGRESS));
  }

  @Test
  @Transactional
  void respects_pagination_ok() {
    var student = userRepository.save(someStudent("history-student-3"));
    var manager = userRepository.save(someUser("history-manager-3", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.changeStatus(complaint.getId(), IN_PROGRESS, null, manager);
    complaintService.changeStatus(complaint.getId(), RESOLVED, null, manager);

    var firstPage = subject.findByCriteria(complaint.getId(), null, null, PageRequest.of(0, 1));

    assertEquals(1, firstPage.size());
  }

  private void backdateCreatedHistoryEntry(String complaintId, Instant datetime) {
    entityManager
        .createNativeQuery(
            "update complaint_status_history set datetime = ?1 where complaint_id = ?2 and"
                + " status = 'CREATED'")
        .setParameter(1, datetime)
        .setParameter(2, complaintId)
        .executeUpdate();
  }
}
