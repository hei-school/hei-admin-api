package school.hei.haapi.repository.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.FakeDataProvider.someUser;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.User.Role.MANAGER;
import static school.hei.haapi.model.User.Role.STAFF_MEMBER;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
import school.hei.haapi.model.ComplaintStatus;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.ComplaintRoutingService;
import school.hei.haapi.service.ComplaintService;
import school.hei.haapi.service.befiana.BefianaClient;

class ComplaintDaoTest extends FacadeITMockedThirdParties {
  @Autowired private ComplaintDao subject;
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
  void filters_by_type_ok() {
    var student = userRepository.save(someStudent("dao-student-1"));
    var manager = userRepository.save(someUser("dao-manager-1", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    complaintRoutingService.create(ComplaintType.FINANCIAL, manager);
    var academic =
        complaintService.create(ComplaintType.ACADEMIC, "Titre academique", "Description", student);
    var financial =
        complaintService.create(ComplaintType.FINANCIAL, "Titre financier", "Description", student);

    var found =
        subject.findByCriteria(
            List.of(ComplaintType.ACADEMIC), null, null, null, null, null, Pageable.unpaged());

    assertTrue(found.stream().anyMatch(c -> c.getId().equals(academic.getId())));
    assertTrue(found.stream().noneMatch(c -> c.getId().equals(financial.getId())));
  }

  @Test
  @Transactional
  void filters_by_status_ok() {
    var student = userRepository.save(someStudent("dao-student-2"));
    var manager = userRepository.save(someUser("dao-manager-2", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var created = complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    var inProgress =
        complaintService.create(ComplaintType.ACADEMIC, "Titre 2", "Description", student);
    complaintService.changeStatus(inProgress.getId(), IN_PROGRESS, null, manager);

    var found =
        subject.findByCriteria(
            null, List.of(ComplaintStatus.IN_PROGRESS), null, null, null, null, Pageable.unpaged());

    assertTrue(found.stream().anyMatch(c -> c.getId().equals(inProgress.getId())));
    assertTrue(found.stream().noneMatch(c -> c.getId().equals(created.getId())));
  }

  @Test
  @Transactional
  void filters_by_routed_manager_id_but_not_manually_assigned_staff_ok() {
    var student = userRepository.save(someStudent("dao-student-3"));
    var manager = userRepository.save(someUser("dao-manager-3", MANAGER));
    var staff = userRepository.save(someUser("dao-staff-3", STAFF_MEMBER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.addAssignees(complaint.getId(), List.of(staff.getId()), manager);

    var foundByManager =
        subject.findByCriteria(null, null, manager.getId(), null, null, null, Pageable.unpaged());
    var foundByStaff =
        subject.findByCriteria(null, null, staff.getId(), null, null, null, Pageable.unpaged());

    assertTrue(foundByManager.stream().anyMatch(c -> c.getId().equals(complaint.getId())));
    assertTrue(foundByStaff.stream().noneMatch(c -> c.getId().equals(complaint.getId())));
  }

  @Test
  @Transactional
  void filters_by_assignee_id_regardless_of_assigner_ok() {
    var student = userRepository.save(someStudent("dao-student-4"));
    var manager = userRepository.save(someUser("dao-manager-4", MANAGER));
    var staff = userRepository.save(someUser("dao-staff-4", STAFF_MEMBER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var complaint =
        complaintService.create(ComplaintType.ACADEMIC, "Titre", "Description", student);
    complaintService.addAssignees(complaint.getId(), List.of(staff.getId()), manager);

    var foundByStaffAsAssignee =
        subject.findByCriteria(null, null, null, staff.getId(), null, null, Pageable.unpaged());

    assertTrue(foundByStaffAsAssignee.stream().anyMatch(c -> c.getId().equals(complaint.getId())));
  }

  @Test
  @Transactional
  void filters_by_date_range_ok() {
    var student = userRepository.save(someStudent("dao-student-5"));
    var manager = userRepository.save(someUser("dao-manager-5", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    var recent =
        complaintService.create(ComplaintType.ACADEMIC, "Titre récent", "Description", student);
    var old =
        complaintService.create(ComplaintType.ACADEMIC, "Titre ancien", "Description", student);
    backdateCreatedAt(old.getId(), Instant.now().minus(30, ChronoUnit.DAYS));

    var found =
        subject.findByCriteria(
            null,
            null,
            null,
            null,
            Instant.now().minus(1, ChronoUnit.DAYS),
            null,
            Pageable.unpaged());

    assertTrue(found.stream().anyMatch(c -> c.getId().equals(recent.getId())));
    assertTrue(found.stream().noneMatch(c -> c.getId().equals(old.getId())));
  }

  @Test
  @Transactional
  void respects_pagination_ok() {
    var student = userRepository.save(someStudent("dao-student-6"));
    var manager = userRepository.save(someUser("dao-manager-6", MANAGER));
    complaintRoutingService.create(ComplaintType.ACADEMIC, manager);
    complaintService.create(ComplaintType.ACADEMIC, "Titre 1", "Description", student);
    complaintService.create(ComplaintType.ACADEMIC, "Titre 2", "Description", student);

    var firstPage =
        subject.findByCriteria(null, null, null, null, null, null, PageRequest.of(0, 1));

    assertEquals(1, firstPage.size());
  }

  private void backdateCreatedAt(String complaintId, Instant datetime) {
    entityManager
        .createNativeQuery("update complaint set created_at = ?1 where id = ?2")
        .setParameter(1, datetime)
        .setParameter(2, complaintId)
        .executeUpdate();
  }
}
