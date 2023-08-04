package school.hei.haapi.integration.conf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.api.TranscriptApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class TranscriptApiMultipart extends TranscriptApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public TranscriptApiMultipart() {
        this(new ApiClient());
    }

    public TranscriptApiMultipart(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }


    private HttpRequest.Builder putStudentTranscriptVersionPdfRequestBuilder(String studentId, String transcriptId, byte[] pdfFile) throws ApiException {
        // verify the required parameter 'studentId' is set
        if (studentId == null) {
            throw new ApiException(400, "Missing the required parameter 'studentId' when calling putStudentTranscriptVersionPdf");
        }
        // verify the required parameter 'transcriptId' is set
        if (transcriptId == null) {
            throw new ApiException(400, "Missing the required parameter 'transcriptId' when calling putStudentTranscriptVersionPdf");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/students/{student_id}/transcripts/{transcript_id}/versions/latest/raw"
                .replace("{student_id}", ApiClient.urlEncode(studentId.toString()))
                .replace("{transcript_id}", ApiClient.urlEncode(transcriptId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    public ApiResponse<StudentTranscriptVersion> putStudentTranscriptVersionPdfWithHttpInfo(String studentId, String transcriptId, byte[] pdfFile) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = putStudentTranscriptVersionPdfRequestBuilder(studentId, transcriptId, pdfFile);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
                    localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            if (localVarResponse.statusCode()/ 100 != 2) {
                throw getApiException("putStudentTranscriptVersionPdf", localVarResponse);
            }
            return new ApiResponse<StudentTranscriptVersion>(
                    localVarResponse.statusCode(),
                    localVarResponse.headers().map(),
                    memberVarObjectMapper.readValue(localVarResponse.body(), new TypeReference<StudentTranscriptVersion>() {})
            );
        } catch (IOException e) {
            throw new ApiException(e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    public StudentTranscriptVersion putStudentTranscriptVersionPdf(String studentId, String transcriptId, byte[] pdfFile) throws ApiException {
        ApiResponse<StudentTranscriptVersion> localVarResponse = putStudentTranscriptVersionPdfWithHttpInfo(studentId, transcriptId, pdfFile);
        return localVarResponse.getData();
    }

}
