package school.hei.haapi.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.controller.FeeCreationJobController;
import school.hei.haapi.endpoint.rest.mapper.FeeCreationJobMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeStudentCreation;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.endpoint.rest.validator.FeeCreationJobValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FeeCreationTask;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.FeeCreationJobService;
import school.hei.haapi.service.FeeCreationTaskService;

class FeeCreationJobControllerTest {
  private FeeCreationJobService feeCreationJobService;
  private FeeCreationTaskService feeCreationTaskService;
  private FeeCreationJobMapper feeCreationJobMapper;
  private FeeCreationJobValidator feeCreationJobValidator;
  private FeeCreationJobController subject;

  @BeforeEach
  void setUp() {
    feeCreationJobService = mock();
    feeCreationTaskService = mock();
    feeCreationJobMapper = mock();
    feeCreationJobValidator = mock();
    subject =
        new FeeCreationJobController(
            feeCreationJobService,
            feeCreationTaskService,
            feeCreationJobMapper,
            feeCreationJobValidator);
  }

  private static CrupdateFeeCreationJob aPayload() {
    return new CrupdateFeeCreationJob()
        .id("job_1")
        .feeTemplateId("template_id")
        .studentRefs(List.of("STD21001"));
  }

  @Test
  void crupdate_validates_maps_and_delegates() {
    var payload = aPayload();
    var feeTemplate = V2FeeTemplate.builder().id("template_id").build();
    var domain = school.hei.haapi.model.FeeCreationJob.builder().id("job_1").build();
    var saved = school.hei.haapi.model.FeeCreationJob.builder().id("job_1").build();
    var rest = new FeeCreationJob().id("job_1");
    when(feeCreationJobService.getFeeTemplateById("template_id")).thenReturn(feeTemplate);
    when(feeCreationJobMapper.toDomain(payload, feeTemplate)).thenReturn(domain);
    when(feeCreationJobService.crupdateAll(List.of(domain))).thenReturn(List.of(saved));
    when(feeCreationJobMapper.toRest(saved)).thenReturn(rest);

    var actual = subject.crupdateFeeCreationJobs(List.of(payload));

    assertEquals(List.of(rest), actual);
    verify(feeCreationJobValidator).accept(List.of(payload));
    verify(feeCreationJobService).crupdateAll(List.of(domain));
  }

  @Test
  void crupdate_does_not_persist_when_validation_fails() {
    var payload = aPayload();
    doThrow(new BadRequestException("Fee creation job id is mandatory"))
        .when(feeCreationJobValidator)
        .accept(List.of(payload));

    assertThrows(
        BadRequestException.class, () -> subject.crupdateFeeCreationJobs(List.of(payload)));
    verify(feeCreationJobService, never()).crupdateAll(any());
    verifyNoInteractions(feeCreationJobMapper);
  }

  @Test
  void getFeeCreationJobs_delegates_paging_and_maps_to_rest() {
    var domain = school.hei.haapi.model.FeeCreationJob.builder().id("job_1").build();
    var rest = new FeeCreationJob().id("job_1");
    var page = new PageFromOne(1);
    var pageSize = new BoundedPageSize(10);
    when(feeCreationJobService.getAll(page, pageSize)).thenReturn(List.of(domain));
    when(feeCreationJobMapper.toRest(domain)).thenReturn(rest);

    var actual = subject.getFeeCreationJobs(page, pageSize);

    assertEquals(List.of(rest), actual);
  }

  @Test
  void getFeeCreationJobById_delegates_and_maps_to_rest() {
    var domain = school.hei.haapi.model.FeeCreationJob.builder().id("job_1").build();
    var rest = new FeeCreationJob().id("job_1");
    when(feeCreationJobService.getById("job_1")).thenReturn(domain);
    when(feeCreationJobMapper.toRest(domain)).thenReturn(rest);

    assertEquals(rest, subject.getFeeCreationJobById("job_1"));
  }

  @Test
  void getFeeCreationJobStudents_resolves_students_once_and_maps_each_task() {
    var task = FeeCreationTask.builder().id("task_1").studentRef("STD21001").build();
    var student = User.builder().id("student_id").ref("STD21001").build();
    var studentsByRef = Map.of("STD21001", student);
    var rest = new FeeStudentCreation().student(new UserIdentifier().ref("STD21001"));
    when(feeCreationTaskService.findAllByJobId("job_1")).thenReturn(List.of(task));
    when(feeCreationTaskService.findStudentsByRefs(List.of(task))).thenReturn(studentsByRef);
    when(feeCreationJobMapper.toRestStudentCreation(task, studentsByRef)).thenReturn(rest);

    var actual = subject.getFeeCreationJobStudents("job_1");

    assertEquals(List.of(rest), actual);
    verify(feeCreationTaskService).findStudentsByRefs(List.of(task));
  }
}
