package school.hei.haapi.integration;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.LATE;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.REMEDIAL_COSTS;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.FEE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE2_ID;
import static school.hei.haapi.integration.conf.TestUtils.FEE3_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MONITOR1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsApiException;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.creatableFee1;
import static school.hei.haapi.integration.conf.TestUtils.creatableStudentFee;
import static school.hei.haapi.integration.conf.TestUtils.createFeeForTest;
import static school.hei.haapi.integration.conf.TestUtils.fee1;
import static school.hei.haapi.integration.conf.TestUtils.fee2;
import static school.hei.haapi.integration.conf.TestUtils.fee3;
import static school.hei.haapi.integration.conf.TestUtils.fee4;
import static school.hei.haapi.integration.conf.TestUtils.requestFile;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.FeeTestData.createFeeStatusHistory;
import static school.hei.haapi.integration.test_data.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.model.User.Status.DISABLED;
import static school.hei.haapi.model.User.Status.ENABLED;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.AdvancedFeeStatisticsGeneration;
import school.hei.haapi.endpoint.rest.model.AdvancedFeeStatisticsType;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.FeesStatistics;
import school.hei.haapi.endpoint.rest.model.FeesWithStats;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.FeeStatusHistoryRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.FeeDao;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class FeeIT extends FacadeITMockedThirdParties {
  @Autowired EntityManager entityManager;
  @Autowired FeeRepository feeRepository;
  @MockBean private BucketComponent bucketComponent;
  @Autowired FeeDao feeDao;

  private User enabledStudentAxel;
  private User disabledStudentTolojanahary;
  private school.hei.haapi.model.Fee axelFee1;
  private school.hei.haapi.model.Fee axelFee2;
  private school.hei.haapi.model.Fee axelFeeDeleted;
  private school.hei.haapi.model.Fee tolojanaharyFee1;
  private school.hei.haapi.model.Fee tolojanaharyFee2;

  @Autowired private UserRepository userRepository;
  @Autowired private FeeStatusHistoryRepository feeStatusHistoryRepository;

  void setUpTestData() {
    enabledStudentAxel = axel();
    enabledStudentAxel.setStatus(ENABLED);
    disabledStudentTolojanahary = tolojanahary();
    disabledStudentTolojanahary.setStatus(DISABLED);

    axelFee1 = createPendingFee(enabledStudentAxel, 100_000, Instant.parse("2026-06-01T08:00:00Z"));
    axelFee2 = createPendingFee(enabledStudentAxel, 200_000, Instant.parse("2026-06-02T08:00:00Z"));
    axelFeeDeleted =
        createPendingFee(enabledStudentAxel, 300_000, Instant.parse("2026-06-03T08:00:00Z"));
    axelFeeDeleted.setDeleted(true);
    tolojanaharyFee1 =
        createPendingFee(
            disabledStudentTolojanahary, 100_000, Instant.parse("2026-06-01T08:00:00Z"));
    tolojanaharyFee2 =
        createPendingFee(
            disabledStudentTolojanahary, 200_000, Instant.parse("2026-06-02T08:00:00Z"));

    var axelFee1history = createFeeStatusHistory(axelFee1, PAID);
    var axelFee2history = createFeeStatusHistory(axelFee2, LATE);
    var tolojanaharyFee1history = createFeeStatusHistory(tolojanaharyFee1, PAID);
    var tolojanaharyFee2history = createFeeStatusHistory(tolojanaharyFee2, LATE);

    userRepository.saveAll(List.of(enabledStudentAxel, disabledStudentTolojanahary));
    feeRepository.saveAll(
        List.of(axelFee1, axelFee2, axelFeeDeleted, tolojanaharyFee1, tolojanaharyFee2));
    axelFee1.getStatusHistories().add(axelFee1history);
    axelFee2.getStatusHistories().add(axelFee2history);
    tolojanaharyFee1.getStatusHistories().add(tolojanaharyFee1history);
    tolojanaharyFee2.getStatusHistories().add(tolojanaharyFee2history);
    feeStatusHistoryRepository.saveAll(
        List.of(
            axelFee1history, axelFee2history, tolojanaharyFee1history, tolojanaharyFee2history));
  }

  // TODO: add @AfterEach when we are ready to fully migrate this test using FacadeIT and a real
  // Postgres database
  void teardown() {
    feeRepository.deleteAll(
        List.of(axelFee1, axelFee2, axelFeeDeleted, tolojanaharyFee1, tolojanaharyFee2));
    userRepository.deleteAll(List.of(enabledStudentAxel, disabledStudentTolojanahary));
  }

  /***
   * Get fee by id without jpa, avoiding FILTER isDeleted = true | false
   * @param feeId
   * @return Fee data by id
   */
  private school.hei.haapi.model.Fee getFeeByIdWithoutJpaFiltering(String feeId) {
    try {
      Query q =
          entityManager.createNativeQuery(
              "SELECT * FROM \"fee\" where id = ?", school.hei.haapi.model.Fee.class);
      q.setParameter(1, feeId);
      return (school.hei.haapi.model.Fee) q.getSingleResult();
    } catch (NullPointerException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void manager_delete_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Fee createdFee = api.createStudentFees(STUDENT1_ID, List.of(createFeeForTest())).getFirst();

    Fee deletedFee = api.deleteStudentFeeById(createdFee.getId(), STUDENT1_ID);

    List<Fee> fees = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    assertFalse(fees.contains(deletedFee));

    school.hei.haapi.model.Fee actualFeeData = getFeeByIdWithoutJpaFiltering(deletedFee.getId());
    assertTrue(actualFeeData.isDeleted());
  }

  @Test
  void student_read_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    Fee actualFee = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    Fee test = api.getStudentFeeById(STUDENT1_ID, FEE3_ID);

    assertEquals(test, fee3());

    List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 20, null);
    List<Fee> lateFees = api.getStudentFees(STUDENT1_ID, 1, 20, LATE);

    assertEquals(fee1(), actualFee);
    assertTrue(actual.contains(fee1()));
    assertTrue(actual.contains(fee2()));
    assertTrue(actual.contains(fee3()));
    assertTrue(lateFees.contains(fee3()));
  }

  @Test
  void monitor_read_own_followed_student_ok() throws ApiException {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);
    PayingApi api = new PayingApi(monitor1Client);

    Fee actualFee = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 10, null);

    assertEquals(fee1(), actualFee);
    assertTrue(actual.contains(fee1()));
    assertTrue(actual.contains(fee2()));
    assertTrue(actual.contains(fee3()));
  }

  @Test
  void manager_read_fee_paid_by_mpbs() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    FeesWithStats actual =
        api.getFees(null, null, null, null, fee1().getCreationDatetime(), null, 1, 10, true, null);
    assertEquals(2, actual.getData().size());
  }

  @Test
  void read_fee_contains_student_first_name() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    FeesWithStats actual =
        api.getFees(null, null, null, null, fee1().getCreationDatetime(), null, 1, 10, true, null);
    assertNotNull(actual.getData().getFirst().getStudentFirstName());
  }

  @Test
  void manager_read_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Fee actualFee = api.getStudentFeeById(STUDENT1_ID, FEE1_ID);
    List<Fee> actualFees1 = api.getStudentFees(STUDENT1_ID, 1, 20, null);
    FeesWithStats actualFees2 =
        api.getFees(null, null, PAID, null, fee1().getCreationDatetime(), null, 1, 10, false, null);

    assertEquals(fee1(), actualFee);
    assertEquals(2, actualFees2.getData().size());
    assertTrue(actualFees1.contains(fee1()));
    assertTrue(actualFees1.contains(fee2()));
    assertTrue(actualFees1.contains(fee3()));
    assertTrue(actualFees2.getData().contains(fee1()));
    assertTrue(actualFees2.getData().contains(fee2()));

    FeesWithStats student2Fees =
        api.getFees(null, null, null, null, fee4().getDueDatetime(), null, 1, 5, false, "STD21002");
    assertEquals(student2Fees.getData().getFirst(), fee4());
    assertFalse(student2Fees.getData().contains(fee1()));
    assertFalse(student2Fees.getData().contains(fee2()));
    assertFalse(student2Fees.getData().contains(fee3()));
  }

  @Test
  void student_read_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeeById(STUDENT2_ID, FEE2_ID));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFees(STUDENT2_ID, null, null, null));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  void monitor_read_other_student_ko() {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);
    PayingApi api = new PayingApi(monitor1Client);

    assertThrowsForbiddenException(() -> api.getStudentFeeById(STUDENT2_ID, FEE2_ID));
    assertThrowsForbiddenException(() -> api.getStudentFees(STUDENT2_ID, null, null, null));
    assertThrowsForbiddenException(
        () -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  void teacher_read_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFeeById(STUDENT2_ID, FEE2_ID));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getStudentFees(STUDENT2_ID, null, null, null));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.getFees(null, null, null, null, null, null, 1, 10, false, null));
  }

  @Test
  @Disabled("It dirties the other tests")
  void student_write_ok() throws ApiException {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    CreateFee createFee = creatableFee1();
    createFee.setType(REMEDIAL_COSTS);

    List<Fee> actualFee0 = api.createStudentFees(STUDENT1_ID, List.of(createFee));

    assertEquals(REMEDIAL_COSTS, actualFee0.getFirst().getType());

    List<Fee> actualFee1 = api.createStudentFees(STUDENT1_ID, List.of(creatableFee1()));

    assertEquals(TUITION, actualFee1.getFirst().getType());
  }

  @Test
  void student_write_other_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);
    CreateFee createFee = creatableFee1();
    createFee.setType(REMEDIAL_COSTS);

    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT2_ID, List.of(createFee)));
  }

  @Test
  @Disabled("It dirties the other tests")
  void manager_write_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    Fee createdFee = api.createStudentFees(STUDENT1_ID, List.of(createFeeForTest())).getFirst();

    Fee updatedFee =
        createdFee.comment("M1 + M2 + M3").dueDatetime(Instant.parse("2021-11-09T10:10:10.00Z"));

    List<Fee> actualUpdated = api.updateStudentFees(STUDENT1_ID, List.of(updatedFee));

    assertEquals(1, actualUpdated.size());
    assertEquals(actualUpdated.getFirst().getComment(), updatedFee.getComment());
    assertEquals(actualUpdated.getFirst().getDueDatetime(), updatedFee.getDueDatetime());

    List<Fee> crupdatedStudentFees = api.crupdateStudentFees(List.of(creatableStudentFee()));

    List<Fee> student1Fees = api.getStudentFees(STUDENT1_ID, 1, 10, null);

    assertEquals(1, crupdatedStudentFees.size());
    assertTrue(student1Fees.contains(crupdatedStudentFees.getFirst()));
  }

  @Test
  void monitor_write_ko() {
    ApiClient monitor1Client = anApiClient(MONITOR1_TOKEN);
    PayingApi api = new PayingApi(monitor1Client);
    Fee feeUpdated =
        fee1().comment("nex comment").dueDatetime(Instant.parse("2021-11-09T10:10:10.00Z"));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(feeUpdated)));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of()));
  }

  @Test
  void teacher_write_ko() {
    ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
    PayingApi api = new PayingApi(teacher1Client);
    Fee feeUpdated =
        fee1().comment("nex comment").dueDatetime(Instant.parse("2021-11-09T10:10:10.00Z"));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(feeUpdated)));
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of()));
  }

  @Test
  void manager_write_with_some_bad_fields_ko() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);
    String wrongId = "some-wrong-id";
    List<Fee> expected = api.getStudentFees(STUDENT1_ID, 1, 5, null);

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Total amount is mandatory\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of(creatableFee1().totalAmount(null))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Total amount must be positive\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of(creatableFee1().totalAmount(-1))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Due datetime is mandatory\"}",
        () -> api.createStudentFees(STUDENT1_ID, List.of(creatableFee1().dueDatetime(null))));

    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Id is mandatory\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().id(null))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify Type\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().type(HARDWARE))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify remainingAmount\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().remainingAmount(10))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify totalAmount\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().totalAmount(10))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Can't modify creationDatetime\"}",
        () ->
            api.updateStudentFees(
                STUDENT1_ID,
                List.of(fee1().creationDatetime(Instant.parse("2021-11-09T10:10:10.00Z")))));
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"Fee with id " + wrongId + " does not exist\"}",
        () -> api.updateStudentFees(STUDENT1_ID, List.of(fee1().id(wrongId))));

    List<Fee> actual = api.getStudentFees(STUDENT1_ID, 1, 5, null);
    assertEquals(expected.size(), actual.size());
  }

  @Test
  void get_fees_by_criteria_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    FeesWithStats feeByMonth =
        api.getFees(
            null,
            null,
            null,
            null,
            Instant.parse("2021-12-01T00:00:00.00Z"),
            Instant.parse("2021-12-31T23:59:59.00Z"),
            1,
            10,
            false,
            null);
    assertEquals(10, feeByMonth.getData().size());
    assertTrue(feeByMonth.getData().contains(fee1()));
    assertTrue(feeByMonth.getData().contains(fee2()));
    assertTrue(feeByMonth.getData().contains(fee3()));
    assertTrue(feeByMonth.getData().contains(fee4()));

    FeesWithStats noFeeByMonth =
        api.getFees(
            null,
            null,
            null,
            null,
            Instant.parse("2021-10-01T00:00:00.00Z"),
            Instant.parse("2021-10-31T23:59:59.00Z"),
            1,
            10,
            false,
            null);
    assertEquals(0, noFeeByMonth.getData().size());

    FeesWithStats feeByStatusLateAndMonth =
        api.getFees(
            null,
            null,
            LATE,
            null,
            Instant.parse("2021-12-01T00:00:00.00Z"),
            Instant.parse("2021-12-31T23:59:59.00Z"),
            1,
            10,
            false,
            null);
    assertTrue(feeByStatusLateAndMonth.getData().contains(fee3()));
    assertTrue(feeByStatusLateAndMonth.getData().contains(fee4()));

    FeesWithStats feeByStatusPaidAndMonth =
        api.getFees(
            null,
            null,
            PAID,
            null,
            Instant.parse("2021-12-01T00:00:00.00Z"),
            Instant.parse("2021-12-31T23:59:59.00Z"),
            1,
            10,
            false,
            null);
    assertEquals(2, feeByStatusPaidAndMonth.getData().size());
    assertTrue(feeByStatusPaidAndMonth.getData().contains(fee1()));
    assertTrue(feeByStatusPaidAndMonth.getData().contains(fee2()));

    FeesWithStats feeByStatusLateAndMonthAndStudentRef =
        api.getFees(
            null,
            null,
            LATE,
            null,
            Instant.parse("2021-12-01T00:00:00.00Z"),
            Instant.parse("2021-12-31T23:59:59.00Z"),
            1,
            10,
            false,
            "STD21002");
    assertEquals(2, feeByStatusLateAndMonthAndStudentRef.getData().size());
    assertTrue(feeByStatusLateAndMonthAndStudentRef.getData().contains(fee4()));

    FeesWithStats feeIsMpbsByMonth =
        api.getFees(
            null,
            null,
            null,
            null,
            Instant.parse("2021-12-01T00:00:00.00Z"),
            Instant.parse("2021-12-31T23:59:59.00Z"),
            1,
            10,
            true,
            null);
    assertEquals(feeIsMpbsByMonth.getData().getLast(), fee1());
  }

  @Test
  void get_fees_statistics_ok() throws ApiException {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    FeesStatistics stats =
        api.getFeesStats(
            Instant.parse("2021-12-01T00:00:00.00Z"), Instant.parse("2021-12-31T00:00:00.00Z"));
    assertEquals(10, stats.getTotalFees());
    assertEquals(2, stats.getPaidFees());
    assertEquals(3, stats.getUnpaidFees());
  }

  @Test
  void manager_generate_advanced_fee_statistics_ok() throws ApiException {
    LocalDateTime fromDateTime = LocalDateTime.parse("2025-04-01T00:00:00.00");
    LocalDateTime toDateTime = LocalDateTime.parse("2025-04-30T23:59:59.99");

    var client = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(client);
    var expectedStats = new AdvancedFeeStatisticsGeneration().data("Total stats generated: 5");
    var actualStat =
        payingApi.generateAdvancedStats(fromDateTime.toInstant(UTC), toDateTime.toInstant(UTC));

    assertEquals(expectedStats, actualStat);
  }

  @Test
  void generate_fees_list_as_xlsx_without_parameters_ok() throws IOException, InterruptedException {
    var response =
        requestFile(URI.create("http://localhost:" + localPort + "/fees/raw"), MANAGER1_TOKEN);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
    assertNotNull(response);
  }

  @Test
  void generate_fees_list_as_xlsx_with_parameters_ok() throws IOException, InterruptedException {
    var responseWithStatus =
        requestFile(
            URI.create("http://localhost:" + localPort + "/fees/raw?status=" + PENDING),
            MANAGER1_TOKEN);
    assertEquals(HttpStatus.OK.value(), responseWithStatus.statusCode());
    assertNotNull(responseWithStatus.body());
    assertNotNull(responseWithStatus);

    var responseWithDateStart =
        requestFile(
            URI.create(
                "http://localhost:"
                    + localPort
                    + "/fees/raw?from_due_datetime=2022-01-01T12:00:00.000Z"),
            MANAGER1_TOKEN);
    assertEquals(HttpStatus.OK.value(), responseWithDateStart.statusCode());
    assertNotNull(responseWithDateStart.body());
    assertNotNull(responseWithDateStart);

    var responseWithDateRange =
        requestFile(
            URI.create(
                "http://localhost:"
                    + localPort
                    + "/fees/raw?from_due_datetime=2022-01-01T12:00:00Z&to_due_datetime=2024-01-02T12:00:00Z"),
            MANAGER1_TOKEN);
    assertEquals(HttpStatus.OK.value(), responseWithDateRange.statusCode());
    assertNotNull(responseWithDateRange.body());
    assertNotNull(responseWithDateRange);
  }

  @Test
  void all_fee_without_status_and_dueDatetime_work() {
    var real_fees = feeRepository.findAll();
    var fees = feeDao.findAllByStatusAndDueDatetimeBetween(null, null, null);

    assertEquals(real_fees.size(), fees.size());
  }

  @Test
  void all_fee_by_status_and_dueDatetime_in_date_range_must_contain_some_fee() {
    var fees =
        feeDao.findAllByStatusAndDueDatetimeBetween(
            null,
            Instant.parse("2021-11-08T08:25:24.00Z"),
            Instant.parse("2022-12-08T08:25:24.00Z"));

    assertFalse(fees.isEmpty());
  }

  @Test
  void manager_request_advanced_fee_stats_generation_ok() {
    ApiClient manager1Client = anApiClient(MANAGER1_TOKEN);
    PayingApi api = new PayingApi(manager1Client);

    assertDoesNotThrow(
        () -> {
          Instant from = Instant.parse("2021-11-08T08:25:24.00Z");
          Instant to = Instant.parse("2021-11-15T08:25:24.00Z");
          api.generateAdvancedStats(from, to);
        });
  }

  @Test
  void student_request_advanced_fee_stats_generation_ko() {
    ApiClient student1Client = anApiClient(STUDENT1_TOKEN);
    PayingApi api = new PayingApi(student1Client);

    assertThrowsForbiddenException(
        () -> api.generateAdvancedStats(now().toInstant(UTC).minus(7, DAYS), now().toInstant(UTC)));
  }

  @Test
  void generate_raw_fees_OK() throws ApiException {
    when(bucketComponent.presign(any(), any()))
        .thenAnswer(invocation -> new URL("https://example.com/file.xlsx"));
    when(bucketComponent.upload(any(), any())).thenReturn(mock());
    var client = anApiClient(MANAGER1_TOKEN);
    var payingApi = new PayingApi(client);
    var from = Instant.parse("2021-12-01T08:25:24.00Z");
    var to = Instant.parse("2023-12-31T08:25:24.00Z");
    var url = payingApi.exportAllFees(AdvancedFeeStatisticsType.ACCOUNTING, from, to);
    assertNotNull(url);
  }

  @Test
  void manager_read_by_category_L1() throws ApiException {
    var manager1Client = anApiClient(MANAGER1_TOKEN);
    var api = new PayingApi(manager1Client);

    var fees = feeRepository.findAll();

    log.info("fees lists : " + fees.getFirst().getCategory());

    var actualWorkFees =
        api.getFees(
            null, null, null, L1, Instant.parse("2021-08-01T05:03:00Z"), null, 1, 10, false, null);
    assertEquals(10, actualWorkFees.getData().size());
  }

  @Test
  void manager_read_by_at_time_now() throws ApiException {
    var manager1Client = anApiClient(MANAGER1_TOKEN);
    var api = new PayingApi(manager1Client);
    var actualWorkFees = api.getFees(null, null, null, L1, null, null, 1, 10, false, null);
    assertEquals(0, actualWorkFees.getData().size());
  }

  @Test
  void findAllByEnabledByDueDatetimeBetween_ok() {
    setUpTestData();
    var from = Instant.parse("2026-01-01T08:00:00.00Z");
    var to = Instant.parse("2026-12-31T23:59:00Z");
    var all2026Fees = feeRepository.findAllByDueDatetimeBetween(from, to);

    assertTrue(all2026Fees.contains(axelFee1));
    assertTrue(all2026Fees.contains(axelFee2));
    assertFalse(all2026Fees.contains(axelFeeDeleted));
    assertFalse(all2026Fees.contains(tolojanaharyFee1));
    assertFalse(all2026Fees.contains(tolojanaharyFee2));
    teardown();
  }

  @Test
  void findAllEnabledByStatusHistoriesBetween_ok() {
    setUpTestData();
    var from = Instant.parse("2026-01-01T00:00:00Z");
    var to = Instant.parse("2026-12-31T23:59:59Z");
    var all2026FeeStatusHistories =
        feeRepository.findDistinctByStatusHistoriesDatetimeBetween(from, to);

    assertTrue(all2026FeeStatusHistories.contains(axelFee1));
    assertTrue(all2026FeeStatusHistories.contains(axelFee2));
    assertFalse(all2026FeeStatusHistories.contains(axelFeeDeleted));
    assertFalse(all2026FeeStatusHistories.contains(tolojanaharyFee1));
    assertFalse(all2026FeeStatusHistories.contains(tolojanaharyFee2));
    teardown();
  }
}
