package school.hei.haapi.endpoint.rest.validator;

import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.ForbiddenException;

@Component
@AllArgsConstructor
public class CreateFeeValidator implements Consumer<CreateFee> {
  private final AuthProvider authProvider;

  @Override
  public void accept(CreateFee createFee) {
    var principalRole = authProvider.getPrincipal().getUser().getRole();

    if (createFee.getTotalAmount() == null) {
      throw new BadRequestException("Total amount is mandatory");
    }
    if (STUDENT.equals(principalRole) && !REMEDIAL_COSTS.equals(createFee.getType())) {
      throw new ForbiddenException("Student is only allowed for REMEDIAL_COSTS");
    }
  }
}
