package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.model.StudentResultOverviewUpserted;
import school.hei.haapi.service.StudentResultOverviewService;
import school.hei.haapi.service.YearlyResultService;

@Slf4j
@Service
@AllArgsConstructor
public class StudentResultOverviewUpsertedService
    implements Consumer<StudentResultOverviewUpserted> {
  private final StudentResultOverviewService studentResultOverviewService;
  private final YearlyResultService yearlyResultService;

  @Override
  @Transactional
  public void accept(StudentResultOverviewUpserted event) {
    log.info("Beggin crupdate StudentsResultoverviews");
    log.info("Get all studentsResultOverviews");
    var studentsResultOverviewsToSave =
        studentResultOverviewService.getStudentResultOverviewsToCrupdate(event.getPromotionId());
    log.info(
        "The number StudentsResultOverviews found is : {%s}"
            .formatted(studentsResultOverviewsToSave.size()));
    studentResultOverviewService.saveAll(studentsResultOverviewsToSave);
    log.info("End crupdate StudentsResultoverviews");

    log.info("Beggin crupdate YearlyResults");
    var yearlyResultsToSave =
        yearlyResultService.getYearlyResultsToCrupdate(event.getPromotionId());
    log.info("The number YearlyResults found is : {%s}".formatted(yearlyResultsToSave.size()));
    yearlyResultService.saveAll(yearlyResultsToSave);
    log.info("End crupdate YearlyResults");
  }
}
