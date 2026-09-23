package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.testData.StaffTestData.adminMialy;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.SearchApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.FakeDataProvider;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.integration.testData.ManagerTestData;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

class SearchIT extends FacadeITMockedThirdParties {

  @Autowired private UserRepository userRepository;

  private String marker;
  private User helene;

  private List<String> userIds = new ArrayList<>();
  private String adminToken;
  private String managerToken;
  private String studentToken;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpSearchTestData();
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAllById(userIds);
    userIds = new ArrayList<>();
  }

  private void setUpSearchTestData() {
    marker = "SRCH" + randomUUID().toString().substring(0, 8);
    save(markedUser("Ryan", User.Role.MANAGER));
    save(markedUser("Rika", User.Role.MANAGER));
    save(markedUser("Ry", User.Role.TEACHER));
    save(markedUser("Ryo", User.Role.TEACHER));
    save(markedUser("Ryna", User.Role.STUDENT));
    save(markedUser("Bryan", User.Role.STUDENT));
    save(markedUser("Mona", User.Role.MONITOR));
    save(markedUser("Orly", User.Role.ORGANIZER));
    save(markedUser("Stef", User.Role.STAFF_MEMBER));
    helene = namedUser("Hélène", "Rakotobe", User.Role.STUDENT);
    save(helene);
    save(namedUser("Andre", "Randrianarisoa", User.Role.TEACHER));

    var admin = adminMialy();
    var manager = ManagerTestData.hasina();
    var student = axel();
    save(admin);
    save(manager);
    save(student);
    adminToken = tokenFor(casdoorAuthServiceMock, admin);
    managerToken = tokenFor(casdoorAuthServiceMock, manager);
    studentToken = tokenFor(casdoorAuthServiceMock, student);
  }

  private User markedUser(String firstName, User.Role role) {
    var user = FakeDataProvider.someUser(marker + firstName, role);
    user.setLastName(marker);
    return user;
  }

  private User namedUser(String firstName, String lastName, User.Role role) {
    var user = FakeDataProvider.someUser(marker + firstName, role);
    user.setLastName(marker + lastName);
    return user;
  }

  private void save(User user) {
    userRepository.save(user);
    userIds.add(user.getId());
  }

  @Test
  void admin_can_global_search() throws ApiException {
    var api = new SearchApi(anApiClient(adminToken));
    var results = api.globalSearchUserGet(null);

    assertNotNull(results, "Search results should not be null");
  }

  @Test
  void manager_can_global_search() throws ApiException {
    var api = new SearchApi(anApiClient(managerToken));
    var results = api.globalSearchUserGet(null);

    assertNotNull(results, "Search results should not be null");
  }

  @Test
  void filter_global_search_by_query_ok() throws ApiException {
    var api = new SearchApi(anApiClient(adminToken));

    var filteredResults = api.globalSearchUserGet(marker + "Ry");

    assertNotNull(filteredResults, "Filtered results should not be null");
    assertTrue(
        filteredResults.getStudents() != null
            || filteredResults.getTeachers() != null
            || filteredResults.getManagers() != null,
        "At least one result category should be present");
  }

  @Test
  void student_cannot_global_search() {
    var api = new SearchApi(anApiClient(studentToken));

    assertThrowsForbiddenException(() -> api.globalSearchUserGet(null));
  }

  @Test
  void should_return_0_results_when_global_search_is_filtered() throws ApiException {
    var result = new SearchApi(anApiClient(managerToken)).globalSearchUserGet(marker + "mahefa");
    var expected = 0;

    int totalResults =
        result.getStudents().size()
            + result.getMonitors().size()
            + result.getTeachers().size()
            + result.getManagers().size();

    assertEquals(expected, totalResults);
  }

  @Test
  void should_return_1_managers_when_searching_ryan() throws ApiException {
    var result = new SearchApi(anApiClient(managerToken)).globalSearchUserGet(marker + "Ryan");
    var expected = 1;

    assertEquals(expected, result.getManagers().size());
  }

  @Test
  void should_return_monitors_organisers_and_staff_members_under_contract_names()
      throws ApiException {
    var result = new SearchApi(anApiClient(managerToken)).globalSearchUserGet(marker);

    assertEquals(1, result.getMonitors().size());
    assertEquals(1, result.getOrganisers().size());
    assertEquals(1, result.getStaffMembers().size());
  }

  @Test
  void global_search_ignores_accents() throws ApiException {
    var api = new SearchApi(anApiClient(managerToken));

    var withoutAccent = api.globalSearchUserGet(marker + "helene");
    var withAccent = api.globalSearchUserGet(marker + "André");

    assertEquals(1, withoutAccent.getStudents().size());
    assertEquals(helene.getId(), withoutAccent.getStudents().get(0).getId());
    assertEquals(1, withAccent.getTeachers().size());
  }

  @Test
  void global_search_combines_first_name_and_last_name_in_any_order() throws ApiException {
    var api = new SearchApi(anApiClient(managerToken));

    var firstNameThenLastName = api.globalSearchUserGet(marker + "Helene  " + marker + "rakotobe");
    var lastNameThenFirstName = api.globalSearchUserGet(marker + "Rakotobe " + marker + "hélène");
    var wordsOfDifferentUsers = api.globalSearchUserGet(marker + "Helene " + marker + "Randria");

    assertEquals(1, firstNameThenLastName.getStudents().size());
    assertEquals(1, lastNameThenFirstName.getStudents().size());
    assertEquals(0, wordsOfDifferentUsers.getStudents().size());
    assertEquals(0, wordsOfDifferentUsers.getTeachers().size());
  }
}
