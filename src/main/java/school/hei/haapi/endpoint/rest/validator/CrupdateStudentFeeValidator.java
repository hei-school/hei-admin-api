package school.hei.haapi.endpoint.rest.validator;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateStudentFee;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class CrupdateStudentFeeValidator implements Consumer<CrupdateStudentFee> {

  @Override
  public void accept(CrupdateStudentFee crupdateStudentFee) {
    if (crupdateStudentFee.getStudentId() == null) {
      throw new BadRequestException("Student id is mandatory");
    }
    if (crupdateStudentFee.getTotalAmount() == null) {
      throw new BadRequestException("Total amount is mandatory");
    }
  }
}
