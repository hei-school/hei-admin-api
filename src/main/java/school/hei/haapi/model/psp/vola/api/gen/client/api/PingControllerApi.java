package school.hei.haapi.model.psp.vola.api.gen.client.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;

@Component("school.hei.tsinjo.model.psp.vola.api.gen.client.api.PingControllerApi")
public class PingControllerApi {
  private ApiClient apiClient;

  public PingControllerApi() {
    this(new ApiClient());
  }

  @Autowired
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
    Object postBody = null;
    String path = UriComponentsBuilder.fromPath("/ping").build().toUriString();

    final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
    final HttpHeaders headerParams = new HttpHeaders();
    final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

    final String[] accepts = {"*/*"};
    final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
    final String[] contentTypes = {};
    final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {};

    ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
    return apiClient.invokeAPI(
        path,
        HttpMethod.GET,
        queryParams,
        postBody,
        headerParams,
        formParams,
        accept,
        contentType,
        authNames,
        returnType);
  }
}
