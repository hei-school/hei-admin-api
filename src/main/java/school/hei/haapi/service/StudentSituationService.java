package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.SuspensionReason.LATE_FEES;
import static school.hei.haapi.endpoint.rest.model.SuspensionReason.OTHER;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.endpoint.rest.model.SituationLateFee;
import school.hei.haapi.endpoint.rest.model.StudentSituation;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;

@Service
@AllArgsConstructor
public class StudentSituationService {
  private final StudentBadgeCodeService studentBadgeCodeService;
  private final FeeRepository feeRepository;
  private final StatusEnumMapper statusEnumMapper;

  public StudentSituation getSituationOfBadge(String publicId) {
    return situationOf(studentBadgeCodeService.getValidBadge(publicId).getStudent());
  }

  StudentSituation situationOf(User student) {
    var situation =
        new StudentSituation().status(statusEnumMapper.toRestStatus(student.getStatus()));
    if (!SUSPENDED.equals(student.getStatus())) {
      return situation;
    }
    var lateFees =
        feeRepository.findLateFeesOfStudent(student.getId()).stream()
            .map(StudentSituationService::toSituationLateFee)
            .toList();
    return situation.suspensionReason(lateFees.isEmpty() ? OTHER : LATE_FEES).lateFees(lateFees);
  }

  private static SituationLateFee toSituationLateFee(Fee fee) {
    var comment = fee.getComment();
    var label =
        comment == null || comment.isBlank()
            ? (fee.getType() == null ? null : fee.getType().getValue())
            : comment;
    return new SituationLateFee().label(label).dueDatetime(fee.getDueDatetime());
  }
}
