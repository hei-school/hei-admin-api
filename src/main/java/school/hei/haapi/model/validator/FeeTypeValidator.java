package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.FeeTypeEntity;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class FeeTypeValidator implements Consumer<FeeTypeEntity> {
  private final FeeTypeComponentValidator feeTypeComponentValidator;

  public void accept(List<FeeTypeEntity> feeTypes) {
    feeTypes.forEach(this);
  }

  @Override
  public void accept(FeeTypeEntity feeType) {
    Set<String> violationMessages = new HashSet<>();
    if (feeType.getId() == null) {
      violationMessages.add("Id is mandatory");
    }
    if (feeType.getName() == null) {
      violationMessages.add("Name is mandatory");
    }
    if (feeType.getFeeTypeComponentEntities() == null
        || feeType.getFeeTypeComponentEntities().isEmpty()) {
      violationMessages.add("Type is mandatory");
    }
    Set<String> feeTypeComponentError =
        feeTypeComponentValidator.getErrors(feeType.getFeeTypeComponentEntities());
    if (!feeTypeComponentError.isEmpty()) {
      violationMessages.addAll(feeTypeComponentError);
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
