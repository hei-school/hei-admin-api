package school.hei.haapi.endpoint.rest.validator;

import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreateStudentWorkFileValidator {
  public void acceptWorkDocumentField(
      String fileName,
      Instant commitmentBegin,
      Instant commitmentEnd,
      ProfessionalExperienceFileTypeEnum professionalExperienceType) {
    if (Objects.isNull(fileName) || fileName.isEmpty() || fileName.isBlank()) {
      throw new BadRequestException("Filename is mandatory");
    }
    if (Objects.isNull(commitmentBegin)) {
      throw new BadRequestException("Commitment begin date is mandatory");
    }
    if (Objects.isNull(professionalExperienceType)) {
      throw new BadRequestException("Professional experience type is mandatory");
    }
    if (!Objects.isNull(commitmentEnd)
        && acceptCommitmentBeginIsLessThan(commitmentBegin, commitmentEnd)) {
      throw new BadRequestException("Commitment begin must be less than commitment end");
    }
  }

  private boolean acceptCommitmentBeginIsLessThan(Instant commtimentBegin, Instant toCompare) {
    return commtimentBegin.isAfter(toCompare);
  }
}
