package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PAID;
import static school.hei.haapi.endpoint.rest.model.FileType.OTHER;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.RECEIVED;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.REJECTED;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.getMockedFile;
import static school.hei.haapi.integration.conf.TestFiles.uploadLetter;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTestData.createPendingFee;
import static school.hei.haapi.integration.testData.LetterTestData.aLetter;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StaffTestData.staffMemberRina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.haapi.endpoint.rest.api.FilesApi;
import school.hei.haapi.endpoint.rest.api.LettersApi;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Letter;
import school.hei.haapi.endpoint.rest.model.RoleEnum;
import school.hei.haapi.endpoint.rest.model.UpdateLettersStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.LetterRepository;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class LetterIT extends FacadeITMockedThirdParties {
  @MockBean EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private LetterRepository letterRepository;
  @Autowired private FeeRepository feeRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  private User studentAxel;
  private User studentFreddy;
  private User teacherToky;
  private User staffRina;
  private User managerHasina;
  private User adminUser;

  private school.hei.haapi.model.Letter axelReceivedLetter;
  private school.hei.haapi.model.Letter axelPendingLetter;
  private school.hei.haapi.model.Letter freddyPendingLetter;
  private school.hei.haapi.model.Letter teacherLetter;
  private school.hei.haapi.model.Letter staffLetter;
  private Fee axelFee;

  private String axelToken;
  private String teacherToken;
  private String staffToken;
  private String managerToken;
  private String adminToken;

  void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    teacherToky = userRepository.save(toky());
    staffRina = userRepository.save(staffMemberRina());
    managerHasina = userRepository.save(hasina());
    adminUser = userRepository.save(adminMialy());

    axelFee =
        feeRepository.save(
            createPendingFee(studentAxel, 5000, Instant.parse("2022-12-08T08:25:24.00Z"))
                .toBuilder()
                // settling a fee through a letter issues a BANK_TRANSFER payment, which the
                // validator requires to carry the fee comment
                .comment("Frais de scolarite")
                .build());

    axelReceivedLetter =
        letterRepository.save(aLetter(studentAxel, "Certificat de residence", RECEIVED));
    axelPendingLetter =
        letterRepository.save(aLetter(studentAxel, "Bordereau de versement", PENDING));
    freddyPendingLetter = letterRepository.save(aLetter(studentFreddy, "CV", PENDING));
    teacherLetter = letterRepository.save(aLetter(teacherToky, "Teacher file", RECEIVED));
    staffLetter = letterRepository.save(aLetter(staffRina, "Staff file", PENDING));
  }

  @BeforeEach
  void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    staffToken = tokenFor(casdoorAuthServiceMock, staffRina);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
  }

  @AfterEach
  void tearDown() {
    letterRepository.deleteAll(
        letterRepository.findAll().stream()
            .filter(l -> ownedUserIds().contains(l.getUser().getId()))
            .toList());
    // accepting a fee-linked letter creates a payment: it has to go before the fee, or it is

    // left dangling for whichever test next reads payments globally

    jdbcTemplate.update("DELETE FROM \"payment\" WHERE fee_id = ?", axelFee.getId());

    jdbcTemplate.update("DELETE FROM \"fee_status_history\" WHERE fee_id = ?", axelFee.getId());

    jdbcTemplate.update("DELETE FROM \"fee\" WHERE id = ?", axelFee.getId());
    userRepository.deleteAll(
        List.of(studentAxel, studentFreddy, teacherToky, staffRina, managerHasina, adminUser));
  }

  private List<String> ownedUserIds() {
    return List.of(
        studentAxel.getId(),
        studentFreddy.getId(),
        teacherToky.getId(),
        staffRina.getId(),
        managerHasina.getId(),
        adminUser.getId());
  }

  private LettersApi apiAs(String token) {
    return new LettersApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static List<String> idsOf(List<Letter> letters) {
    return letters.stream().map(Letter::getId).toList();
  }

  @Test
  void manager_read_ko() {
    var api = apiAs(managerToken);

    assertThrowsForbiddenException(() -> api.getLetterStats(null));
    assertThrowsForbiddenException(
        () -> api.getLetters(1, 15, null, null, null, null, null, null, null));
  }

  @Test
  void manager_read_stats_ok() throws ApiException {
    var letterStats = apiAs(managerToken).getStudentsLetterStats();

    assertNotNull(letterStats);
  }

  @Test
  void admin_read_stats_ok() throws ApiException {
    var letterStats = apiAs(adminToken).getStudentsLetterStats();

    assertNotNull(letterStats);
  }

  @Test
  void staff_read_own_letters_ok() throws ApiException {
    var api = apiAs(staffToken);

    var letters = api.getLettersByUserId(staffRina.getId(), null, 1, 15, null);

    assertEquals(1, letters.size());
    assertEquals(staffLetter.getId(), letters.getFirst().getId());
    assertThrowsForbiddenException(() -> api.getLetterStats(null));
  }

  @Test
  void manager_read_ok() throws ApiException {
    var api = apiAs(managerToken);

    var actual = api.getStudentsLetters(1, 15, null, null, null, null, null, null);
    assertTrue(idsOf(actual).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(actual).contains(axelPendingLetter.getId()));
    assertTrue(idsOf(actual).contains(freddyPendingLetter.getId()));
    assertFalse(idsOf(actual).contains(teacherLetter.getId()));

    var filteredByStudentRef =
        api.getStudentsLetters(1, 15, studentAxel.getRef(), null, null, null, null, null);
    assertTrue(idsOf(filteredByStudentRef).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(filteredByStudentRef).contains(axelPendingLetter.getId()));
    assertFalse(idsOf(filteredByStudentRef).contains(freddyPendingLetter.getId()));

    var filteredByStudentName =
        api.getStudentsLetters(1, 15, null, null, null, studentAxel.getFirstName(), null, null);
    assertTrue(idsOf(filteredByStudentName).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(filteredByStudentName).contains(axelPendingLetter.getId()));
    assertFalse(idsOf(filteredByStudentName).contains(freddyPendingLetter.getId()));

    var filteredByLetterRef =
        api.getStudentsLetters(1, 15, null, axelReceivedLetter.getRef(), null, null, null, null);
    assertTrue(idsOf(filteredByLetterRef).contains(axelReceivedLetter.getId()));
    assertFalse(idsOf(filteredByLetterRef).contains(axelPendingLetter.getId()));

    var filteredByStatus =
        api.getStudentsLetters(1, 15, studentAxel.getRef(), null, PENDING, null, null, null);
    assertFalse(idsOf(filteredByStatus).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(filteredByStatus).contains(axelPendingLetter.getId()));
  }

  @Test
  void manager_read_by_id() throws ApiException {
    var actual = apiAs(managerToken).getLetterById(axelReceivedLetter.getId());

    assertEquals(axelReceivedLetter.getId(), actual.getId());
    assertEquals(axelReceivedLetter.getRef(), actual.getRef());
    assertEquals(axelReceivedLetter.getDescription(), actual.getDescription());
    assertEquals(RECEIVED, actual.getStatus());
  }

  @Test
  void manager_read_students_letter() throws ApiException {
    var api = apiAs(managerToken);

    var axelLetters = api.getLettersByUserId(studentAxel.getId(), null, 1, 15, null);
    assertTrue(idsOf(axelLetters).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(axelLetters).contains(axelPendingLetter.getId()));
    assertFalse(idsOf(axelLetters).contains(freddyPendingLetter.getId()));

    var freddyLetters = api.getLettersByUserId(studentFreddy.getId(), null, 1, 15, null);
    assertFalse(idsOf(freddyLetters).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(freddyLetters).contains(freddyPendingLetter.getId()));

    var axelPendingOnly = api.getLettersByUserId(studentAxel.getId(), null, 1, 15, PENDING);
    assertFalse(idsOf(axelPendingOnly).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(axelPendingOnly).contains(axelPendingLetter.getId()));
  }

  @Test
  void manager_create_and_update_students_letter()
      throws IOException, InterruptedException, ApiException {
    var apiClient = anApiClient(managerToken);
    var api = new LettersApi(apiClient);
    var payingApi = new PayingApi(apiClient);
    var filesApi = new FilesApi(apiClient);

    var toBeReceived =
        uploadLetter(
            localPort, managerToken, studentAxel.getId(), "Certificat", "file", null, null, null);
    var createdLetter1 = objectMapper.readValue(toBeReceived.body(), Letter.class);
    assertEquals("Certificat", createdLetter1.getDescription());
    assertEquals(PENDING, createdLetter1.getStatus());

    var toBeRejected =
        uploadLetter(
            localPort, managerToken, studentAxel.getId(), "A rejeter", "file", null, null, null);
    var createdLetter2 = objectMapper.readValue(toBeRejected.body(), Letter.class);
    assertEquals("A rejeter", createdLetter2.getDescription());
    assertEquals(PENDING, createdLetter2.getStatus());

    var updatedLetters =
        api.updateLettersStatus(
            List.of(
                new UpdateLettersStatus().id(createdLetter1.getId()).status(RECEIVED),
                new UpdateLettersStatus()
                    .id(createdLetter2.getId())
                    .status(REJECTED)
                    .reasonForRefusal("Mauvais format")));

    var updatedLetter1 = updatedLetters.getFirst();
    assertEquals(RECEIVED, updatedLetter1.getStatus());
    assertNotNull(updatedLetter1.getApprovalDatetime());
    assertEquals(createdLetter1.getId(), updatedLetter1.getId());
    assertNull(createdLetter1.getFee());

    var updatedLetter2 = updatedLetters.get(1);
    assertEquals(REJECTED, updatedLetter2.getStatus());
    assertNotNull(updatedLetter2.getApprovalDatetime());
    assertEquals(createdLetter2.getId(), updatedLetter2.getId());

    // only the accepted letter is filed: LetterService saves a FileInfo on RECEIVED, not on
    // REJECTED
    var fileInfos = filesApi.getUserFiles(studentAxel.getId(), 1, 15, OTHER);
    assertEquals(1, fileInfos.size());
    assertEquals(createdLetter1.getDescription(), fileInfos.getFirst().getName());

    var notLinkedToAFee =
        api.getStudentsLetters(1, 15, studentAxel.getRef(), null, null, null, null, false);
    assertTrue(idsOf(notLinkedToAFee).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(notLinkedToAFee).contains(axelPendingLetter.getId()));
  }

  @Test
  void manager_accepting_a_fee_linked_letter_settles_the_fee()
      throws IOException, InterruptedException, ApiException {
    var apiClient = anApiClient(managerToken);
    var api = new LettersApi(apiClient);
    var payingApi = new PayingApi(apiClient);

    var feeLetterUpload =
        uploadLetter(
            localPort,
            managerToken,
            studentAxel.getId(),
            "Test fee",
            "file",
            axelFee.getId(),
            5000,
            null);
    var createdFeeLetter = objectMapper.readValue(feeLetterUpload.body(), Letter.class);

    var feeLetterUpdated =
        api.updateLettersStatus(
                List.of(new UpdateLettersStatus().id(createdFeeLetter.getId()).status(RECEIVED)))
            .getFirst();

    var actualFee = payingApi.getStudentFeeById(studentAxel.getId(), axelFee.getId());
    assertEquals(actualFee.getComment(), feeLetterUpdated.getFee().getComment());
    assertEquals(actualFee.getType(), feeLetterUpdated.getFee().getType());
    assertEquals(PAID, actualFee.getStatus());

    var filteredByFeeId =
        api.getStudentsLetters(1, 15, null, null, null, null, axelFee.getId(), null);
    assertEquals(feeLetterUpdated.getId(), filteredByFeeId.getFirst().getId());
  }

  @Test
  void student_read_self_ok() throws ApiException {
    var actual = apiAs(axelToken).getLettersByUserId(studentAxel.getId(), null, 1, 15, null);

    assertTrue(idsOf(actual).contains(axelReceivedLetter.getId()));
    assertTrue(idsOf(actual).contains(axelPendingLetter.getId()));
    assertFalse(idsOf(actual).contains(freddyPendingLetter.getId()));
  }

  @Test
  void teacher_read_others_letter_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(
        () -> api.getLettersByUserId(studentAxel.getId(), null, 1, 15, null));
  }

  @Test
  void teacher_upload_letter_for_fee_ko() {
    var api = apiAs(teacherToken);

    assertThrowsForbiddenException(
        () ->
            api.createLetter(
                teacherToky.getId(),
                "filename",
                "description",
                axelFee.getId(),
                null,
                null,
                getMockedFile("img", ".png")));

    assertThrowsForbiddenException(
        () ->
            api.createLetter(
                teacherToky.getId(),
                "filename",
                "description",
                null,
                null,
                studentAxel.getId(),
                getMockedFile("img", ".png")));
  }

  @Test
  void upload_letter_with_bad_request_ko() {
    var api = apiAs(axelToken);

    assertBadRequestException(
        "Cannot link letter with both fee and event participant",
        () ->
            api.createLetter(
                studentAxel.getId(),
                "filename",
                "description",
                axelFee.getId(),
                0,
                studentAxel.getId(),
                getMockedFile("img", ".png")));

    assertBadRequestException(
        "Cannot create a letter for a fee without a given amount",
        () ->
            api.createLetter(
                studentAxel.getId(),
                "filename",
                "description",
                axelFee.getId(),
                null,
                null,
                getMockedFile("img", ".png")));
  }

  @Test
  void teacher_read_self_ok() throws ApiException {
    var actual = apiAs(teacherToken).getLettersByUserId(teacherToky.getId(), null, 1, 15, null);

    assertEquals(1, actual.size());
    assertEquals(teacherLetter.getId(), actual.getFirst().getId());
  }

  @Test
  void student_forbidden_endpoint() {
    var api = apiAs(axelToken);

    assertThrowsForbiddenException(
        () -> api.getLettersByUserId(studentFreddy.getId(), null, 1, 15, null));
    assertThrowsForbiddenException(
        () ->
            api.updateLettersStatus(
                List.of(new UpdateLettersStatus().id(randomUUID().toString()).status(RECEIVED))));
  }

  @Test
  void student_upload_own_letter_ok() throws IOException, InterruptedException {
    var response =
        uploadLetter(
            localPort, axelToken, studentAxel.getId(), "Certificat", "file", null, null, null);

    var createdLetter = objectMapper.readValue(response.body(), Letter.class);
    assertEquals("Certificat", createdLetter.getDescription());
    assertEquals(PENDING, createdLetter.getStatus());
  }

  @Test
  void admin_filter_letters_by_multiple_roles() throws ApiException {
    var roles = List.of(RoleEnum.TEACHER, RoleEnum.STAFF_MEMBER);

    var letters = apiAs(adminToken).getLetters(1, 15, null, null, null, null, null, null, roles);

    // students are excluded by the role filter, staff and teachers are kept
    assertFalse(idsOf(letters).contains(axelReceivedLetter.getId()));
    assertFalse(idsOf(letters).contains(axelPendingLetter.getId()));
    assertFalse(idsOf(letters).contains(freddyPendingLetter.getId()));
    assertTrue(idsOf(letters).contains(teacherLetter.getId()));
    assertTrue(idsOf(letters).contains(staffLetter.getId()));
  }
}
