package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.endpoint.rest.model.JobHealth.FAILED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.SUCCEEDED;
import static school.hei.haapi.endpoint.rest.model.JobHealth.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.JobProgression.FINISHED;
import static school.hei.haapi.endpoint.rest.model.JobProgression.PENDING;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatisticsComputationTriggered;
import school.hei.haapi.endpoint.event.model.FeeCreationJobStatusComputationTriggered;
import school.hei.haapi.endpoint.event.model.FeeCreationTaskTriggered;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeCreationJob;
import school.hei.haapi.endpoint.rest.model.FeeStudentCreation;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.FeeCreationJobStatisticsComputationTriggeredService;
import school.hei.haapi.service.event.FeeCreationJobStatusComputationTriggeredService;
import school.hei.haapi.service.event.FeeCreationTaskTriggeredService;

@Testcontainers
@AutoConfigureMockMvc
public class FeeCreationJobIT extends FacadeITMockedThirdParties {
  private static final String FEE_TEMPLATE_ID = "v2_fee_template1_id";
  private static final String UNKNOWN_STUDENT_REF = "STD21099";

  @MockBean private EventProducer eventProducer;

  @Autowired private FeeCreationTaskTriggeredService taskConsumer;
  @Autowired private FeeCreationJobStatusComputationTriggeredService statusConsumer;
  @Autowired private FeeCreationJobStatisticsComputationTriggeredService statisticsConsumer;
  @Autowired private FeeRepository feeRepository;
  @Autowired private UserService userService;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  private PayingApi managerApi() {
    return new PayingApi(anApiClient(MANAGER1_TOKEN));
  }

  private FeeCreationJob submitJob(String jobId, List<String> studentRefs) throws ApiException {
    var submitted =
        managerApi()
            .crupdateFeeCreationJobs(
                List.of(
                    new CrupdateFeeCreationJob()
                        .id(jobId)
                        .feeTemplateId(FEE_TEMPLATE_ID)
                        .studentRefs(studentRefs)));
    assertEquals(1, submitted.size());
    return submitted.getFirst();
  }

  /** Every event the producer was handed so far, flattened and filtered to the given type. */
  @SuppressWarnings("unchecked")
  private <T> List<T> dispatchedOfType(Class<T> type) {
    var captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer, atLeastOnce()).accept(captor.capture());
    return (List<T>)
        captor.getAllValues().stream()
            .flatMap(events -> ((List<Object>) events).stream())
            .filter(type::isInstance)
            .toList();
  }

  private void drainTasksOf(String jobId, int expectedTaskCount) {
    var taskEvents =
        dispatchedOfType(FeeCreationTaskTriggered.class).stream()
            // the producer mock accumulates across submissions, so only this job's tasks are run
            .filter(event -> jobId.equals(event.getTask().getJob().getId()))
            .toList();
    assertEquals(expectedTaskCount, taskEvents.size());

    taskEvents.forEach(taskConsumer::accept);
    computeJobOf(jobId);
  }

  private void computeJobOf(String jobId) {
    try {
      statusConsumer.accept(
          FeeCreationJobStatusComputationTriggered.builder().jobId(jobId).build());
    } catch (IllegalStateException retriedLater) {
      // the job did not settle, the event would come back later
    }
    try {
      statisticsConsumer.accept(
          FeeCreationJobStatisticsComputationTriggered.builder().jobId(jobId).build());
    } catch (IllegalStateException retriedLater) {
      // idem
    }
  }

  private static FeeStudentCreation studentCreationOf(
      List<FeeStudentCreation> students, String ref) {
    return students.stream()
        .filter(student -> ref.equals(student.getStudent().getRef()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No student creation for ref=" + ref));
  }

  @Test
  void submitted_job_is_pending_until_its_tasks_run() throws ApiException {
    var submitted = submitJob("job_pending_id", List.of("STD26010"));

    assertEquals("job_pending_id", submitted.getId());
    assertEquals(PENDING, submitted.getStatus().getProgression());
    assertEquals(UNKNOWN, submitted.getStatus().getHealth());
    assertEquals(FEE_TEMPLATE_ID, submitted.getFeeTemplate().getId());
    assertEquals(2, submitted.getFeeTemplate().getContents().size());
    assertEquals(1, submitted.getStatistics().getTotalCount());
    assertNull(submitted.getEndDatetime());

    var polled = managerApi().getFeeCreationJobById("job_pending_id");
    assertEquals(PENDING, polled.getStatus().getProgression());
    assertTrue(polled.getFailures().isEmpty());

    var students = managerApi().getFeeCreationJobStudents("job_pending_id");
    assertEquals(1, students.size());
    assertEquals(PENDING, students.getFirst().getFeesCreationStatus().getProgression());
    assertEquals(UNKNOWN, students.getFirst().getFeesCreationStatus().getHealth());
  }

  @Test
  void a_fully_processed_job_succeeds_and_creates_one_fee_per_content() throws ApiException {
    var studentRef = "STD21002";
    submitJob("job_succeeded_id", List.of(studentRef));

    drainTasksOf("job_succeeded_id", 1);

    var job = managerApi().getFeeCreationJobById("job_succeeded_id");
    assertEquals(FINISHED, job.getStatus().getProgression());
    assertEquals(SUCCEEDED, job.getStatus().getHealth());
    assertEquals(1, job.getStatistics().getTotalCount());
    assertEquals(1, job.getStatistics().getSuccessCount());
    assertEquals(0, job.getStatistics().getFailureCount());
    assertTrue(job.getFailures().isEmpty());
    assertNotNull(job.getEndDatetime());

    var students = managerApi().getFeeCreationJobStudents("job_succeeded_id");
    var student = studentCreationOf(students, studentRef);
    assertEquals(FINISHED, student.getFeesCreationStatus().getProgression());
    assertEquals(SUCCEEDED, student.getFeesCreationStatus().getHealth());
    assertNull(student.getMessage());
    assertNotNull(student.getStudent().getId());

    var studentId = userService.findByRef(studentRef).getId();
    var createdFees =
        feeRepository.findAll().stream()
            .filter(
                fee ->
                    studentId.equals(fee.getStudent().getId())
                        && fee.getFeeTemplate() != null
                        && FEE_TEMPLATE_ID.equals(fee.getFeeTemplate().getId()))
            .toList();
    assertEquals(2, createdFees.size());
    assertTrue(createdFees.stream().anyMatch(fee -> fee.getTotalAmount() == 5000));
    assertTrue(createdFees.stream().anyMatch(fee -> fee.getTotalAmount() == 6000));
  }

  @Test
  void an_unknown_student_ref_fails_its_task_and_the_whole_job() throws ApiException {
    submitJob("job_failed_id", List.of(UNKNOWN_STUDENT_REF));

    drainTasksOf("job_failed_id", 1);

    var job = managerApi().getFeeCreationJobById("job_failed_id");
    assertEquals(FINISHED, job.getStatus().getProgression());
    assertEquals(FAILED, job.getStatus().getHealth());
    assertEquals(0, job.getStatistics().getSuccessCount());
    assertEquals(1, job.getStatistics().getFailureCount());
    assertEquals(1, job.getFailures().size());
    assertEquals(UNKNOWN_STUDENT_REF, job.getFailures().getFirst().getStudentRef());
    assertTrue(job.getFailures().getFirst().getMessage().contains(UNKNOWN_STUDENT_REF));

    var students = managerApi().getFeeCreationJobStudents("job_failed_id");
    var student = studentCreationOf(students, UNKNOWN_STUDENT_REF);
    assertEquals(FINISHED, student.getFeesCreationStatus().getProgression());
    assertEquals(FAILED, student.getFeesCreationStatus().getHealth());
    assertNotNull(student.getMessage());
    // the reference names nobody, so there is no student to describe beyond it
    assertNull(student.getStudent().getId());
    assertNull(student.getStudent().getFirstName());
  }

  @Test
  void a_partly_failed_job_reports_each_task_on_its_own() throws ApiException {
    var succeedingRef = "STD21003";
    submitJob("job_mixed_id", List.of(succeedingRef, UNKNOWN_STUDENT_REF));

    drainTasksOf("job_mixed_id", 2);

    var job = managerApi().getFeeCreationJobById("job_mixed_id");
    assertEquals(FINISHED, job.getStatus().getProgression());
    // a single failure is enough to make the whole job unhealthy
    assertEquals(FAILED, job.getStatus().getHealth());
    assertEquals(2, job.getStatistics().getTotalCount());
    assertEquals(1, job.getStatistics().getSuccessCount());
    assertEquals(1, job.getStatistics().getFailureCount());
    assertEquals(1, job.getFailures().size());

    var students = managerApi().getFeeCreationJobStudents("job_mixed_id");
    assertEquals(2, students.size());
    assertEquals(
        SUCCEEDED, studentCreationOf(students, succeedingRef).getFeesCreationStatus().getHealth());
    assertEquals(
        FAILED,
        studentCreationOf(students, UNKNOWN_STUDENT_REF).getFeesCreationStatus().getHealth());
  }

  @Test
  void charging_twice_the_same_template_to_a_student_is_refused() throws ApiException {
    var studentRef = "STD21009";
    submitJob("job_first_charge_id", List.of(studentRef));
    drainTasksOf("job_first_charge_id", 1);
    assertEquals(
        SUCCEEDED,
        managerApi().getFeeCreationJobById("job_first_charge_id").getStatus().getHealth());

    submitJob("job_second_charge_id", List.of(studentRef));
    drainTasksOf("job_second_charge_id", 1);

    var job = managerApi().getFeeCreationJobById("job_second_charge_id");
    assertEquals(FINISHED, job.getStatus().getProgression());
    assertEquals(FAILED, job.getStatus().getHealth());
    assertTrue(job.getFailures().getFirst().getMessage().contains("already created"));

    var studentId = userService.findByRef(studentRef).getId();
    var feeCount =
        feeRepository.findAll().stream()
            .filter(
                fee ->
                    studentId.equals(fee.getStudent().getId())
                        && fee.getFeeTemplate() != null
                        && FEE_TEMPLATE_ID.equals(fee.getFeeTemplate().getId()))
            .count();
    // the second job did not add a single fee on top of the first one
    assertEquals(2, feeCount);
  }

  @Test
  void submitting_a_known_job_again_neither_duplicates_tasks_nor_redispatches()
      throws ApiException {
    submitJob("job_idempotent_id", List.of("STD21001"));
    drainTasksOf("job_idempotent_id", 1);

    var resubmitted = submitJob("job_idempotent_id", List.of("STD21001", "STD21002"));

    // the second submission returns the job as it stands, the extra reference is ignored
    assertEquals(FINISHED, resubmitted.getStatus().getProgression());
    assertEquals(1, resubmitted.getStatistics().getTotalCount());
    assertEquals(1, managerApi().getFeeCreationJobStudents("job_idempotent_id").size());
    // the resubmission dispatched nothing new: still the single task of the first submission
    assertEquals(
        1,
        dispatchedOfType(FeeCreationTaskTriggered.class).stream()
            .filter(event -> "job_idempotent_id".equals(event.getTask().getJob().getId()))
            .count());
  }

  @Test
  void jobs_are_listed_most_recent_first() throws ApiException {
    submitJob("job_listed_id", List.of("STD21001"));

    var listed = managerApi().getFeeCreationJobs(1, 10);

    assertTrue(listed.stream().anyMatch(job -> "job_listed_id".equals(job.getId())));
  }
}
