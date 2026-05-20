package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import school.hei.haapi.endpoint.rest.model.ResultOverviewStatus;
import school.hei.haapi.endpoint.rest.model.ResultsOverview;
import school.hei.haapi.endpoint.rest.model.StudentResultOverview;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;

public class StudentResultOverviewMapper {
  public StudentResultOverview toRest(
      school.hei.haapi.model.StudentResultOverview studentResultOverview) {
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
    return studentResultOverviews.stream().map(this::toRest).toList();
  }
}
