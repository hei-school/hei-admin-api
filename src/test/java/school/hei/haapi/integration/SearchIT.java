package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.student1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.api.SearchApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

public class SearchIT extends FacadeITMockedThirdParties {

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
  }

  @Test
  void admin_can_global_search() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    SearchApi api = new SearchApi(apiClient);

    var results = api.globalSearchGet(null);

    assertNotNull(results, "Search results should not be null");
  }

  @Test
  void manager_can_global_search() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    SearchApi api = new SearchApi(apiClient);

    var results = api.globalSearchGet(null);

    assertNotNull(results, "Search results should not be null");
  }

  @Test
  void filter_global_search_by_query_ok() throws ApiException {
    ApiClient apiClient = anApiClient(ADMIN1_TOKEN);
    SearchApi api = new SearchApi(apiClient);
    String searchQuery = "Ryan";

    var filteredResults = api.globalSearchGet(searchQuery);

    assertNotNull(filteredResults, "Filtered results should not be null");
    assertTrue(
        filteredResults.getStudents() != null
            || filteredResults.getTeachers() != null
            || filteredResults.getManagers() != null,
        "At least one result category should be present");
  }

  @Test
  void filter_global_search_by_student_ref_ok() throws ApiException {
    ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
    SearchApi api = new SearchApi(apiClient);
    String studentRef = student1().getRef();

    var filteredResults = api.globalSearchGet(studentRef);

    assertNotNull(filteredResults, "Filtered results should not be null");
  }

  @Test
  void student_cannot_global_search() {
    ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
    SearchApi api = new SearchApi(apiClient);

    assertThrowsForbiddenException(() -> api.globalSearchGet(null));
  }
}
