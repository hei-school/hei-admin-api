package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.StudentResultOverviewUpserted;
import school.hei.haapi.service.StudentResultOverviewService;

@Slf4j
@Service
@AllArgsConstructor
public class StudentResultOverviewUpsertedService
    implements Consumer<StudentResultOverviewUpserted> {
  private final StudentResultOverviewService studentResultOverviewService;

  @Override
  public void accept(StudentResultOverviewUpserted studentResultOverviewUpserted) {
    studentResultOverviewService.saveAll();
    log.info("Crupdate studentResultOverviews");
  }
}
