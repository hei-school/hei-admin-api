package school.hei.haapi.model.validator;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.TranscriptClaim;
import school.hei.haapi.model.exception.BadRequestException;

@Component
@AllArgsConstructor
public class TranscriptClaimValidator implements Consumer<TranscriptClaim> {
  private final Validator validator;

  public void accept(List<TranscriptClaim> transcriptClaims) {
    transcriptClaims.forEach(this::accept);
  }

  @Override
  public void accept(TranscriptClaim transcriptClaim) {
    Set<ConstraintViolation<TranscriptClaim>> violations = validator.validate(transcriptClaim);
    if (transcriptClaim.getId() == null) {
      throw new BadRequestException("TranscriptClaim id is mandatory");
    }
    if (transcriptClaim.getClaimStatus() == null) {
      throw new BadRequestException("Status is mandatory");
    }
    if (transcriptClaim.getCreationDatetime() != null
        && transcriptClaim.getClosedDatetime() != null
        && transcriptClaim.getClosedDatetime().isBefore(transcriptClaim.getCreationDatetime())) {
      throw new BadRequestException("closed datetime should be after creation datetime");
    }
    if (transcriptClaim.getReason() == null) {
      throw new BadRequestException("Reason is mandatory");
    }

    if (!violations.isEmpty()) {
      String constraintMessages =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining(". "));
      throw new BadRequestException(constraintMessages);
    }
  }
}
