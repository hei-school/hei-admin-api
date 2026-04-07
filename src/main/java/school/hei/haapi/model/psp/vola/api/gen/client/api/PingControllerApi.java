package school.hei.haapi.model.psp.vola.api.gen.client.api;

import java.util.Collections;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;

@jakarta.annotation.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2026-03-31T10:07:31.002871040+03:00[Indian/Antananarivo]")
public class PingControllerApi {
  private ApiClient apiClient;

  public PingControllerApi() {
    this(new ApiClient());
  }

  public PingControllerApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * <b>200</b> - OK
   *
   * @return String
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public String ping() throws RestClientException {
    return pingWithHttpInfo().getBody();
  }

  /**
   * <b>200</b> - OK
   *
   * @return ResponseEntity&lt;String&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<String> pingWithHttpInfo() throws RestClientException {
    Object localVarPostBody = null;

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    final String[] localVarAccepts = {"*/*"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {};

    ParameterizedTypeReference<String> localReturnType =
        new ParameterizedTypeReference<String>() {};
    return apiClient.invokeAPI(
        "/ping",
        HttpMethod.GET,
        Collections.<String, Object>emptyMap(),
        localVarQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAccept,
        localVarContentType,
        localVarAuthNames,
        localReturnType);
  }
}
