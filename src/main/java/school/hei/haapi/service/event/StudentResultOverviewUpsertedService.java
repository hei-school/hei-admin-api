package school.hei.haapi.service.event;

import static org.reflections.Reflections.log;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.service.StudentResultOverviewService;

@Service
@AllArgsConstructor
public class StudentResultOverviewUpsertedService
    implements Consumer<StudentResultOverviewUpsertedService> {
  private final StudentResultOverviewService studentResultOverviewService;

  @Override
  public void accept(StudentResultOverviewUpsertedService studentResultOverviewUpserted) {
    studentResultOverviewService.saveAll();
    log.info("Crupdate studentResultOverviews");
  }
}
