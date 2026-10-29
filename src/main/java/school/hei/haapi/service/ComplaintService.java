package school.hei.haapi.service;

import static school.hei.haapi.model.ComplaintStatus.CREATED;
import static school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
import static school.hei.haapi.model.ComplaintStatus.REJECTED;
import static school.hei.haapi.model.ComplaintStatus.RESOLVED;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.ComplaintEmailNotificationRequested;
import school.hei.haapi.endpoint.event.model.ComplaintSmsNotificationRequested;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintAssignee;
import school.hei.haapi.model.ComplaintStatus;
import school.hei.haapi.model.ComplaintType;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.ComplaintAssigneeRepository;
import school.hei.haapi.repository.ComplaintRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.ComplaintDao;

@Service
@AllArgsConstructor
public class ComplaintService {
  private final ComplaintRepository complaintRepository;
  private final ComplaintAssigneeRepository complaintAssigneeRepository;
  private final UserRepository userRepository;
  private final ComplaintRoutingService complaintRoutingService;
  private final ComplaintStatusHistoryService complaintStatusHistoryService;
  private final ComplaintDao complaintDao;
  private final EventProducer<ComplaintEmailNotificationRequested> emailEventProducer;
  private final EventProducer<ComplaintSmsNotificationRequested> smsEventProducer;

  public Complaint getById(String id) {
    return complaintRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Complaint with id #%s not found".formatted(id)));
  }

  public List<Complaint> getComplaints(
      List<ComplaintType> types,
      List<ComplaintStatus> statuses,
      String managerId,
      String assigneeId,
      Instant from,
      Instant to,
      Pageable pageable) {
    return complaintDao.findByCriteria(types, statuses, managerId, assigneeId, from, to, pageable);
  }

  public List<Complaint> getStudentComplaints(String studentId, Pageable pageable) {
    return complaintRepository.findAllByStudent_IdOrderByCreatedAtDesc(studentId, pageable);
  }

  @Transactional
  public Complaint create(ComplaintType type, String title, String description, User student) {
    var manager = complaintRoutingService.getLast(type).getManager();

    var complaint =
        complaintRepository.save(
            Complaint.builder()
                .type(type)
                .title(title)
                .description(description)
                .student(student)
                .build());

    complaintStatusHistoryService.save(complaint, CREATED, "Réclamation envoyée.", student);

    var savedAssignee =
        complaintAssigneeRepository.save(
            ComplaintAssignee.builder()
                .complaint(complaint)
                .user(manager)
                .assignedBy(null)
                .build());
    addAssigneeInMemory(complaint, savedAssignee);

    emailEventProducer.accept(
        List.of(
            ComplaintEmailNotificationRequested.builder()
                .complaintId(complaint.getId())
                .userId(manager.getId())
                .build()));

    return getById(complaint.getId());
  }

  @Transactional
  public Complaint addAssignees(String complaintId, List<String> userIds, User actor) {
    var complaint = getById(complaintId);

    for (var userId : userIds) {
      if (complaintAssigneeRepository.existsByComplaint_IdAndUser_Id(complaintId, userId)) {
        continue;
      }
      var user =
          userRepository
              .findById(userId)
              .orElseThrow(
                  () -> new NotFoundException("User with id #%s not found".formatted(userId)));
      var savedAssignee =
          complaintAssigneeRepository.save(
              ComplaintAssignee.builder()
                  .complaint(complaint)
                  .user(user)
                  .assignedBy(actor)
                  .build());
      addAssigneeInMemory(complaint, savedAssignee);

      emailEventProducer.accept(
          List.of(
              ComplaintEmailNotificationRequested.builder()
                  .complaintId(complaintId)
                  .userId(userId)
                  .build()));
      smsEventProducer.accept(
          List.of(
              ComplaintSmsNotificationRequested.builder()
                  .complaintId(complaintId)
                  .userId(userId)
                  .build()));
    }

    return getById(complaintId);
  }

  @Transactional
  public Complaint changeStatus(
      String complaintId, ComplaintStatus newStatus, String comment, User actor) {
    var complaint = getById(complaintId);
    assertValidTransition(complaint.getStatus(), newStatus);

    var resolvedComment = resolveComment(newStatus, comment);
    var studentId = complaint.getStudent().getId();

    complaint.setStatus(newStatus);
    complaintRepository.save(complaint);
    complaintStatusHistoryService.save(complaint, newStatus, resolvedComment, actor);

    if (newStatus == IN_PROGRESS) {
      smsEventProducer.accept(
          List.of(
              ComplaintSmsNotificationRequested.builder()
                  .complaintId(complaintId)
                  .userId(studentId)
                  .build()));
    } else if (newStatus == RESOLVED || newStatus == REJECTED) {
      emailEventProducer.accept(
          List.of(
              ComplaintEmailNotificationRequested.builder()
                  .complaintId(complaintId)
                  .userId(studentId)
                  .build()));
      smsEventProducer.accept(
          List.of(
              ComplaintSmsNotificationRequested.builder()
                  .complaintId(complaintId)
                  .userId(studentId)
                  .build()));
    }

    return getById(complaintId);
  }

  private void assertValidTransition(ComplaintStatus from, ComplaintStatus to) {
    var isValid =
        (from == CREATED && to == IN_PROGRESS)
            || (from == IN_PROGRESS && to == RESOLVED)
            || (from == CREATED && to == REJECTED);
    if (!isValid) {
      throw new BadRequestException("Cannot move a complaint from %s to %s".formatted(from, to));
    }
  }

  private String resolveComment(ComplaintStatus newStatus, String comment) {
    if (comment != null && !comment.isBlank()) {
      return comment;
    }
    if (newStatus == REJECTED) {
      throw new BadRequestException("A reason is required to reject a complaint");
    }
    return switch (newStatus) {
      case IN_PROGRESS -> "Votre réclamation est en cours de traitement.";
      case RESOLVED -> "Votre réclamation a été résolue.";
      default ->
          throw new BadRequestException("No default message for status %s".formatted(newStatus));
    };
  }

  private void addAssigneeInMemory(Complaint complaint, ComplaintAssignee assignee) {
    if (complaint.getAssignees() == null) {
      complaint.setAssignees(new ArrayList<>());
    }
    complaint.getAssignees().add(assignee);
  }
}
