package school.hei.haapi.service.event;

import jakarta.transaction.Transactional;
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
  @Transactional
  public void accept(YearlyResultTranscriptGeneration yearlyResultTranscriptGeneration) {
    gradeResultService.uploadYearlyResultTranscript(
        yearlyResultTranscriptGeneration.getUserId(),
        yearlyResultTranscriptGeneration.getYearlyResult());
  }
}
