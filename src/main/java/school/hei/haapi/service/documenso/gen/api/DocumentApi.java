package school.hei.haapi.service.documenso.gen.api;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.service.documenso.gen.invoker.ApiClient;
import school.hei.haapi.service.documenso.gen.invoker.BaseApi;
import school.hei.haapi.service.documenso.gen.model.DocumentGet200Response;

@jakarta.annotation.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2026-08-10T16:46:27.452926300+03:00[Indian/Antananarivo]",
    comments = "Generator version: 7.7.0")
public class DocumentApi extends BaseApi {

  public DocumentApi() {
    super(new ApiClient());
  }

  public DocumentApi(ApiClient apiClient) {
    super(apiClient);
  }

  /**
   * Download document Downloads the document. \&quot;signed\&quot; returns the completed document
   * with signatures, \&quot;original\&quot; returns the original uploaded document.
   *
   * <p><b>200</b> - Successful response
   *
   * @param documentId (required)
   * @param version (optional, default to signed)
   * @return File
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public File documentDownload(BigDecimal documentId, String version) throws RestClientException {
    return documentDownloadWithHttpInfo(documentId, version).getBody();
  }

  /**
   * Download document Downloads the document. \&quot;signed\&quot; returns the completed document
   * with signatures, \&quot;original\&quot; returns the original uploaded document.
   *
   * <p><b>200</b> - Successful response
   *
   * @param documentId (required)
   * @param version (optional, default to signed)
   * @return ResponseEntity&lt;File&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<File> documentDownloadWithHttpInfo(BigDecimal documentId, String version)
      throws RestClientException {
    Object localVarPostBody = null;

    // verify the required parameter 'documentId' is set
    if (documentId == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'documentId' when calling documentDownload");
    }

    // create path and map variables
    final Map<String, Object> uriVariables = new HashMap<String, Object>();
    uriVariables.put("documentId", documentId);

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "version", version));

    final String[] localVarAccepts = {"application/pdf"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {"apiKey"};

    ParameterizedTypeReference<File> localReturnType = new ParameterizedTypeReference<File>() {};
    return apiClient.invokeAPI(
        "/document/{documentId}/download",
        HttpMethod.GET,
        uriVariables,
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

  /**
   * Get document
   *
   * <p><b>200</b> - Successful response
   *
   * @param documentId (required)
   * @return DocumentGet200Response
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public DocumentGet200Response documentGet(BigDecimal documentId) throws RestClientException {
    return documentGetWithHttpInfo(documentId).getBody();
  }

  /**
   * Get document
   *
   * <p><b>200</b> - Successful response
   *
   * @param documentId (required)
   * @return ResponseEntity&lt;DocumentGet200Response&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<DocumentGet200Response> documentGetWithHttpInfo(BigDecimal documentId)
      throws RestClientException {
    Object localVarPostBody = null;

    // verify the required parameter 'documentId' is set
    if (documentId == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'documentId' when calling documentGet");
    }

    // create path and map variables
    final Map<String, Object> uriVariables = new HashMap<String, Object>();
    uriVariables.put("documentId", documentId);

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    final String[] localVarAccepts = {"application/json"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {"apiKey"};

    ParameterizedTypeReference<DocumentGet200Response> localReturnType =
        new ParameterizedTypeReference<DocumentGet200Response>() {};
    return apiClient.invokeAPI(
        "/document/{documentId}",
        HttpMethod.GET,
        uriVariables,
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

  @Override
  public <T> ResponseEntity<T> invokeAPI(
      String url, HttpMethod method, Object request, ParameterizedTypeReference<T> returnType)
      throws RestClientException {
    String localVarPath = url.replace(apiClient.getBasePath(), "");
    Object localVarPostBody = request;

    final Map<String, Object> uriVariables = new HashMap<String, Object>();
    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    final String[] localVarAccepts = {"application/json"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {"apiKey"};

    return apiClient.invokeAPI(
        localVarPath,
        method,
        uriVariables,
        localVarQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAccept,
        localVarContentType,
        localVarAuthNames,
        returnType);
  }
}
