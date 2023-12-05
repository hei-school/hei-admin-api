package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FeeTypeComponent;
import school.hei.haapi.model.FeeTypeComponentEntity;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FeeTypeComponentValidator implements Consumer<FeeTypeComponentEntity> {

  public Set<String> getErrors(List<FeeTypeComponentEntity> FeeTypeComponent) {
    Set<String> violationMessages = new HashSet<>();
    FeeTypeComponent.forEach(
        feeTypeComponent -> {
          Set<String> feeTypeComponentError = getErrors(feeTypeComponent);
          if (!feeTypeComponentError.isEmpty()) {
            violationMessages.addAll(feeTypeComponentError);
          }
        });
    return violationMessages;
  }

  public Set<String> getErrors(FeeTypeComponentEntity feeTypeComponent) {
    Set<String> violationMessages = new HashSet<>();
    if (feeTypeComponent.getType() == null) {
      violationMessages.add("feeTypeComponent have to have a Type");
    }
    if (feeTypeComponent.getName() == null) {
      violationMessages.add("feeTypeComponent have to have a name");
    }
    if (feeTypeComponent.getMonthsNumber() == null) {
      violationMessages.add("feeTypeComponent have to have a MonthsNumber");
    }
    if (feeTypeComponent.getMonthlyAmount() == null) {
      violationMessages.add("feeTypeComponent have to have a MonthlyAmount");
    }
    return violationMessages;
  }

  public void accept(List<FeeTypeComponentEntity> feeTypeComponents) {
    feeTypeComponents.forEach(this);
  }

  @Override
  public void accept(FeeTypeComponentEntity feeTypeComponent) {
    Set<String> violationMessages = getErrors(feeTypeComponent);
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
