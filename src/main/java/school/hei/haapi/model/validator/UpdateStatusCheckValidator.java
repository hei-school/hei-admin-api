package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.UpdateStatusCheck;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class UpdateStatusCheckValidator implements Consumer<UpdateStatusCheck> {

  @Override
  public void accept(UpdateStatusCheck updateStatusCheck) {
    if (updateStatusCheck == null) {
      throw new BadRequestException("UpdateStatusCheck must not be null");
    }
    Set<String> violations = new HashSet<>();

    if (updateStatusCheck.getDescription() != null
        && updateStatusCheck.getDescription().isBlank()) {
      violations.add("Description must not be blank");
    }

    if (!violations.isEmpty()) {
      throw new BadRequestException(String.join(", ", violations));
    }
  }
}
