package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.YearlyResultTranscriptGeneration;
import school.hei.haapi.service.GradeResultService;

@Service
@RequiredArgsConstructor
public class YearlyResultTranscriptGenerationService
    implements Consumer<YearlyResultTranscriptGeneration> {
  private final GradeResultService gradeResultService;

  @Override
  public void accept(YearlyResultTranscriptGeneration yearlyResultTranscriptGeneration) {
    gradeResultService.uploadYearlyResultTranscript(
        yearlyResultTranscriptGeneration.getUser(),
        yearlyResultTranscriptGeneration.getYearlyResult());
  }
}
