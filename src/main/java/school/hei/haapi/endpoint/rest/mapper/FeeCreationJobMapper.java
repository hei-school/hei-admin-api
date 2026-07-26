package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PENDING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeCreationJobFailure;
import school.hei.haapi.endpoint.rest.model.FeeCreationJobStatistics;
import school.hei.haapi.endpoint.rest.model.FeeStudentCreation;
import school.hei.haapi.endpoint.rest.model.FeeStudentCreationStatus;
import school.hei.haapi.endpoint.rest.model.JobStatus;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.User;
import school.hei.haapi.model.V2FeeTemplate;

@Component
@AllArgsConstructor
public class FeeCreationJobMapper {
  private final FeeTemplateMapper feeTemplateMapper;
  private final UserMapper userMapper;

  public school.hei.haapi.model.FeeCreationJob toDomain(
      CrupdateFeeCreationJob rest, V2FeeTemplate feeTemplate) {
    var job =
        school.hei.haapi.model.FeeCreationJob.builder()
            .id(rest.getId())
            .feeTemplate(feeTemplate)
            .tasks(new ArrayList<>())
            .build();
    rest.getStudentRefs()
        .forEach(
            studentRef -> job.addTask(FeeCreationTask.builder().studentRef(studentRef).build()));
    return job;
  }

  public school.hei.haapi.endpoint.rest.model.FeeCreationJob toRest(
      school.hei.haapi.model.FeeCreationJob domain) {
    var tasks = domain.getTasks() == null ? List.<FeeCreationTask>of() : domain.getTasks();
    return new school.hei.haapi.endpoint.rest.model.FeeCreationJob()
        .id(domain.getId())
        .feeTemplate(feeTemplateMapper.toRestDetailed(domain.getFeeTemplate()))
        .creationDatetime(domain.getCreationDatetime())
        .endDatetime(domain.getEndDatetime())
        .status(toRestStatus(domain))
        .statistics(toRestStatistics(domain, tasks))
        .failures(toRestFailures(tasks));
  }

  private JobStatus toRestStatus(school.hei.haapi.model.FeeCreationJob domain) {
    return domain
        .getActualStatus()
        .map(
            status ->
                new JobStatus()
                    .progression(status.getProgression())
                    .health(status.getHealth())
                    .creationDatetime(status.getCreationDatetime()))
        .orElseGet(
            () ->
                new JobStatus()
                    .progression(PENDING)
                    .health(UNKNOWN)
                    .creationDatetime(domain.getCreationDatetime()));
  }

  private FeeCreationJobStatistics toRestStatistics(
      school.hei.haapi.model.FeeCreationJob domain, List<FeeCreationTask> tasks) {
    var statistics = domain.getStatistics();
    if (statistics == null) {
      return new FeeCreationJobStatistics()
          .totalCount(tasks.size())
          .successCount(0)
          .failureCount(0)
          .updateDatetime(domain.getCreationDatetime());
    }
    return new FeeCreationJobStatistics()
        .totalCount(statistics.getTotalCount())
        .successCount(statistics.getSuccessCount())
        .failureCount(statistics.getFailureCount())
        .updateDatetime(statistics.getUpdateDatetime());
  }

  public FeeStudentCreation toRestStudentCreation(FeeCreationTask task, Map<String, User> byRef) {
    var student = byRef.get(task.getStudentRef());
    return new FeeStudentCreation()
        .student(
            student == null
                ? new UserIdentifier().ref(task.getStudentRef())
                : userMapper.toIdentifier(student))
        .feesCreationStatus(toRestTaskStatus(task))
        .message(task.getMessage());
  }

  private FeeStudentCreationStatus toRestTaskStatus(FeeCreationTask task) {
    return task.getActualStatus()
        .map(
            status ->
                new FeeStudentCreationStatus()
                    .progression(status.getProgression())
                    .health(status.getHealth())
                    .creationDatetime(status.getCreationDatetime()))
        .orElseGet(() -> new FeeStudentCreationStatus().progression(PENDING).health(UNKNOWN));
  }

  private List<FeeCreationJobFailure> toRestFailures(List<FeeCreationTask> tasks) {
    return tasks.stream()
        .filter(
            task ->
                task.getActualStatus()
                    .map(status -> FAILED.equals(status.getHealth()))
                    .orElse(false))
        .map(
            task ->
                new FeeCreationJobFailure()
                    .studentRef(task.getStudentRef())
                    .message(task.getMessage()))
        .toList();
  }
}
