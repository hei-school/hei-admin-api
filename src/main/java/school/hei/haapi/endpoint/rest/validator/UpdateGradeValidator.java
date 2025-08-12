package school.hei.haapi.endpoint.rest.validator;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class UpdateGradeValidator implements Consumer<UpdateGrade> {
  private GradeValidator gradeValidator;

  @Override
  public void accept(UpdateGrade crupdateGrade) {
    gradeValidator.accept(crupdateGrade.getGrade());

    if (crupdateGrade.getComment() == null || crupdateGrade.getComment().isBlank()) {
      throw new BadRequestException(
          "Grade modification must be followed by comment about the change");
    }
  }
}
