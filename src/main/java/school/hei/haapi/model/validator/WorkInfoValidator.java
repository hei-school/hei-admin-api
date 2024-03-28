package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.WorkInfo;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class WorkInfoValidator implements Consumer<WorkInfo> {
  public void accept(List<WorkInfo> workInfoToValidate) {
    workInfoToValidate.forEach(this::accept);
  }

  @Override
  public void accept(WorkInfo workInfo) {
    Set<String> violationMessages = new HashSet<>();
    if (workInfo.getStudent() == null) {
      violationMessages.add("Student is mandatory");
    }
    if (workInfo.getCommitmentBeginDate() == null) {
      violationMessages.add("Commitment begin date is mandatory");
    }
    if (workInfo.getBusiness() == null
        && workInfo.getBusiness().isEmpty()
        && workInfo.getBusiness().isBlank()) {
      violationMessages.add("Student business is mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formatedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formatedViolationMessages);
    }
  }
}
