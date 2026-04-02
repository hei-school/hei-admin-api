package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateStatusCheck;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class CreateStatusCheckValidator implements Consumer<CreateStatusCheck> {

  @Override
  public void accept(CreateStatusCheck createStatusCheck) {
    if (createStatusCheck == null) {
      throw new BadRequestException("CreateStatusCheck must not be null");
    }
    Set<String> violations = new HashSet<>();

    if (createStatusCheck.getConcernedStudentId() == null
        || createStatusCheck.getConcernedStudentId().isBlank()) {
      violations.add("The concerned student ID is mandatory");
    }

    if (createStatusCheck.getRequestingUserId() == null
        || createStatusCheck.getRequestingUserId().isBlank()) {
      violations.add("The requesting user ID is mandatory");
    }

    if (createStatusCheck.getDescription() == null
        || createStatusCheck.getDescription().isBlank()) {
      violations.add("description is mandatory");
    }

    if (!violations.isEmpty()) {
      throw new BadRequestException(violations.toString());
    }
  }
}
