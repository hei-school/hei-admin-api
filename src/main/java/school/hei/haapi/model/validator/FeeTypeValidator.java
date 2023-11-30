package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.FeeType;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class FeeTypeValidator implements Consumer<FeeType> {

  private final FeeTypeComponentValidator feeTypeComponentValidator;

  public void accept(List<FeeType> feeTypes) {
    feeTypes.forEach(this::accept);
  }

  @Override
  public void accept(FeeType feeType) {
    Set<String> violationMessages = new HashSet<>();
    if (feeType.getTypes() == null || feeType.getTypes().isEmpty()) {
      violationMessages.add("Type is mandatory");
    } else {
      feeTypeComponentValidator.accept(feeType.getTypes());
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
