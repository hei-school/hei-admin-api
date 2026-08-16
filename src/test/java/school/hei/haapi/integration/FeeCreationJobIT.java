package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
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
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.V2FeeTemplateTestData.aTwoMonthsTemplate;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
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
import school.hei.haapi.model.User;
import school.hei.haapi.model.V2FeeTemplate;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.V2FeeTemplateRepository;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.FeeCreationJobStatisticsComputationTriggeredService;
import school.hei.haapi.service.event.FeeCreationJobStatusComputationTriggeredService;
import school.hei.haapi.service.event.FeeCreationTaskTriggeredService;

@Testcontainers
@AutoConfigureMockMvc
public class FeeCreationJobIT extends FacadeITMockedThirdParties {
  private static final int STUDENT_COUNT = 6;
  private static final String UNKNOWN_STUDENT_REF = "STD-unknown-" + randomUUID();

  @MockBean private EventProducer eventProducer;

  @Autowired private FeeCreationTaskTriggeredService taskConsumer;
  @Autowired private FeeCreationJobStatusComputationTriggeredService statusConsumer;
  @Autowired private FeeCreationJobStatisticsComputationTriggeredService statisticsConsumer;
  @Autowired private FeeRepository feeRepository;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private V2FeeTemplateRepository v2FeeTemplateRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  private User managerHasina;
  private V2FeeTemplate feeTemplate;
  private List<User> students;
  private String managerToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpS3Service(fileService, axel());
    managerHasina = userRepository.save(hasina());
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);

    feeTemplate = v2FeeTemplateRepository.save(aTwoMonthsTemplate());
    // the jobs address students by ref: the tests submit these students own refs
    students =
        userRepository.saveAll(IntStream.range(0, STUDENT_COUNT).mapToObj(i -> axel()).toList());
  }

  @AfterEach
  void tearDown() {
    var studentIds = students.stream().map(User::getId).toList();
    var placeholders = String.join(",", studentIds.stream().map(id -> "?").toList());
    // Fee carries @SQLDelete, so a repository delete would only flag is_deleted
    jdbcTemplate.update(
        "DELETE FROM \"fee_status_history\" WHERE fee_id IN (SELECT id FROM \"fee\" WHERE"
            + " user_id IN ("
            + placeholders
            + "))",
        studentIds.toArray());
    jdbcTemplate.update(
        "DELETE FROM \"fee\" WHERE user_id IN (" + placeholders + ")", studentIds.toArray());
    userRepository.deleteAllById(studentIds);

    // the jobs reference the template, and everything hangs off them, so they go first
    var jobsOfTemplate =
        "SELECT id FROM \"fee_creation_job\" WHERE id_fee_template = '" + feeTemplate.getId() + "'";
    jdbcTemplate.update(
        "DELETE FROM \"fee_creation_task_status\" WHERE id_fee_creation_task IN (SELECT id FROM"
            + " \"fee_creation_task\" WHERE id_fee_creation_job IN ("
            + jobsOfTemplate
            + "))");
    jdbcTemplate.update(
        "DELETE FROM \"fee_creation_task\" WHERE id_fee_creation_job IN (" + jobsOfTemplate + ")");
    jdbcTemplate.update(
        "DELETE FROM \"fee_creation_job_status\" WHERE id_fee_creation_job IN ("
            + jobsOfTemplate
            + ")");
    jdbcTemplate.update(
        "DELETE FROM \"fee_creation_job_statistics\" WHERE id_fee_creation_job IN ("
            + jobsOfTemplate
            + ")");
    jdbcTemplate.update(
        "DELETE FROM \"fee_creation_job\" WHERE id_fee_template = ?", feeTemplate.getId());

    v2FeeTemplateRepository.deleteById(feeTemplate.getId());
    userRepository.deleteById(managerHasina.getId());
  }

  /** The ref of one of this test's own students, so no fixed value is ever reused. */
  private String refOf(int index) {
    return students.get(index).getRef();
  }

  private PayingApi managerApi() {
    return new PayingApi(anApiClient(managerToken));
  }

  private FeeCreationJob submitJob(String jobId, List<String> studentRefs) throws ApiException {
    var submitted =
        managerApi()
            .crupdateFeeCreationJobs(
                List.of(
                    new CrupdateFeeCreationJob()
                        .id(jobId)
                        .feeTemplateId(feeTemplate.getId())
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
    var submitted = submitJob("job_pending_id", List.of(refOf(0)));

    assertEquals("job_pending_id", submitted.getId());
    assertEquals(PENDING, submitted.getStatus().getProgression());
    assertEquals(UNKNOWN, submitted.getStatus().getHealth());
    assertEquals(feeTemplate.getId(), submitted.getFeeTemplate().getId());
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
    var studentRef = refOf(1);
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
                        && feeTemplate.getId().equals(fee.getFeeTemplate().getId()))
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
    var succeedingRef = refOf(2);
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
    var studentRef = refOf(3);
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
                        && feeTemplate.getId().equals(fee.getFeeTemplate().getId()))
            .count();
    // the second job did not add a single fee on top of the first one
    assertEquals(2, feeCount);
  }

  @Test
  void submitting_a_known_job_again_neither_duplicates_tasks_nor_redispatches()
      throws ApiException {
    submitJob("job_idempotent_id", List.of(refOf(4)));
    drainTasksOf("job_idempotent_id", 1);

    var resubmitted = submitJob("job_idempotent_id", List.of(refOf(4), refOf(1)));

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
    submitJob("job_listed_id", List.of(refOf(4)));

    var listed = managerApi().getFeeCreationJobs(1, 10);

    assertTrue(listed.stream().anyMatch(job -> "job_listed_id".equals(job.getId())));
  }
}
