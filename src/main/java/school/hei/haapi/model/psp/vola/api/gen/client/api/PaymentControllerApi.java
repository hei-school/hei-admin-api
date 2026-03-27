package school.hei.haapi.model.psp.vola.api.gen.client.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;

@Component("paymentControllerApi")
public class PaymentControllerApi {
  private ApiClient apiClient;

  public PaymentControllerApi() {
    this(new ApiClient());
  }

  @Autowired
  public PaymentControllerApi(ApiClient apiClient) {
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
   * @param apiKey (required)
   * @param payerEmail (required)
   * @param pspType (required)
   * @param pspPaymentId (required)
   * @return Payment
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public Payment createPayment(
      String apiKey, String payerEmail, String pspType, String pspPaymentId)
      throws RestClientException {
    return createPaymentWithHttpInfo(apiKey, payerEmail, pspType, pspPaymentId).getBody();
  }

  /**
   * <b>200</b> - OK
   *
   * @param apiKey (required)
   * @param payerEmail (required)
   * @param pspType (required)
   * @param pspPaymentId (required)
   * @return ResponseEntity&lt;Payment&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<Payment> createPaymentWithHttpInfo(
      String apiKey, String payerEmail, String pspType, String pspPaymentId)
      throws RestClientException {
    Object postBody = null;
    // verify the required parameter 'apiKey' is set
    if (apiKey == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'apiKey' when calling createPayment");
    }
    // verify the required parameter 'payerEmail' is set
    if (payerEmail == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'payerEmail' when calling createPayment");
    }
    // verify the required parameter 'pspType' is set
    if (pspType == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'pspType' when calling createPayment");
    }
    // verify the required parameter 'pspPaymentId' is set
    if (pspPaymentId == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'pspPaymentId' when calling createPayment");
    }
    String path = UriComponentsBuilder.fromPath("/payment").build().toUriString();

    final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
    final HttpHeaders headerParams = new HttpHeaders();
    final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "apiKey", apiKey));
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "payerEmail", payerEmail));
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "pspType", pspType));
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "pspPaymentId", pspPaymentId));

    final String[] accepts = {"*/*"};
    final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
    final String[] contentTypes = {};
    final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {};

    ParameterizedTypeReference<Payment> returnType = new ParameterizedTypeReference<Payment>() {};
    return apiClient.invokeAPI(
        path,
        HttpMethod.POST,
        queryParams,
        postBody,
        headerParams,
        formParams,
        accept,
        contentType,
        authNames,
        returnType);
  }

  /**
   * <b>200</b> - OK
   *
   * @param apiKey (required)
   * @param payerEmail (required)
   * @param pspType (required)
   * @param pspPaymentId (required)
   * @return Payment
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public Payment getPayment(String apiKey, String payerEmail, String pspType, String pspPaymentId)
      throws RestClientException {
    return getPaymentWithHttpInfo(apiKey, payerEmail, pspType, pspPaymentId).getBody();
  }

  /**
   * <b>200</b> - OK
   *
   * @param apiKey (required)
   * @param payerEmail (required)
   * @param pspType (required)
   * @param pspPaymentId (required)
   * @return ResponseEntity&lt;Payment&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<Payment> getPaymentWithHttpInfo(
      String apiKey, String payerEmail, String pspType, String pspPaymentId)
      throws RestClientException {
    Object postBody = null;
    // verify the required parameter 'apiKey' is set
    if (apiKey == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'apiKey' when calling getPayment");
    }
    // verify the required parameter 'payerEmail' is set
    if (payerEmail == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'payerEmail' when calling getPayment");
    }
    // verify the required parameter 'pspType' is set
    if (pspType == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'pspType' when calling getPayment");
    }
    // verify the required parameter 'pspPaymentId' is set
    if (pspPaymentId == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'pspPaymentId' when calling getPayment");
    }
    String path = UriComponentsBuilder.fromPath("/payment").build().toUriString();

    final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
    final HttpHeaders headerParams = new HttpHeaders();
    final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "apiKey", apiKey));
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "payerEmail", payerEmail));
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "pspType", pspType));
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "pspPaymentId", pspPaymentId));

    final String[] accepts = {"*/*"};
    final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
    final String[] contentTypes = {};
    final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {};

    ParameterizedTypeReference<Payment> returnType = new ParameterizedTypeReference<Payment>() {};
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
