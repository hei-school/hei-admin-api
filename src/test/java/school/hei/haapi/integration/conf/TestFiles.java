package school.hei.haapi.integration.conf;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static software.amazon.awssdk.core.internal.util.ChunkContentUtils.CRLF;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.shaded.com.google.common.primitives.Bytes;
import school.hei.haapi.model.exception.ApiException;

/** Reads the fixture files under {@code src/test/resources/mock} and posts them as multipart. */
public class TestFiles {

  public static File getMockedFile(String fileName, String extension) {
    try {
      var resource = new ClassPathResource("mock/" + fileName + extension);
      return resource.getFile();
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  public static byte[] getMockedFileAsByte(String fileName, String extension) {
    try {
      var file = getMockedFile(fileName, extension);
      return FileUtils.readFileToByteArray(file);
    } catch (IOException ioException) {
      throw new ApiException(SERVER_EXCEPTION, ioException.getMessage());
    }
  }

  public static HttpResponse<byte[]> requestFile(URI request, String token)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newBuilder().build();

    return httpClient.send(
        HttpRequest.newBuilder()
            .uri(request)
            .GET()
            .header("Authorization", "Bearer " + token)
            .build(),
        HttpResponse.BodyHandlers.ofByteArray());
  }

  public static HttpResponse<InputStream> uploadProfilePicture(
      Integer serverPort, String token, String subjectId, String resource)
      throws IOException, InterruptedException {
    var client = HttpClient.newHttpClient();

    var basePath = "http://localhost:" + serverPort;

    var boundary = "---------------------------" + System.currentTimeMillis();
    var contentTypeHeader = "multipart/form-data; boundary=" + boundary;

    var file = getMockedFile("img", ".png");

    var requestBodyPrefix =
        "--"
            + boundary
            + CRLF
            + "Content-Disposition: form-data; name=\"file_to_upload\"; filename=\""
            + file.getName()
            + "\""
            + CRLF
            + "Content-Type: image/png"
            + CRLF
            + CRLF;
    byte[] fileBytes = Files.readAllBytes(Paths.get(file.getPath()));
    var requestBodySuffix = CRLF + "--" + boundary + "--" + CRLF;

    byte[] requestBody =
        Bytes.concat(requestBodyPrefix.getBytes(), fileBytes, requestBodySuffix.getBytes());
    var uriComponentsBuilder =
        UriComponentsBuilder.fromUri(
            URI.create(basePath + String.format("/%s/%s/picture/raw", resource, subjectId)));
    var request =
        HttpRequest.newBuilder()
            .uri(uriComponentsBuilder.build().toUri())
            .header("Content-Type", contentTypeHeader)
            .header("Authorization", "Bearer " + token)
            // a known length keeps the request out of chunked encoding: when the server denies it
            // before reading the body, a chunked request dies on a closed connection instead of
            // surfacing the status
            .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
  }

  public static HttpResponse<InputStream> uploadLetter(
      Integer serverPort,
      String token,
      String subjectId,
      String description,
      String filename,
      String feeId,
      Integer amount,
      String eventParticipantId)
      throws IOException, InterruptedException {
    var client = HttpClient.newHttpClient();

    var basePath = "http://localhost:" + serverPort;

    var boundary = "---------------------------" + System.currentTimeMillis();
    var contentTypeHeader = "multipart/form-data; boundary=" + boundary;

    var file = getMockedFile("img", ".png");

    var filePart =
        "--"
            + boundary
            + CRLF
            + "Content-Disposition: form-data; name=\"file_to_upload\"; filename=\""
            + file.getName()
            + "\""
            + CRLF
            + "Content-Type: image/png"
            + CRLF
            + CRLF;
    byte[] fileBytes = Files.readAllBytes(Paths.get(file.getPath()));
    var requestBodySuffix = CRLF + "--" + boundary + "--" + CRLF;

    var descriptionPart =
        "--"
            + boundary
            + CRLF
            + "Content-Disposition: form-data; name=\"description\""
            + CRLF
            + CRLF
            + description
            + CRLF;
    byte[] requestBody =
        Bytes.concat(
            descriptionPart.getBytes(),
            filePart.getBytes(),
            fileBytes,
            requestBodySuffix.getBytes());

    var path = basePath + String.format("/users/%s/letters?filename=%s", subjectId, filename);

    if (Objects.nonNull(feeId)) {
      path = path + "&fee_id=" + feeId;
    }

    if (Objects.nonNull(amount)) {
      path = path + "&amount=" + amount;
    }

    if (Objects.nonNull(eventParticipantId)) {
      path = path + "&event_participant_id=" + eventParticipantId;
    }

    var uriComponentsBuilder = UriComponentsBuilder.fromUri(URI.create(path));
    var requestBodyStream = new ByteArrayInputStream(requestBody);
    var request =
        HttpRequest.newBuilder()
            .uri(uriComponentsBuilder.build().toUri())
            .header("Content-Type", contentTypeHeader)
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofInputStream(() -> requestBodyStream))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
  }
}
