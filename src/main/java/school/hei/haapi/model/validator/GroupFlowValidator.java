package school.hei.haapi.model.validator;

import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;

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
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class GroupFlowValidator implements Consumer<GroupFlow> {
  private final Validator validator;
  private final UserService userService;

  public void accept(List<GroupFlow> groupFlows) {
    groupFlows.forEach(this::accept);
  }

  @Override
  public void accept(GroupFlow groupFlow) {
    var studentsGroup = userService.getByGroupId(groupFlow.getGroup().getId());
    boolean isStudentInGroup = studentsGroup.contains(groupFlow.getStudent());
    GroupFlow.GroupFlowType groupFlowType = groupFlow.getGroupFlowType();

    Set<ConstraintViolation<GroupFlow>> violations = validator.validate(groupFlow);
    if (groupFlow.getGroup() == null) {
      throw new BadRequestException("Group is mandatory");
    }
    if (groupFlow.getStudent() == null) {
      throw new BadRequestException("Student is mandatory");
    }
    if (isStudentInGroup && JOIN.equals(groupFlowType)) {
      throw new BadRequestException("Student is already in group");
    }
    if (!isStudentInGroup && LEAVE.equals(groupFlowType)) {
      throw new BadRequestException("Student has already left this group");
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
