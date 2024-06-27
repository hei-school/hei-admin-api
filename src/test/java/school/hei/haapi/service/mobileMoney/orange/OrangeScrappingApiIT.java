package school.hei.haapi.service.mobileMoney.orange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.http.model.OrangeDailyTransactionScrappingDetails;
import school.hei.haapi.http.model.OrangeTransactionScrappingDetails;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.integration.StudentIT;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.endpoint.rest.model.MobileMoneyType.ORANGE_MONEY;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = OrangeScrappingApiIT.ContextInitializer.class)
@AutoConfigureMockMvc
class OrangeScrappingApiIT extends MockedThirdParties {
    @Autowired OrangeScrappingApi subject;
    @Autowired
    ObjectMapper objectMapper;
    private HttpClient httpClientMock;

    public static <T> HttpResponse<T> httpResponseMock(T body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<T>> previousResponse() {
                return Optional.empty();
            }
            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public T body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }
            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
            @BeforeEach
    void setUp() throws IOException, InterruptedException {
        httpClientMock = mock(HttpClient.class);
        when(httpClientMock.send(any(), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(httpResponseMock(objectMapper.writeValueAsString(returnedHttpResponse())));
    }

    @Test
    void fetchThenSaveTransactionsDetails() {
        try (MockedStatic<HttpClient> mockedHttpClient = Mockito.mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(httpClientMock);
            List<TransactionDetails> actual = subject.fetchThenSaveTransactionsDetails(ORANGE_MONEY);

            assertEquals(expectedFetchedTransactionDetails(), actual);
        }
    }

    @Test
    void getByTransactionRef() {
        TransactionDetails actual = subject.getByTransactionRef(ORANGE_MONEY, "psp2_id");

        assertEquals(expectedFetchedTransactionDetails().get(0), actual);
    }

    private OrangeDailyTransactionScrappingDetails returnedHttpResponse() {
        return OrangeDailyTransactionScrappingDetails
                .builder()
                .timestamp(Instant.now())
                .transactionDate("2024-06-12T08:25:24.00Z")
                .transactions(returnedTransactionList())
                .build();
    }

    private List<OrangeTransactionScrappingDetails> returnedTransactionList() {
        return List.of(OrangeTransactionScrappingDetails
                .builder()
                        .ref("psp2_id")
                        .date("12/06/2024")
                        .clientNumber("0000000")
                        .number(1)
                        .amount(300)
                        .status("Succés")
                        .time("10:16:07")
                .build());
    }

    private List<TransactionDetails> expectedFetchedTransactionDetails() {
        return List.of(TransactionDetails
                .builder()
                        .pspDatetimeTransactionCreation(Instant.parse("2024-06-12T00:00:00.00Z"))
                        .pspTransactionRef("psp2_id")
                        .pspTransactionAmount(300)
                .build());
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }
}