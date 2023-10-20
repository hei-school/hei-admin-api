package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.exception.BadRequestException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GroupFlowValidator implements Consumer<GroupFlow> {
  private final Validator validator;

  public void accept(List<GroupFlow> groupFlows) {
    groupFlows.forEach(this::accept);
  }

  @Override
  public void accept(GroupFlow groupFlow) {
    Set<ConstraintViolation<GroupFlow>> violations = validator.validate(groupFlow);
    if (groupFlow.getGroup() == null) {
      throw new BadRequestException("Group is mandatory");
    }
    if (groupFlow.getStudent() == null) {
      throw new BadRequestException("Student is mandatory");
    }
    if (groupFlow.getFlowDatetime() == null) {
      throw new BadRequestException("Flow datetime datetime is mandatory");
    }
    if (!violations.isEmpty()) {
      String constraintMessages = violations.stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(constraintMessages);
    }
  }
}
