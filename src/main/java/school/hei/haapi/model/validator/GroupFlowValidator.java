package school.hei.haapi.model.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.exception.BadRequestException;

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
    if (!violations.isEmpty()) {
      String constraintMessages =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining(". "));
      throw new BadRequestException(constraintMessages);
    }
  }
}
