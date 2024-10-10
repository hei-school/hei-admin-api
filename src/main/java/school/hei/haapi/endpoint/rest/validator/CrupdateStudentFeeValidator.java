package school.hei.haapi.endpoint.rest.validator;

import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateStudentFee;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.ForbiddenException;

@Component
@AllArgsConstructor
public class CrupdateStudentFeeValidator implements Consumer<CrupdateStudentFee> {
  private final AuthProvider authProvider;

  @Override
  public void accept(CrupdateStudentFee crupdateStudentFee) {
    var principalRole = authProvider.getPrincipal().getUser().getRole();

    if (crupdateStudentFee.getStudentId() == null) {
      throw new BadRequestException("Student id is mandatory");
    }
    if (crupdateStudentFee.getTotalAmount() == null) {
      throw new BadRequestException("Total amount is mandatory");
    }
    if (STUDENT.equals(principalRole)
        && (!REMEDIAL_COSTS.equals(crupdateStudentFee.getType())
            && !TUITION.equals(crupdateStudentFee.getType()))) {
      throw new ForbiddenException("Student is only allowed for REMEDIAL_COSTS or TUITION");
    }
  }
}
