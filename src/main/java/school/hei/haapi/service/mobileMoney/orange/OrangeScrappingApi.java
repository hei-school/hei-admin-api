package school.hei.haapi.service.mobileMoney.orange;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.InstantUtils.getYesterday;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.mapper.ExternalExceptionMapper;
import school.hei.haapi.http.mapper.ExternalResponseMapper;
import school.hei.haapi.http.model.OrangeDailyTransactionScrappingDetails;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.MobileTransactionDetails;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.service.MobilePaymentService;
import school.hei.haapi.service.mobileMoney.MobileMoneyApi;

@Component("OrangeScrappingApi")
@AllArgsConstructor
@Slf4j
class OrangeScrappingApi implements MobileMoneyApi {
  private final ObjectMapper objectMapper;
  private final ExternalExceptionMapper exceptionMapper;
  private final ExternalResponseMapper responseMapper;
  private final MobilePaymentService mobilePaymentService;

  private static final String BASE_URL =
      "https://o90a12nuyc.execute-api.eu-west-3.amazonaws.com/Prod";

  @Override
  public List<TransactionDetails> fetchThenSaveTransactionsDetails(MobileMoneyType type) {
    String PATH = "/transactions?date=" + getYesterday();

    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpRequest httpRequest =
          HttpRequest.newBuilder().uri(URI.create(BASE_URL + PATH)).GET().build();

      log.info("Fetching data from = {}", getYesterday());
      HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      exceptionMapper.accept(response);
      var mappedResponseList =
          objectMapper
              .readValue(response.body(), OrangeDailyTransactionScrappingDetails.class)
              .getTransactions()
              .stream()
              .map(responseMapper::from)
              .toList();

      // store the collected data ...
      var savedResponseList =
          mobilePaymentService.saveAll(
              mappedResponseList.stream()
                  .map(responseMapper::toDomainMobileTransactionDetails)
                  .toList());
      log.info(
          "Saved transactions = {}",
          savedResponseList.stream().map(MobileTransactionDetails::getPspTransactionRef).toList());

      return savedResponseList.stream()
          .map(responseMapper::toRestMobileTransactionDetails)
          .toList();
    } catch (IOException | InterruptedException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  @Override
  public TransactionDetails getByTransactionRef(MobileMoneyType type, String ref)
      throws ApiException {
    return responseMapper.toExternalTransactionDetails(
        mobilePaymentService.findTransactionById(ref));
  }
}
