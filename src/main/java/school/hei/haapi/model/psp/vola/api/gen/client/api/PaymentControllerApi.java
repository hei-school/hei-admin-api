package school.hei.haapi.model.psp.vola.api.gen.client.api;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentInfo;
import school.hei.haapi.model.psp.vola.api.gen.client.model.RecoveryResult;

@jakarta.annotation.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2026-03-31T10:07:31.002871040+03:00[Indian/Antananarivo]")
public class PaymentControllerApi {
  private ApiClient apiClient;

  public PaymentControllerApi() {
    this(new ApiClient());
  }

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
    Object localVarPostBody = null;

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

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "apiKey", apiKey));
    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "payerEmail", payerEmail));
    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "pspType", pspType));
    localVarQueryParams.putAll(
        apiClient.parameterToMultiValueMap(null, "pspPaymentId", pspPaymentId));

    final String[] localVarAccepts = {"*/*"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {};

    ParameterizedTypeReference<Payment> localReturnType =
        new ParameterizedTypeReference<Payment>() {};
    return apiClient.invokeAPI(
        "/payment",
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
    Object localVarPostBody = null;

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

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "apiKey", apiKey));
    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "payerEmail", payerEmail));
    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "pspType", pspType));
    localVarQueryParams.putAll(
        apiClient.parameterToMultiValueMap(null, "pspPaymentId", pspPaymentId));

    final String[] localVarAccepts = {"*/*"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {};

    ParameterizedTypeReference<Payment> localReturnType =
        new ParameterizedTypeReference<Payment>() {};
    return apiClient.invokeAPI(
        "/payment",
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
   * <b>200</b> - OK
   *
   * @param apiKey (required)
   * @param paymentInfo (required)
   * @return List&lt;Payment&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public List<Payment> getPayments(String apiKey, List<PaymentInfo> paymentInfo)
      throws RestClientException {
    return getPaymentsWithHttpInfo(apiKey, paymentInfo).getBody();
  }

  /**
   * <b>200</b> - OK
   *
   * @param apiKey (required)
   * @param paymentInfo (required)
   * @return ResponseEntity&lt;List&lt;Payment&gt;&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<List<Payment>> getPaymentsWithHttpInfo(
      String apiKey, List<PaymentInfo> paymentInfo) throws RestClientException {
    Object localVarPostBody = paymentInfo;

    // verify the required parameter 'apiKey' is set
    if (apiKey == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'apiKey' when calling getPayments");
    }

    // verify the required parameter 'paymentInfo' is set
    if (paymentInfo == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'paymentInfo' when calling getPayments");
    }

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "apiKey", apiKey));

    final String[] localVarAccepts = {"*/*"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {"application/json"};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {};

    ParameterizedTypeReference<List<Payment>> localReturnType =
        new ParameterizedTypeReference<List<Payment>>() {};
    return apiClient.invokeAPI(
        "/payments/search",
        HttpMethod.PUT,
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
   * <b>200</b> - OK
   *
   * @param date (required)
   * @return RecoveryResult
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public RecoveryResult sync(LocalDate date) throws RestClientException {
    return syncWithHttpInfo(date).getBody();
  }

  /**
   * <b>200</b> - OK
   *
   * @param date (required)
   * @return ResponseEntity&lt;RecoveryResult&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<RecoveryResult> syncWithHttpInfo(LocalDate date)
      throws RestClientException {
    Object localVarPostBody = null;

    // verify the required parameter 'date' is set
    if (date == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST, "Missing the required parameter 'date' when calling sync");
    }

    final MultiValueMap<String, String> localVarQueryParams =
        new LinkedMultiValueMap<String, String>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams =
        new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> localVarFormParams =
        new LinkedMultiValueMap<String, Object>();

    localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "date", date));

    final String[] localVarAccepts = {"*/*"};
    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {};

    ParameterizedTypeReference<RecoveryResult> localReturnType =
        new ParameterizedTypeReference<RecoveryResult>() {};
    return apiClient.invokeAPI(
        "/orange/sync",
        HttpMethod.PUT,
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
