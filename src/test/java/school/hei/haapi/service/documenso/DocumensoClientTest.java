package school.hei.haapi.service.documenso;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class DocumensoClientTest {
  private static final byte[] A_PDF = "%PDF-1.7 signed".getBytes(StandardCharsets.UTF_8);

  private HttpServer server;
  private final List<String> servedUris = new ArrayList<>();
  private byte[] body = A_PDF;
  private int statusCode = 200;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/document/", this::serve);
    server.start();
  }

  @AfterEach
  void stopServer() {
    server.stop(0);
  }

  private void serve(HttpExchange exchange) throws IOException {
    servedUris.add(exchange.getRequestURI().toString());
    exchange.getResponseHeaders().add("Content-Type", "application/pdf");
    exchange.sendResponseHeaders(statusCode, body.length == 0 ? -1 : body.length);
    try (var out = exchange.getResponseBody()) {
      out.write(body);
    }
  }

  private DocumensoClient subject() {
    return new DocumensoClient("http://localhost:" + server.getAddress().getPort(), "an-api-key");
  }

  @Test
  void a_signed_document_is_downloaded_as_a_pdf_file() throws IOException {
    var downloaded = subject().downloadSignedDocument(42L);

    assertArrayEquals(A_PDF, Files.readAllBytes(downloaded.toPath()));
    assertTrue(downloaded.getName().endsWith(".pdf"), downloaded.getName());
    assertEquals(List.of("/document/42/download?version=signed"), servedUris);
  }

  @Test
  void an_empty_response_is_rejected_rather_than_archived() {
    body = new byte[0];

    assertThrows(RestClientException.class, () -> subject().downloadSignedDocument(42L));
  }
}
