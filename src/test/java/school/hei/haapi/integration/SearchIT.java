package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;

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
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

class SearchIT extends FacadeITMockedThirdParties {

  @Autowired private UserRepository userRepository;

  private List<String> userIds = new ArrayList<>();

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpSearchTestData();
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAllById(userIds);
    userIds = new ArrayList<>();
  }

  private void setUpSearchTestData() {
    save(FakeDataProvider.someUser("Ryan", User.Role.MANAGER));
    save(FakeDataProvider.someUser("Rika", User.Role.MANAGER));
    save(FakeDataProvider.someUser("Ry", User.Role.TEACHER));
    save(FakeDataProvider.someUser("Ryo", User.Role.TEACHER));
    save(FakeDataProvider.someUser("Ryna", User.Role.STUDENT));
    save(FakeDataProvider.someUser("Bryan", User.Role.STUDENT));
  }

  private void save(User user) {
    userRepository.save(user);
    userIds.add(user.getId());
  }

  @Test
  void admin_can_global_search() throws ApiException {
    var api = new SearchApi(anApiClient(ADMIN1_TOKEN));
    var results = api.globalSearchUserGet(null);

    assertNotNull(results, "Search results should not be null");
  }

  @Test
  void manager_can_global_search() throws ApiException {
    var api = new SearchApi(anApiClient(MANAGER1_TOKEN));
    var results = api.globalSearchUserGet(null);

    assertNotNull(results, "Search results should not be null");
  }

  @Test
  void filter_global_search_by_query_ok() throws ApiException {
    var api = new SearchApi(anApiClient(ADMIN1_TOKEN));

    var filteredResults = api.globalSearchUserGet("Ry");

    assertNotNull(filteredResults, "Filtered results should not be null");
    assertTrue(
        filteredResults.getStudents() != null
            || filteredResults.getTeachers() != null
            || filteredResults.getManagers() != null,
        "At least one result category should be present");
  }

  @Test
  void student_cannot_global_search() {
    var api = new SearchApi(anApiClient(STUDENT1_TOKEN));

    assertThrowsForbiddenException(() -> api.globalSearchUserGet(null));
  }

  @Test
  void should_return_0_results_when_global_search_is_filtered() throws ApiException {
    var result = new SearchApi(anApiClient(MANAGER1_TOKEN)).globalSearchUserGet("mahefa");
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
    var result = new SearchApi(anApiClient(MANAGER1_TOKEN)).globalSearchUserGet("Ryan");
    var expected = 1;

    assertEquals(expected, result.getManagers().size());
  }
}
