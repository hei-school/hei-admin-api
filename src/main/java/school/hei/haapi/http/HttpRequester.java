package school.hei.haapi.http;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;

@Getter
@Component
public class HttpRequester {
    private final HttpClient httpClient = HttpClient.newHttpClient();
}
