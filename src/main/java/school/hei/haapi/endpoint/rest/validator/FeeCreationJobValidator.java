package school.hei.haapi.endpoint.rest.validator;

import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeCreationJob;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FeeCreationJobValidator {

  public void accept(List<CrupdateFeeCreationJob> feeCreationJobs) {
    if (feeCreationJobs == null) {
      throw new BadRequestException("Provided fee creation jobs list is null");
    }
    feeCreationJobs.forEach(this::accept);
    var duplicatedJobIds =
        duplicatesOf(feeCreationJobs.stream().map(CrupdateFeeCreationJob::getId));
    if (!duplicatedJobIds.isEmpty()) {
      throw new BadRequestException(
          "Fee creation job ids must be unique, duplicated: " + duplicatedJobIds);
    }
  }

  public void accept(CrupdateFeeCreationJob feeCreationJob) {
    if (feeCreationJob == null) {
      throw new BadRequestException("Provided fee creation job is null");
    }
    if (feeCreationJob.getId() == null) {
      throw new BadRequestException("Fee creation job id is mandatory");
    }
    if (feeCreationJob.getFeeTemplateId() == null) {
      throw new BadRequestException("Fee creation job fee template id is mandatory");
    }
    var studentRefs = feeCreationJob.getStudentRefs();
    if (studentRefs == null || studentRefs.isEmpty()) {
      throw new BadRequestException("Fee creation job student refs are mandatory");
    }
    if (studentRefs.stream().anyMatch(ref -> ref == null || ref.isBlank())) {
      throw new BadRequestException("Fee creation job student refs cannot be blank");
    }
    var duplicatedRefs = duplicatesOf(studentRefs.stream());
    if (!duplicatedRefs.isEmpty()) {
      throw new BadRequestException(
          "Fee creation job student refs must be unique, duplicated: " + duplicatedRefs);
    }
  }

  private List<String> duplicatesOf(java.util.stream.Stream<String> values) {
    var seen = new HashSet<String>();
    return values.filter(value -> !seen.add(value)).distinct().sorted().toList();
  }
}
