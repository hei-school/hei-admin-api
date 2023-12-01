package school.hei.haapi.model.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class UUIDValidator implements Consumer<String> {

  public void accept(List<String> UIDIds) {
    UIDIds.forEach(this);
  }

  @Override
  public void accept(String UIDId) {
    Set<String> violationMessages = new HashSet<>();
    try {
      UUID uid = UUID.fromString(UIDId);
    } catch (Exception e) {
      violationMessages.add("The Id " + UIDId + " must be an UID.");
    }
    if (!violationMessages.isEmpty()) {
      String formattedViolationMessages =
          violationMessages.stream().map(String::toString).collect(Collectors.joining(". "));
      throw new BadRequestException(formattedViolationMessages);
    }
  }
}
