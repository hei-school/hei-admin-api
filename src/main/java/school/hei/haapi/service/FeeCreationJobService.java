package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatisticsComputationTriggered;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatusComputationTriggered;
import school.hei.haapi.endpoint.event.model.FeeCreationTaskTriggered;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FeeCreationJob;
import school.hei.haapi.model.FeeCreationJobStatistics;
import school.hei.haapi.model.JobStatus;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FeeCreationJobRepository;
import school.hei.haapi.repository.V2FeeTemplateRepository;

@Service
@AllArgsConstructor
public class FeeCreationJobService {
  private final FeeCreationJobRepository feeCreationJobRepository;
  private final V2FeeTemplateRepository v2FeeTemplateRepository;
  private final EventProducer eventProducer;

  public FeeCreationJob getById(String id) {
    return feeCreationJobRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("FeeCreationJob.id=" + id + " not found"));
  }

  @Transactional(readOnly = true)
  public Optional<JobStatus> getActualStatusOf(String jobId) {
    return getById(jobId).getActualStatus();
  }

  public List<FeeCreationJob> getAll(PageFromOne page, BoundedPageSize pageSize) {
    var pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return feeCreationJobRepository.findAll(pageable).getContent();
  }

  public school.hei.haapi.model.V2FeeTemplate getFeeTemplateById(String feeTemplateId) {
    return v2FeeTemplateRepository
        .findById(feeTemplateId)
        .orElseThrow(() -> new NotFoundException("FeeTemplate.id=" + feeTemplateId + " not found"));
  }

  @Transactional
  public FeeCreationJob crupdate(FeeCreationJob toCreate) {
    var alreadyKnown = feeCreationJobRepository.findById(toCreate.getId());
    if (alreadyKnown.isPresent()) {
      return alreadyKnown.get();
    }
    var saved = feeCreationJobRepository.save(toCreate);
    saved
        .getTasks()
        .forEach(
            task ->
                eventProducer.accept(
                    List.of(FeeCreationTaskTriggered.builder().task(task).build())));
    var jobId = saved.getId();

    eventProducer.accept(
        List.of(FeeCreationJobStatusComputationTriggered.builder().jobId(jobId).build()));
    eventProducer.accept(
        List.of(FeeCreationJobStatisticsComputationTriggered.builder().jobId(jobId).build()));

    return saved;
  }

  public List<FeeCreationJob> crupdateAll(List<FeeCreationJob> toCreate) {
    return toCreate.stream().map(this::crupdate).toList();
  }

  @Transactional
  public FeeCreationJob updateStatus(String jobId, JobStatus status) {
    var job = getById(jobId);
    status.setJob(job);
    if (job.getStatuses() == null) {
      job.setStatuses(new ArrayList<>());
    }
    job.getStatuses().add(status);
    if (FINISHED.equals(status.getProgression()) && job.getEndDatetime() == null) {
      job.setEndDatetime(Instant.now());
    }
    return feeCreationJobRepository.save(job);
  }

  @Transactional
  public FeeCreationJob updateStatistics(String jobId, FeeCreationJobStatistics statistics) {
    var job = getById(jobId);
    var actual = job.getStatistics();
    if (actual == null) {
      statistics.setJob(job);
      job.setStatistics(statistics);
    } else {
      actual.setTotalCount(statistics.getTotalCount());
      actual.setSuccessCount(statistics.getSuccessCount());
      actual.setFailureCount(statistics.getFailureCount());
    }
    return feeCreationJobRepository.save(job);
  }
}
