package school.hei.haapi.endpoint.rest.validator;

import java.util.Objects;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class GradeValidator implements Consumer<CreateGrade> {

  @Override
  public void accept(CreateGrade crupdateGrade) {
    if (crupdateGrade == null) {
      throw new BadRequestException("Grade is null");
    }
    if (Objects.requireNonNull(crupdateGrade.getScore()) > 20
        || Objects.requireNonNull(crupdateGrade.getScore()) < 0) {
      throw new BadRequestException("score must be between 0 and 20");
    }
  }
}
