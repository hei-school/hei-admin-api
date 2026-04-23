package school.hei.haapi.model.psp.vola.api.gen.client.api;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    var params = new LinkedHashMap<String, Object>();
    params.put("apiKey", apiKey);
    params.put("payerEmail", payerEmail);
    params.put("pspType", pspType);
    params.put("pspPaymentId", pspPaymentId);

    validateRequiredParams(params, "createPayment");
    return invoke(
        "/payment",
        HttpMethod.POST,
        null,
        params,
        new String[] {"*/*"},
        new String[] {},
        new ParameterizedTypeReference<Payment>() {});
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

    var params = new LinkedHashMap<String, Object>();
    params.put("apiKey", apiKey);
    params.put("payerEmail", payerEmail);
    params.put("pspType", pspType);
    params.put("pspPaymentId", pspPaymentId);

    validateRequiredParams(params, "getPayment");

    return invoke(
        "/payment",
        HttpMethod.GET,
        null,
        params,
        new String[] {"*/*"},
        new String[] {},
        new ParameterizedTypeReference<Payment>() {});
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
    var params = new LinkedHashMap<String, Object>();
    params.put("apiKey", apiKey);
    params.put("paymentInfo", paymentInfo);

    validateRequiredParams(params, "getPayments");

    return invoke(
        "/payments/search",
        HttpMethod.PUT,
        paymentInfo,
        params,
        new String[] {"*/*"},
        new String[] {"application/json"},
        new ParameterizedTypeReference<List<Payment>>() {});
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
    var params = new LinkedHashMap<String, Object>();
    params.put("date", date);
    validateRequiredParams(params, "sync");

    return invoke(
        "/orange/sync",
        HttpMethod.PUT,
        null,
        params,
        new String[] {"*/*"},
        new String[] {},
        new ParameterizedTypeReference<RecoveryResult>() {});
  }

  private <T> ResponseEntity<T> invoke(
      String path,
      HttpMethod method,
      Object body,
      Map<String, Object> queryParams,
      String[] accepts,
      String[] contentTypes,
      ParameterizedTypeReference<T> returnType) {
    final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<>();
    final HttpHeaders localVarHeaderParams = new HttpHeaders();
    final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<>();
    final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<>();

    if (queryParams != null) {
      queryParams.forEach(
          (key, value) ->
              localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, key, value)));
    }

    final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(accepts);
    final MediaType localVarContentType = apiClient.selectHeaderContentType(contentTypes);

    var localVarAuthNames = new String[] {};

    return apiClient.invokeAPI(
        path,
        method,
        Collections.emptyMap(),
        localVarQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAccept,
        localVarContentType,
        localVarAuthNames,
        returnType);
  }

  private void validateRequiredParams(Map<String, ?> params, String methodName) {
    params.forEach(
        (name, value) -> {
          if (value == null) {
            throw new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Missing the required parameter '" + name + "'" + " when calling " + methodName);
          }
        });
  }
}
