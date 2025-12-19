package school.hei.haapi.model.psp.vola.api.gen.client.api;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.haapi.model.psp.vola.api.gen.client.ApiClient;
import school.hei.haapi.model.psp.vola.api.gen.client.model.Payment;
import school.hei.haapi.model.psp.vola.api.gen.client.model.PaymentInfo;

@Component("school.hei.haapi.model.psp.vola.api.gen.client.api.PaymentControllerApi")
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
   * @param paymentInfos (required)
   * @param apiKey (required)
   * @return List&lt;Payment&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public List<Payment> getPayments(List<PaymentInfo> paymentInfos, String apiKey)
      throws RestClientException {
    return getPaymentsWithHttpInfo(paymentInfos, apiKey).getBody();
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

  /**
   * <b>200</b> - OK
   *
   * @param paymentInfos (required)
   * @param apiKey (required)
   * @return ResponseEntity&lt;List&lt;Payment&gt;&gt;
   * @throws RestClientException if an error occurs while attempting to invoke the API
   */
  public ResponseEntity<List<Payment>> getPaymentsWithHttpInfo(
      List<PaymentInfo> paymentInfos, String apiKey) throws RestClientException {
    Object postBody = paymentInfos;

    if (paymentInfos == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'paymentInfos' when calling getPayments");
    }
    if (apiKey == null) {
      throw new HttpClientErrorException(
          HttpStatus.BAD_REQUEST,
          "Missing the required parameter 'apiKey' when calling getPayments");
    }

    String path =
        UriComponentsBuilder.fromPath("/payments")
            .build()
            .toUriString(); // ← Changé de /payments/batch à /payments

    final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
    final HttpHeaders headerParams = new HttpHeaders();
    final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
    queryParams.putAll(apiClient.parameterToMultiValueMap(null, "apiKey", apiKey));

    final String[] accepts = {"*/*"};
    final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
    final String[] contentTypes = {"application/json"};
    final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {};

    ParameterizedTypeReference<List<Payment>> returnType =
        new ParameterizedTypeReference<List<Payment>>() {};
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
}
