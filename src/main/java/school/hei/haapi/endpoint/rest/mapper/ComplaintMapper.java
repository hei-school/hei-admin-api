package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Complaint;
import school.hei.haapi.endpoint.rest.model.ComplaintStatus;
import school.hei.haapi.endpoint.rest.model.ComplaintStatusHistory;
import school.hei.haapi.endpoint.rest.model.ComplaintType;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;

@Component
@AllArgsConstructor
public class ComplaintMapper {
  private final UserMapper userMapper;

  public Complaint toRest(school.hei.haapi.model.Complaint complaint) {
    return new Complaint()
        .id(complaint.getId())
        .type(toRestType(complaint.getType()))
        .title(complaint.getTitle())
        .description(complaint.getDescription())
        .status(toRestStatus(complaint.getStatus()))
        .student(userMapper.toIdentifier(complaint.getStudent()))
        .assignees(toRestAssignees(complaint))
        .createdAt(complaint.getCreatedAt());
  }

  public List<Complaint> toRest(List<school.hei.haapi.model.Complaint> complaints) {
    return complaints.stream().map(this::toRest).toList();
  }

  public ComplaintStatusHistory toRest(school.hei.haapi.model.ComplaintStatusHistory history) {
    return new ComplaintStatusHistory()
        .status(toRestStatus(history.getStatus()))
        .comment(history.getComment())
        .changedBy(
            history.getChangedBy() != null ? userMapper.toIdentifier(history.getChangedBy()) : null)
        .datetime(history.getDatetime());
  }

  public List<ComplaintStatusHistory> toRestHistory(
      List<school.hei.haapi.model.ComplaintStatusHistory> histories) {
    return histories.stream().map(this::toRest).toList();
  }

  private List<UserIdentifier> toRestAssignees(school.hei.haapi.model.Complaint complaint) {
    if (complaint.getAssignees() == null) {
      return List.of();
    }
    return complaint.getAssignees().stream()
        .map(assignee -> userMapper.toIdentifier(assignee.getUser()))
        .toList();
  }

  public ComplaintType toRestType(school.hei.haapi.model.ComplaintType domain) {
    if (domain == null) return null;
    return switch (domain) {
      case ACADEMIC -> ComplaintType.ACADEMIC;
      case FINANCIAL -> ComplaintType.FINANCIAL;
    };
  }

  public school.hei.haapi.model.ComplaintType toDomainType(ComplaintType rest) {
    if (rest == null) return null;
    return switch (rest) {
      case ACADEMIC -> school.hei.haapi.model.ComplaintType.ACADEMIC;
      case FINANCIAL -> school.hei.haapi.model.ComplaintType.FINANCIAL;
    };
  }

  public List<school.hei.haapi.model.ComplaintType> toDomainTypes(List<ComplaintType> rest) {
    if (rest == null) {
      return List.of();
    }
    return rest.stream().map(this::toDomainType).toList();
  }

  public ComplaintStatus toRestStatus(school.hei.haapi.model.ComplaintStatus domain) {
    if (domain == null) return null;
    return switch (domain) {
      case CREATED -> ComplaintStatus.CREATED;
      case IN_PROGRESS -> ComplaintStatus.IN_PROGRESS;
      case RESOLVED -> ComplaintStatus.RESOLVED;
      case REJECTED -> ComplaintStatus.REJECTED;
    };
  }

  public school.hei.haapi.model.ComplaintStatus toDomainStatus(ComplaintStatus rest) {
    if (rest == null) return null;
    return switch (rest) {
      case CREATED -> school.hei.haapi.model.ComplaintStatus.CREATED;
      case IN_PROGRESS -> school.hei.haapi.model.ComplaintStatus.IN_PROGRESS;
      case RESOLVED -> school.hei.haapi.model.ComplaintStatus.RESOLVED;
      case REJECTED -> school.hei.haapi.model.ComplaintStatus.REJECTED;
    };
  }

  public List<school.hei.haapi.model.ComplaintStatus> toDomainStatuses(List<ComplaintStatus> rest) {
    if (rest == null) {
      return List.of();
    }
    return rest.stream().map(this::toDomainStatus).toList();
  }
}
