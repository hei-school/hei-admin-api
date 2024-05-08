package school.hei.haapi.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@Component
public class HttpClient {
    private String baseURL;
    private String bearer;

    public void setToken(String token) {
        this.bearer = "Bearer " + token;
    }
}
