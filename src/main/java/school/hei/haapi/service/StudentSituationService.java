package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.SuspensionReason.LATE_FEES;
import static school.hei.haapi.endpoint.rest.model.SuspensionReason.OTHER;
import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.SituationLateFee;
import school.hei.haapi.endpoint.rest.model.SuspensionReason;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;

/** What a scanned badge tells about the fees of its student: late fees, never their amounts. */
@Service
@AllArgsConstructor
public class StudentSituationService {
  private final FeeRepository feeRepository;

  public List<SituationLateFee> lateFeesOf(User student) {
    return feeRepository.findLateFeesOfStudent(student.getId()).stream()
        .map(StudentSituationService::toSituationLateFee)
        .toList();
  }

  /** null when the student is not suspended. */
  public SuspensionReason suspensionReasonOf(User student, List<SituationLateFee> lateFees) {
    if (!SUSPENDED.equals(student.getStatus())) {
      return null;
    }
    return lateFees.isEmpty() ? OTHER : LATE_FEES;
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
