package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.Sex.F;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestFiles.uploadProfilePicture;
import static school.hei.haapi.integration.conf.TestMocks.setUpEventBridge;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StaffTestData.staffMemberRina;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.rest.api.UsersApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.StaffMember;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

public class StaffMemberIT extends FacadeITMockedThirdParties {
  private static final String STAFF_MEMBER_XLSX_PATH = "/staff_members/raw/xlsx";

  @MockBean private EventBridgeClient eventBridgeClientMock;
  @Autowired ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;

  private User staffRina;
  private User adminUser;
  private User managerHasina;

  private String staffToken;
  private String adminToken;
  private String managerToken;

  private void setUpTestData() {
    staffRina = userRepository.save(staffMemberRina());
    adminUser = userRepository.save(adminMialy());
    managerHasina = userRepository.save(hasina());
  }

  @BeforeEach
  public void setUp() {
    setUpEventBridge(eventBridgeClientMock);
    setUpTestData();
    setUpS3Service(fileService, staffRina);

    staffToken = tokenFor(casdoorAuthServiceMock, staffRina);
    adminToken = tokenFor(casdoorAuthServiceMock, adminUser);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll(List.of(staffRina, adminUser, managerHasina));
  }

  private UsersApi apiAs(String token) {
    return new UsersApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private HttpResponse<byte[]> getStaffMembersXlsx(String token)
      throws IOException, InterruptedException {
    return HttpClient.newBuilder()
        .build()
        .send(
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + localPort + STAFF_MEMBER_XLSX_PATH))
                .GET()
                .header("Authorization", "Bearer " + token)
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());
  }

  @Test
  void admin_read_staff_members_ok() throws ApiException {
    var actual = apiAs(adminToken).getStaffMembers(1, 100, null, null, null, null);

    assertTrue(actual.stream().anyMatch(s -> staffRina.getId().equals(s.getId())));
  }

  @Test
  void admin_read_staff_by_id_ok() throws ApiException {
    assertNotNull(apiAs(adminToken).getStaffMemberById(staffRina.getId()));
  }

  @Test
  void staff_read_staff_by_id_ok() throws ApiException {
    assertNotNull(apiAs(staffToken).getStaffMemberById(staffRina.getId()));
  }

  @Test
  void manager_read_ko() {
    var api = apiAs(managerToken);

    assertThrowsForbiddenException(() -> api.getStaffMembers(1, 15, null, null, null, null));
    assertThrowsForbiddenException(() -> api.getStaffMemberById(staffRina.getId()));
  }

  @Test
  void staff_upload_profile_picture() throws IOException, InterruptedException {
    var response = uploadProfilePicture(localPort, staffToken, staffRina.getId(), "staff_members");

    var staffMember = objectMapper.readValue(response.body(), StaffMember.class);

    assertEquals(staffRina.getRef(), staffMember.getRef());
    assertEquals(200, response.statusCode());
  }

  @Test
  void admin_upload_profile_picture() throws IOException, InterruptedException {
    var response = uploadProfilePicture(localPort, adminToken, staffRina.getId(), "staff_members");

    var staffMember = objectMapper.readValue(response.body(), StaffMember.class);

    assertEquals(staffRina.getRef(), staffMember.getRef());
    assertEquals(200, response.statusCode());
  }

  @Test
  void admin_create_staff_member_ok() throws ApiException {
    var api = apiAs(adminToken);
    var toCreate =
        new StaffMember()
            .address("test")
            .firstName("test")
            .lastName("test")
            .id(randomUUID().toString())
            .nic("test")
            .cnaps("cnaps")
            .ostie("ostie")
            .degree("degree")
            .function("function")
            .email("staff+" + randomUUID() + "@gmail.com")
            .sex(F)
            .ref("STF" + randomUUID())
            .status(ENABLED)
            .entranceDatetime(Instant.now())
            .coordinates(new Coordinates().latitude(null).longitude(null));

    var actual = api.crupdateStaffMembers(List.of(toCreate));

    assertEquals(1, actual.size());
    assertEquals(toCreate.getDegree(), actual.getFirst().getDegree());
    assertEquals(toCreate.getFunction(), actual.getFirst().getFunction());

    var after = api.getStaffMembers(1, 100, null, null, null, null);
    assertTrue(after.stream().anyMatch(s -> toCreate.getRef().equals(s.getRef())));

    userRepository.deleteById(actual.getFirst().getId());
  }

  @Test
  void admin_read_staff_xlsx_ok() throws IOException, InterruptedException {
    var response = getStaffMembersXlsx(adminToken);

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void manager_read_staff_xlsx_ko() throws IOException, InterruptedException {
    var response = getStaffMembersXlsx(managerToken);

    assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
  }
}
