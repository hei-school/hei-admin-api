package school.hei.haapi.service.documenso.gen.api;

import java.math.BigDecimal;
import java.util.Collections;
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
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200Response;

@jakarta.annotation.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2026-08-10T16:46:27.452926300+03:00[Indian/Antananarivo]",
    comments = "Generator version: 7.7.0")
public class TemplateApi extends BaseApi {

  public TemplateApi() {
    super(new ApiClient());
  }

  public TemplateApi(ApiClient apiClient) {
    super(apiClient);
  }

  /**
   * Use template Use the template to create a document
   *
   * <p><b>200</b> - Successful response
   *
   * @param templateCreateDocumentFromTemplateRequest (required)
   * @return TemplateCreateDocumentFromTemplate200Response
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public TemplateCreateDocumentFromTemplate200Response templateCreateDocumentFromTemplate(
      TemplateCreateDocumentFromTemplateRequest templateCreateDocumentFromTemplateRequest)
      throws RestClientException {
    return templateCreateDocumentFromTemplateWithHttpInfo(templateCreateDocumentFromTemplateRequest)
        .getBody();
  }

  /**
   * Use template Use the template to create a document
   *
   * <p><b>200</b> - Successful response
   *
   * @param templateCreateDocumentFromTemplateRequest (required)
   * @return ResponseEntity&lt;TemplateCreateDocumentFromTemplate200Response&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<TemplateCreateDocumentFromTemplate200Response>
      templateCreateDocumentFromTemplateWithHttpInfo(
          TemplateCreateDocumentFromTemplateRequest templateCreateDocumentFromTemplateRequest)
          throws RestClientException {
    Object localVarPostBody = templateCreateDocumentFromTemplateRequest;

    // verify the required parameter 'templateCreateDocumentFromTemplateRequest' is set
    if (templateCreateDocumentFromTemplateRequest == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'templateCreateDocumentFromTemplateRequest' when calling"
              + " templateCreateDocumentFromTemplate");
    }

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    final String[] localVarAccepts = {"application/json"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {"application/json"};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {"apiKey"};

    ParameterizedTypeReference<TemplateCreateDocumentFromTemplate200Response> localReturnType =
        new ParameterizedTypeReference<TemplateCreateDocumentFromTemplate200Response>() {};
    return apiClient.invokeAPI(
        "/template/use",
        HttpMethod.POST,
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

  /**
   * Find templates
   *
   * <p><b>200</b> - Successful response
   *
   * @param query (optional)
   * @param page (optional)
   * @param perPage (optional)
   * @return TemplateFindTemplates200Response
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public TemplateFindTemplates200Response templateFindTemplates(
      String query, BigDecimal page, BigDecimal perPage) throws RestClientException {
    return templateFindTemplatesWithHttpInfo(query, page, perPage).getBody();
  }

  /**
   * Find templates
   *
   * <p><b>200</b> - Successful response
   *
   * @param query (optional)
   * @param page (optional)
   * @param perPage (optional)
   * @return ResponseEntity&lt;TemplateFindTemplates200Response&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<TemplateFindTemplates200Response> templateFindTemplatesWithHttpInfo(
      String query, BigDecimal page, BigDecimal perPage) throws RestClientException {
    Object localVarPostBody = null;

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "query", query));
    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "page", page));
    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "perPage", perPage));

    final String[] localVarAccepts = {"application/json"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {"apiKey"};

    ParameterizedTypeReference<TemplateFindTemplates200Response> localReturnType =
        new ParameterizedTypeReference<TemplateFindTemplates200Response>() {};
    return apiClient.invokeAPI(
        "/template",
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

  /**
   * Get template
   *
   * <p><b>200</b> - Successful response
   *
   * @param templateId (required)
   * @return TemplateGetTemplateById200Response
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public TemplateGetTemplateById200Response templateGetTemplateById(BigDecimal templateId)
      throws RestClientException {
    return templateGetTemplateByIdWithHttpInfo(templateId).getBody();
  }

  /**
   * Get template
   *
   * <p><b>200</b> - Successful response
   *
   * @param templateId (required)
   * @return ResponseEntity&lt;TemplateGetTemplateById200Response&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<TemplateGetTemplateById200Response> templateGetTemplateByIdWithHttpInfo(
      BigDecimal templateId) throws RestClientException {
    Object localVarPostBody = null;

    // verify the required parameter 'templateId' is set
    if (templateId == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'templateId' when calling templateGetTemplateById");
    }

    // create path and map variables
    final Map<String, Object> uriVariables = new HashMap<String, Object>();
    uriVariables.put("templateId", templateId);

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

    ParameterizedTypeReference<TemplateGetTemplateById200Response> localReturnType =
        new ParameterizedTypeReference<TemplateGetTemplateById200Response>() {};
    return apiClient.invokeAPI(
        "/template/{templateId}",
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
