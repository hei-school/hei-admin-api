package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.ResultsOverview;
import school.hei.haapi.endpoint.rest.model.StudentResultOverview;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;

@Slf4j
@Component
@AllArgsConstructor
public class StudentResultOverviewMapper {
  public StudentResultOverview toRest(
      school.hei.haapi.model.StudentResultOverview studentResultOverview) {
    log.info("student result overview to rest : " + studentResultOverview);
    var user = studentResultOverview.getStudent();
    var student =
        new UserIdentifier()
            .id(user.getId())
            .ref(user.getRef())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .nic(user.getNic());
    var resultOverview =
        new ResultsOverview()
            .weightedAverage(studentResultOverview.getWeightedAverage())
            .obtainedCredits(studentResultOverview.getObtainedCredits())
            .status(ResultOverviewStatus.valueOf(studentResultOverview.getStatus().toString()))
            .totalCredits(studentResultOverview.getTotalCredits());
    return new StudentResultOverview().student(student).resultsOverview(resultOverview);
  }

  public List<StudentResultOverview> toRestList(
      List<school.hei.haapi.model.StudentResultOverview> studentResultOverviews) {
    log.info("students results overviews to rest : " + studentResultOverviews);
    return studentResultOverviews.stream().map(this::toRest).toList();
  }
}
