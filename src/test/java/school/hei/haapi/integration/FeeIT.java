package school.hei.haapi.integration;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.UNKNOWN;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.UNPAID;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.RETAKE_EXAM_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsApiException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.requestFile;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createFeeStatusHistory;
import static school.hei.haapi.integration.testData.FeeTestData.createFeeWithStatus;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.model.User.Status.DISABLED;
import static school.hei.haapi.model.User.Status.ENABLED;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AdvancedFeeStatisticsType;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.endpoint.rest.model.FeeStatusEnum;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FeeStatusHistory;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FeeStatusHistoryRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.FeeDao;

class FeeIT extends FacadeITMockedThirdParties {
  /** Every fee of this test is due inside this window, so filters can isolate them. */
  private static final Instant WINDOW_FROM = Instant.parse("2026-06-01T00:00:00.00Z");

  private static final Instant WINDOW_TO = Instant.parse("2026-06-30T23:59:59.00Z");

  /** Outside the window: used to assert a filter excludes what it should. */
  private static final Instant OUTSIDE_WINDOW = Instant.parse("2027-03-01T08:00:00Z");

  @Autowired EntityManager entityManager;
  @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
  @Autowired FeeRepository feeRepository;
  @Autowired FeeDao feeDao;
  @Autowired private UserRepository userRepository;
  @Autowired private FeeStatusHistoryRepository feeStatusHistoryRepository;
  @MockBean private BucketComponent bucketComponent;

  private User enabledStudentAxel;
  private User disabledStudentTolojanahary;
  private User studentFreddy;
  private User monitorAxel;
  private User managerHasina;
  private User teacherToky;

  private Fee axelFeePaid;
  private Fee axelFeeLate;
  private Fee axelFeePending;
  private Fee axelFeeUnpaid;
  private Fee axelFeeDeleted;
  private Fee freddyFeeLate;
  private Fee tolojanaharyFeeOutside1;
  private Fee tolojanaharyFeeOutside2;

  private final List<FeeStatusHistory> statusHistories = new ArrayList<>();

  /** Fees the tests create through the API, swept in tearDown. */
  private final List<String> createdFeeIds = new ArrayList<>();

  private String axelToken;
  private String monitorToken;
  private String managerToken;
  private String teacherToken;

  private void setUpTestData() {
    enabledStudentAxel = axel();
    enabledStudentAxel.setStatus(ENABLED);
    enabledStudentAxel = userRepository.save(enabledStudentAxel);

    disabledStudentTolojanahary = tolojanahary();
    disabledStudentTolojanahary.setStatus(DISABLED);
    disabledStudentTolojanahary = userRepository.save(disabledStudentTolojanahary);

    studentFreddy = userRepository.save(freddy());
    managerHasina = userRepository.save(hasina());
    teacherToky = userRepository.save(toky());

    monitorAxel = monitorOfAxel();
    monitorAxel.setMonitors(new ArrayList<>(List.of(enabledStudentAxel)));
    monitorAxel = userRepository.save(monitorAxel);

    axelFeePaid =
        createFeeWithStatus(
            enabledStudentAxel, 100_000, Instant.parse("2026-06-01T08:00:00Z"), PAID);
    axelFeeLate =
        createFeeWithStatus(
            enabledStudentAxel, 200_000, Instant.parse("2026-06-02T08:00:00Z"), LATE);
    axelFeePending =
        createFeeWithStatus(
            enabledStudentAxel, 400_000, Instant.parse("2026-06-15T08:00:00Z"), PENDING);
    axelFeeUnpaid =
        createFeeWithStatus(
            enabledStudentAxel, 400_000, Instant.parse("2026-06-20T08:00:00Z"), UNPAID);
    axelFeeDeleted =
        createPendingFee(enabledStudentAxel, 300_000, Instant.parse("2026-06-03T08:00:00Z"));
    axelFeeDeleted.setDeleted(true);
    freddyFeeLate =
        createFeeWithStatus(studentFreddy, 150_000, Instant.parse("2026-06-05T08:00:00Z"), LATE);
    tolojanaharyFeeOutside1 =
        createPendingFee(disabledStudentTolojanahary, 100_000, OUTSIDE_WINDOW);
    tolojanaharyFeeOutside2 =
        createPendingFee(disabledStudentTolojanahary, 200_000, OUTSIDE_WINDOW.plus(1, DAYS));

    feeRepository.saveAll(
        List.of(
            axelFeePaid,
            axelFeeLate,
            axelFeePending,
            axelFeeUnpaid,
            axelFeeDeleted,
            freddyFeeLate,
            tolojanaharyFeeOutside1,
            tolojanaharyFeeOutside2));

    addStatusHistory(axelFeePaid, PAID);
    addStatusHistory(axelFeeLate, LATE);
    addStatusHistory(axelFeePending, PENDING);
    addStatusHistory(axelFeeUnpaid, UNPAID);
    addStatusHistory(freddyFeeLate, LATE);
    addStatusHistory(tolojanaharyFeeOutside1, PAID);
    addStatusHistory(tolojanaharyFeeOutside2, LATE);
    feeStatusHistoryRepository.saveAll(statusHistories);
  }

  private void addStatusHistory(Fee fee, FeeStatusEnum status) {
    var history = createFeeStatusHistory(fee, status);
    fee.getStatusHistories().add(history);
    statusHistories.add(history);
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, enabledStudentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, enabledStudentAxel);
    monitorToken = tokenFor(casdoorAuthServiceMock, monitorAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
  }

  @AfterEach
  void tearDown() {
    statusHistories.clear();

    List<String> ownedFeeIds = new ArrayList<>(createdFeeIds);
    ownedFeeIds.addAll(
        List.of(
            axelFeePaid.getId(),
            axelFeeLate.getId(),
            axelFeePending.getId(),
            axelFeeUnpaid.getId(),
            axelFeeDeleted.getId(),
            freddyFeeLate.getId(),
            tolojanaharyFeeOutside1.getId(),
            tolojanaharyFeeOutside2.getId()));
    // Fee carries @SQLDelete, so a repository delete only flags is_deleted and the rows would pile
    // up: the cleanup has to reach the table directly. Histories go first, and by fee id rather
    // than by reference — the service adds its own on every status change.
    var placeholders = String.join(",", ownedFeeIds.stream().map(id -> "?").toList());
    jdbcTemplate.update(
        "DELETE FROM \"fee_status_history\" WHERE fee_id IN (" + placeholders + ")",
        ownedFeeIds.toArray());
    jdbcTemplate.update(
        "DELETE FROM \"payment\" WHERE fee_id IN (" + placeholders + ")", ownedFeeIds.toArray());
    jdbcTemplate.update(
        "DELETE FROM \"fee\" WHERE id IN (" + placeholders + ")", ownedFeeIds.toArray());
    createdFeeIds.clear();

    monitorAxel.setMonitors(new ArrayList<>());
    userRepository.save(monitorAxel);
    userRepository.deleteAll(
        List.of(
            enabledStudentAxel,
            disabledStudentTolojanahary,
            studentFreddy,
            monitorAxel,
            managerHasina,
            teacherToky));
  }

  /** Reads a fee bypassing the JPA {@code isDeleted} filter. */
  private Fee getFeeByIdWithoutJpaFiltering(String feeId) {
    var q = entityManager.createNativeQuery("SELECT * FROM \"fee\" where id = ?", Fee.class);
    q.setParameter(1, feeId);
    return (Fee) q.getSingleResult();
  }

  private PayingApi apiAs(String token) {
    return new PayingApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static List<String> idsOf(List<school.hei.haapi.endpoint.rest.model.Fee> fees) {
    return fees.stream().map(fee -> fee.getId()).toList();
  }

  private static CreateFee aCreatableFee() {
    return new CreateFee()
        .type(TUITION)
        .totalAmount(5000)
        .category(UNKNOWN)
        .frequency(FeeFrequency.UNKNOWN)
        .comment("Comment")
        .dueDatetime(Instant.parse("2026-06-10T08:25:24.00Z"));
  }

  private school.hei.haapi.endpoint.rest.model.Fee createFeeThroughApi(User student)
      throws ApiException {
    var created =
        apiAs(managerToken).createStudentFees(student.getId(), List.of(aCreatableFee())).getFirst();
    createdFeeIds.add(created.getId());
    return created;
  }

  @Test
  void getStudentFeesByStudentId_areSorted_withPendingFirst_thenLate_thenUnpaid_thenPaid()
      throws ApiException {
    var actualStatusOrder =
        apiAs(managerToken).getFeesByStudentId(enabledStudentAxel.getId(), 1, 50, null).stream()
            .map(fee -> fee.getStatus())
            .toList();

    assertThat(actualStatusOrder).containsSequence(PENDING, LATE, UNPAID, PAID);
  }

  @Test
  void manager_delete_ok() throws ApiException {
    var api = apiAs(managerToken);
    var createdFee = createFeeThroughApi(enabledStudentAxel);

    var deletedFee = api.deleteStudentFeeById(createdFee.getId(), enabledStudentAxel.getId());

    var fees = api.getFeesByStudentId(enabledStudentAxel.getId(), 1, 50, null);
    assertFalse(idsOf(fees).contains(deletedFee.getId()));
    assertTrue(getFeeByIdWithoutJpaFiltering(deletedFee.getId()).isDeleted());
  }

  @Test
  void student_read_ok() throws ApiException {
    var api = apiAs(axelToken);

    var actualFee = api.getStudentFeeById(enabledStudentAxel.getId(), axelFeePaid.getId());
    var actual = api.getFeesByStudentId(enabledStudentAxel.getId(), 1, 50, null);
    var lateFees = api.getFeesByStudentId(enabledStudentAxel.getId(), 1, 50, LATE);

    assertEquals(axelFeePaid.getId(), actualFee.getId());
    assertTrue(idsOf(actual).contains(axelFeePaid.getId()));
    assertTrue(idsOf(actual).contains(axelFeeLate.getId()));
    assertFalse(idsOf(actual).contains(axelFeeDeleted.getId()));
    assertEquals(List.of(axelFeeLate.getId()), idsOf(lateFees));
  }

  @Test
  void monitor_read_own_followed_student_ok() throws ApiException {
    var api = apiAs(monitorToken);

    var actualFee = api.getStudentFeeById(enabledStudentAxel.getId(), axelFeePaid.getId());
    var actual = api.getFeesByStudentId(enabledStudentAxel.getId(), 1, 50, null);

    assertEquals(axelFeePaid.getId(), actualFee.getId());
    assertTrue(idsOf(actual).contains(axelFeeLate.getId()));
  }

  @Test
  void read_fee_contains_student_first_name() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getFees(null, null, null, null, WINDOW_FROM, WINDOW_TO, 1, 50, false, null);

    assertNotNull(actual.getData().getFirst().getStudentFirstName());
  }

  @Test
  void manager_read_ok() throws ApiException {
    var api = apiAs(managerToken);

    var actualFee = api.getStudentFeeById(enabledStudentAxel.getId(), axelFeePaid.getId());
    var axelFees = api.getFeesByStudentId(enabledStudentAxel.getId(), 1, 50, null);
    var paidInWindow =
        api.getFees(null, null, PAID, null, WINDOW_FROM, WINDOW_TO, 1, 50, false, null);

    assertEquals(axelFeePaid.getId(), actualFee.getId());
    assertTrue(idsOf(axelFees).contains(axelFeePaid.getId()));
    assertTrue(idsOf(paidInWindow.getData()).contains(axelFeePaid.getId()));
    assertFalse(idsOf(paidInWindow.getData()).contains(axelFeeLate.getId()));

    var freddyFees =
        api.getFees(
            null, null, null, null, WINDOW_FROM, WINDOW_TO, 1, 50, false, studentFreddy.getRef());
    assertEquals(List.of(freddyFeeLate.getId()), idsOf(freddyFees.getData()));
  }

  @Test
  void student_read_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(
        () -> api.getStudentFeeById(studentFreddy.getId(), freddyFeeLate.getId()));
    assertThrowsForbiddenException(
        () -> api.getFeesByStudentId(studentFreddy.getId(), null, null, null));
    assertThrowsForbiddenException(
        () -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  void monitor_read_other_student_ko() {
    var api = apiAs(monitorToken);

    assertThrowsForbiddenException(
        () -> api.getStudentFeeById(studentFreddy.getId(), freddyFeeLate.getId()));
    assertThrowsForbiddenException(
        () -> api.getFeesByStudentId(studentFreddy.getId(), null, null, null));
    assertThrowsForbiddenException(
        () -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  void teacher_read_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(
        () -> api.getStudentFeeById(studentFreddy.getId(), freddyFeeLate.getId()));
    assertThrowsForbiddenException(
        () -> api.getFeesByStudentId(studentFreddy.getId(), null, null, null));
    assertThrowsForbiddenException(
        () -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  void student_write_ok() throws ApiException {
    var api = apiAs(axelToken);

    var retakeExamFee = aCreatableFee().type(RETAKE_EXAM_COSTS);
    var retakeCreated = api.createStudentFees(enabledStudentAxel.getId(), List.of(retakeExamFee));
    retakeCreated.forEach(f -> createdFeeIds.add(f.getId()));
    assertEquals(RETAKE_EXAM_COSTS, retakeCreated.getFirst().getType());

    var tuitionCreated =
        api.createStudentFees(enabledStudentAxel.getId(), List.of(aCreatableFee()));
    tuitionCreated.forEach(f -> createdFeeIds.add(f.getId()));
    assertEquals(TUITION, tuitionCreated.getFirst().getType());
  }

  @Test
  void student_write_other_ko() {
    var api = apiAs(axelToken);
    var createFee = aCreatableFee().type(RETAKE_EXAM_COSTS);

    assertThrowsForbiddenException(
        () -> api.createStudentFees(studentFreddy.getId(), List.of(createFee)));
  }

  @Test
  void manager_write_ok() throws ApiException {
    var api = apiAs(managerToken);
    var createdFee = createFeeThroughApi(enabledStudentAxel);

    var updatedFee =
        createdFee.comment("M1 + M2 + M3").dueDatetime(Instant.parse("2026-06-09T10:10:10.00Z"));
    var actualUpdated = api.updateStudentFees(enabledStudentAxel.getId(), List.of(updatedFee));

    assertEquals(1, actualUpdated.size());
    assertEquals(updatedFee.getComment(), actualUpdated.getFirst().getComment());
    assertEquals(updatedFee.getDueDatetime(), actualUpdated.getFirst().getDueDatetime());
  }

  @Test
  void monitor_write_ko() {
    var api = apiAs(monitorToken);
    var feeUpdated =
        new school.hei.haapi.endpoint.rest.model.Fee()
            .id(axelFeePaid.getId())
            .comment("new comment")
            .dueDatetime(WINDOW_FROM);

    assertThrowsForbiddenException(
        () -> api.updateStudentFees(enabledStudentAxel.getId(), List.of(feeUpdated)));
    assertThrowsForbiddenException(
        () -> api.createStudentFees(enabledStudentAxel.getId(), List.of()));
  }

  @Test
  void teacher_write_ko() {
    var api = apiAs(teacherToken);
    var feeUpdated =
        new school.hei.haapi.endpoint.rest.model.Fee()
            .id(axelFeePaid.getId())
            .comment("new comment")
            .dueDatetime(WINDOW_FROM);

    assertThrowsForbiddenException(
        () -> api.updateStudentFees(enabledStudentAxel.getId(), List.of(feeUpdated)));
    assertThrowsForbiddenException(
        () -> api.createStudentFees(enabledStudentAxel.getId(), List.of()));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    var api = apiAs(managerToken);
    var studentId = enabledStudentAxel.getId();
    var wrongId = "some-wrong-id";
    var before = api.getFeesByStudentId(studentId, 1, 50, null);
    var existing = api.getStudentFeeById(studentId, axelFeePaid.getId());

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Total amount is mandatory\"}",
        () -> api.createStudentFees(studentId, List.of(aCreatableFee().totalAmount(null))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Total amount must be positive\"}",
        () -> api.createStudentFees(studentId, List.of(aCreatableFee().totalAmount(-1))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Due datetime is mandatory\"}",
        () -> api.createStudentFees(studentId, List.of(aCreatableFee().dueDatetime(null))));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Id is mandatory\"}",
        () -> api.updateStudentFees(studentId, List.of(copyOf(existing).id(null))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify Type\"}",
        () -> api.updateStudentFees(studentId, List.of(copyOf(existing).type(HARDWARE))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify remainingAmount\"}",
        () -> api.updateStudentFees(studentId, List.of(copyOf(existing).remainingAmount(10))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify totalAmount\"}",
        () -> api.updateStudentFees(studentId, List.of(copyOf(existing).totalAmount(10))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify creationDatetime\"}",
        () ->
            api.updateStudentFees(
                studentId,
                List.of(copyOf(existing).creationDatetime(Instant.parse("2026-06-09T10:10:10Z")))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Fee with"
            + " id "
            + wrongId
            + " does not exist\"}",
        () -> api.updateStudentFees(studentId, List.of(copyOf(existing).id(wrongId))));

    var after = api.getFeesByStudentId(studentId, 1, 50, null);
    assertEquals(before.size(), after.size());
  }

  private static school.hei.haapi.endpoint.rest.model.Fee copyOf(
      school.hei.haapi.endpoint.rest.model.Fee fee) {
    return new school.hei.haapi.endpoint.rest.model.Fee()
        .id(fee.getId())
        .studentId(fee.getStudentId())
        .status(fee.getStatus())
        .type(fee.getType())
        .totalAmount(fee.getTotalAmount())
        .remainingAmount(fee.getRemainingAmount())
        .comment(fee.getComment())
        .creationDatetime(fee.getCreationDatetime())
        .dueDatetime(fee.getDueDatetime());
  }

  @Test
  void get_fees_by_criteria_ok() throws ApiException {
    var api = apiAs(managerToken);

    var inWindow = api.getFees(null, null, null, null, WINDOW_FROM, WINDOW_TO, 1, 50, false, null);
    assertTrue(idsOf(inWindow.getData()).contains(axelFeePaid.getId()));
    assertTrue(idsOf(inWindow.getData()).contains(axelFeeLate.getId()));
    assertTrue(idsOf(inWindow.getData()).contains(freddyFeeLate.getId()));
    assertFalse(idsOf(inWindow.getData()).contains(tolojanaharyFeeOutside1.getId()));
    assertFalse(idsOf(inWindow.getData()).contains(axelFeeDeleted.getId()));

    var lateInWindow =
        api.getFees(null, null, LATE, null, WINDOW_FROM, WINDOW_TO, 1, 50, false, null);
    assertTrue(idsOf(lateInWindow.getData()).contains(axelFeeLate.getId()));
    assertTrue(idsOf(lateInWindow.getData()).contains(freddyFeeLate.getId()));
    assertFalse(idsOf(lateInWindow.getData()).contains(axelFeePaid.getId()));

    var lateInWindowForFreddy =
        api.getFees(
            null, null, LATE, null, WINDOW_FROM, WINDOW_TO, 1, 50, false, studentFreddy.getRef());
    assertEquals(List.of(freddyFeeLate.getId()), idsOf(lateInWindowForFreddy.getData()));
  }

  @Test
  void get_fees_statistics_ok() throws ApiException {
    var api = apiAs(managerToken);

    var stats = api.getFeesStats(WINDOW_FROM, WINDOW_TO);
    var listed = api.getFees(null, null, null, null, WINDOW_FROM, WINDOW_TO, 1, 500, false, null);
    var paid = api.getFees(null, null, PAID, null, WINDOW_FROM, WINDOW_TO, 1, 500, false, null);

    // the stats endpoint must agree with the list endpoint over the same window
    assertEquals(listed.getData().size(), stats.getTotalFees());
    assertEquals(paid.getData().size(), stats.getPaidFees());
  }

  @Test
  void manager_generate_advanced_fee_statistics_ok() {
    var fromDateTime = LocalDateTime.parse("2026-06-01T00:00:00.00");
    var toDateTime = LocalDateTime.parse("2026-06-30T23:59:59.99");

    assertDoesNotThrow(
        () ->
            apiAs(managerToken)
                .generateAdvancedStats(fromDateTime.toInstant(UTC), toDateTime.toInstant(UTC)));
  }

  @Test
  void generate_fees_list_as_xlsx_without_parameters_ok() throws IOException, InterruptedException {
    var response =
        requestFile(URI.create("http://localhost:" + localPort + "/fees/raw"), managerToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void generate_fees_list_as_xlsx_with_parameters_ok() throws IOException, InterruptedException {
    var withStatus =
        requestFile(
            URI.create("http://localhost:" + localPort + "/fees/raw?status=" + PENDING),
            managerToken);
    assertEquals(HttpStatus.OK.value(), withStatus.statusCode());

    var withDateStart =
        requestFile(
            URI.create(
                "http://localhost:"
                    + localPort
                    + "/fees/raw?from_due_datetime=2026-01-01T12:00:00.000Z"),
            managerToken);
    assertEquals(HttpStatus.OK.value(), withDateStart.statusCode());

    var withDateRange =
        requestFile(
            URI.create(
                "http://localhost:"
                    + localPort
                    + "/fees/raw?from_due_datetime=2026-01-01T12:00:00Z&to_due_datetime=2026-12-31T12:00:00Z"),
            managerToken);
    assertEquals(HttpStatus.OK.value(), withDateRange.statusCode());
  }

  @Test
  void all_fee_without_status_and_dueDatetime_work() {
    var realFees = feeRepository.findAll();
    var fees = feeDao.findAllByStatusAndDueDatetimeBetween(null, null, null);

    assertEquals(realFees.size(), fees.size());
  }

  @Test
  void all_fee_by_status_and_dueDatetime_in_date_range_must_contain_some_fee() {
    var fees = feeDao.findAllByStatusAndDueDatetimeBetween(null, WINDOW_FROM, WINDOW_TO);

    assertFalse(fees.isEmpty());
  }

  @Test
  void manager_request_advanced_fee_stats_generation_ok() {
    var api = apiAs(managerToken);

    assertDoesNotThrow(() -> api.generateAdvancedStats(WINDOW_FROM, WINDOW_TO));
  }

  @Test
  void student_request_advanced_fee_stats_generation_ko() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(
        () -> api.generateAdvancedStats(now().toInstant(UTC).minus(7, DAYS), now().toInstant(UTC)));
  }

  @Test
  void generate_raw_fees_OK() throws ApiException {
    when(bucketComponent.presign(any(), any()))
        .thenAnswer(invocation -> new URL("https://example.com/file.xlsx"));
    when(bucketComponent.upload(any(), any())).thenReturn(mock());

    var url =
        apiAs(managerToken)
            .exportAllFees(AdvancedFeeStatisticsType.ACCOUNTING, WINDOW_FROM, WINDOW_TO);

    assertNotNull(url);
  }

  @Test
  void manager_read_by_category_ok() throws ApiException {
    var actual =
        apiAs(managerToken)
            .getFees(null, null, null, UNKNOWN, WINDOW_FROM, WINDOW_TO, 1, 50, false, null);

    // every fee of this test carries the UNKNOWN category
    assertTrue(idsOf(actual.getData()).contains(axelFeePaid.getId()));
    assertTrue(idsOf(actual.getData()).contains(freddyFeeLate.getId()));
  }

  @Test
  void findAllByEnabledByDueDatetimeBetween_ok() {
    var all2026Fees = feeRepository.findAllByDueDatetimeBetween(WINDOW_FROM, WINDOW_TO);

    var idsInWindow = all2026Fees.stream().map(Fee::getId).toList();
    assertTrue(idsInWindow.contains(axelFeePaid.getId()));
    assertTrue(idsInWindow.contains(axelFeeLate.getId()));
    assertFalse(idsInWindow.contains(axelFeeDeleted.getId()));
    assertFalse(idsInWindow.contains(tolojanaharyFeeOutside1.getId()));
    assertFalse(idsInWindow.contains(tolojanaharyFeeOutside2.getId()));
  }

  @Test
  void findAllEnabledByStatusHistoriesBetween_ok() {
    var from = Instant.parse("2026-01-01T00:00:00Z");
    var to = Instant.parse("2026-12-31T23:59:59Z");

    var withHistories = feeRepository.findDistinctByStatusHistoriesDatetimeBetween(from, to);

    assertFalse(withHistories.stream().map(Fee::getId).toList().contains(axelFeeDeleted.getId()));
  }
}
