package school.hei.haapi.endpoint.rest.validator;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Transcript;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class CreateTranscriptValidator implements Consumer<Transcript> {

  @Override
  public void accept(Transcript transcript) {
    if (transcript.getId() == null) {
      throw new BadRequestException("id is mandatory");
    }
    if (transcript.getStudentId() == null) {
      throw new BadRequestException("Student id is mandatory");
    }
    if (transcript.getSemester() == null) {
      throw new BadRequestException("Semester is mandatory");
    }
    if (transcript.getAcademicYear() == null) {
      throw new BadRequestException("Academic year is mandatory");
    }
  }

  public void accept(List<Transcript> transcripts) {
    transcripts.forEach(this::accept);
  }
}
