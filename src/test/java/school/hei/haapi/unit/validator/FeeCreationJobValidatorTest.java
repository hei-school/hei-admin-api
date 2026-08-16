package school.hei.haapi.unit.validator;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsDomainBadRequestException;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeCreationJob;
import school.hei.haapi.endpoint.rest.validator.FeeCreationJobValidator;

class FeeCreationJobValidatorTest {
  private final FeeCreationJobValidator subject = new FeeCreationJobValidator();

  private static CrupdateFeeCreationJob aJob(
      String id, String feeTemplateId, List<String> studentRefs) {
    return new CrupdateFeeCreationJob()
        .id(id)
        .feeTemplateId(feeTemplateId)
        .studentRefs(studentRefs);
  }

  private static CrupdateFeeCreationJob validJob(String id) {
    return aJob(id, "template_id", List.of("STD21001", "STD21002"));
  }

  @Test
  void accept_valid_jobs_does_not_throw() {
    assertDoesNotThrow(() -> subject.accept(List.of(validJob("job_1"), validJob("job_2"))));
  }

  @Test
  void accept_empty_list_does_not_throw() {
    assertDoesNotThrow(() -> subject.accept(List.of()));
  }

  @Test
  void accept_null_list_throws() {
    assertThrowsDomainBadRequestException(
        "Provided fee creation jobs list is null",
        () -> subject.accept((List<CrupdateFeeCreationJob>) null));
  }

  @Test
  void accept_null_job_throws() {
    assertThrowsDomainBadRequestException(
        "Provided fee creation job is null",
        () -> subject.accept(asList(validJob("job_1"), (CrupdateFeeCreationJob) null)));
  }

  @Test
  void accept_job_without_id_throws() {
    var job = aJob(null, "template_id", List.of("STD21001"));
    assertThrowsDomainBadRequestException(
        "Fee creation job id is mandatory", () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_job_without_fee_template_id_throws() {
    var job = aJob("job_1", null, List.of("STD21001"));
    assertThrowsDomainBadRequestException(
        "Fee creation job fee template id is mandatory", () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_job_without_student_refs_throws() {
    var job = aJob("job_1", "template_id", null);
    assertThrowsDomainBadRequestException(
        "Fee creation job student refs are mandatory", () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_job_with_empty_student_refs_throws() {
    var job = aJob("job_1", "template_id", List.of());
    assertThrowsDomainBadRequestException(
        "Fee creation job student refs are mandatory", () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_job_with_blank_student_ref_throws() {
    var job = aJob("job_1", "template_id", List.of("STD21001", "  "));
    assertThrowsDomainBadRequestException(
        "Fee creation job student refs cannot be blank", () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_job_with_null_student_ref_throws() {
    var job = aJob("job_1", "template_id", asList("STD21001", null));
    assertThrowsDomainBadRequestException(
        "Fee creation job student refs cannot be blank", () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_job_with_duplicated_student_refs_throws() {
    var job = aJob("job_1", "template_id", List.of("STD21001", "STD21002", "STD21001"));
    assertThrowsDomainBadRequestException(
        "Fee creation job student refs must be unique, duplicated: [STD21001]",
        () -> subject.accept(List.of(job)));
  }

  @Test
  void accept_jobs_with_duplicated_ids_throws() {
    assertThrowsDomainBadRequestException(
        "Fee creation job ids must be unique, duplicated: [job_1]",
        () -> subject.accept(List.of(validJob("job_1"), validJob("job_1"))));
  }
}
