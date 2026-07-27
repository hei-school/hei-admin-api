package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.FeeCreationJobMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeStudentCreation;
import school.hei.haapi.endpoint.rest.validator.FeeCreationJobValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.FeeCreationJobService;
import school.hei.haapi.service.FeeCreationTaskService;

@RestController
@RequiredArgsConstructor
public class FeeCreationJobController {
  private final FeeCreationJobService feeCreationJobService;
  private final FeeCreationTaskService feeCreationTaskService;
  private final FeeCreationJobMapper feeCreationJobMapper;
  private final FeeCreationJobValidator feeCreationJobValidator;

  @PutMapping("/feeCreationJobs")
  public List<FeeCreationJob> crupdateFeeCreationJobs(
      @RequestBody List<CrupdateFeeCreationJob> crupdateFeeCreationJobs) {
    feeCreationJobValidator.accept(crupdateFeeCreationJobs);
    var toCreate =
        crupdateFeeCreationJobs.stream()
            .map(
                job ->
                    feeCreationJobMapper.toDomain(
                        job, feeCreationJobService.getFeeTemplateById(job.getFeeTemplateId())))
            .toList();
    return feeCreationJobService.crupdateAll(toCreate).stream()
        .map(feeCreationJobMapper::toRest)
        .toList();
  }

  @GetMapping("/feeCreationJobs")
  public List<FeeCreationJob> getFeeCreationJobs(
      @RequestParam PageFromOne page, @RequestParam("page_size") BoundedPageSize pageSize) {
    return feeCreationJobService.getAll(page, pageSize).stream()
        .map(feeCreationJobMapper::toRest)
        .toList();
  }

  @GetMapping("/feeCreationJobs/{id}")
  public FeeCreationJob getFeeCreationJobById(@PathVariable(name = "id") String id) {
    return feeCreationJobMapper.toRest(feeCreationJobService.getById(id));
  }

  @GetMapping("/feeCreationJobs/{id}/students")
  public List<FeeStudentCreation> getFeeCreationJobStudents(@PathVariable(name = "id") String id) {
    var tasks = feeCreationTaskService.findAllByJobId(id);
    var studentsByRef = feeCreationTaskService.findStudentsByRefs(tasks);
    return tasks.stream()
        .map(task -> feeCreationJobMapper.toRestStudentCreation(task, studentsByRef))
        .toList();
  }
}
