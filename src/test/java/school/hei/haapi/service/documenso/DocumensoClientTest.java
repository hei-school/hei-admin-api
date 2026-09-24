package school.hei.haapi.service.documenso;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
  private String json = "{}";
  private String receivedBody;
  private int statusCode = 200;

  @BeforeEach
  void startServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext("/document/", this::serve);
    server.createContext("/folder", this::serveJson);
    server.createContext("/template", this::serveJson);
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

  private void serveJson(HttpExchange exchange) throws IOException {
    servedUris.add(exchange.getRequestURI().toString());
    receivedBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    var payload = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, payload.length);
    try (var out = exchange.getResponseBody()) {
      out.write(payload);
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

  @Test
  void only_the_folders_of_the_asked_parent_are_kept() {
    /* the second one hangs off another branch: the API's parentId filter is not documented as
     * exclusive, and reusing it would scatter the fiches */
    json =
        """
        {"data":[{"id":"f1","name":"L3","parentId":"year","type":"DOCUMENT"},
                 {"id":"f2","name":"L3","parentId":"elsewhere","type":"DOCUMENT"}]}
        """;

    var folders = subject().findFolders("year", "DOCUMENT");

    assertEquals(1, folders.size());
    assertEquals("f1", folders.getFirst().getId());
    assertTrue(servedUris.getFirst().contains("parentId=year"), servedUris.getFirst());
    assertTrue(servedUris.getFirst().contains("type=DOCUMENT"), servedUris.getFirst());
  }

  @Test
  void the_root_folders_are_asked_for_without_a_parent() {
    json =
        """
        {"data":[{"id":"f1","name":"Fiches","parentId":null,"type":"DOCUMENT"}]}
        """;

    var folders = subject().findFolders(null, "DOCUMENT");

    assertEquals(1, folders.size());
    assertFalse(servedUris.getFirst().contains("parentId"), servedUris.getFirst());
  }

  @Test
  void an_empty_listing_reads_as_no_folder_rather_than_null() {
    json = "{}";

    assertEquals(List.of(), subject().findFolders("year", "DOCUMENT"));
  }

  @Test
  void a_folder_is_created_under_its_parent() {
    json =
        """
        {"id":"new","name":"L3","parentId":"year","type":"DOCUMENT"}
        """;

    var created = subject().createFolder(new CreateRemoteFolder("L3", "year", "DOCUMENT"));

    assertEquals("new", created.getId());
    assertEquals("/folder/create", servedUris.getFirst());
    assertTrue(receivedBody.contains("\"name\":\"L3\""), receivedBody);
    assertTrue(receivedBody.contains("\"parentId\":\"year\""), receivedBody);
  }

  @Test
  void a_root_folder_is_created_without_a_parent_key_at_all() {
    json =
        """
        {"id":"root","name":"Fiches","type":"DOCUMENT"}
        """;

    subject().createFolder(new CreateRemoteFolder("Fiches", null, "DOCUMENT"));

    /* Documenso refuses a null parentId, so the key has to be absent, not empty */
    assertFalse(receivedBody.contains("parentId"), receivedBody);
  }

  @Test
  void the_templates_of_a_folder_are_asked_for_by_folder() {
    /* the generated findTemplates cannot pass a folderId, and Documenso then answers with the root
     * only: a template filed in a folder would silently vanish from the sync */
    json =
        """
        {"data":[{"id":7,"title":"Fiche 2027 L3.pdf"}],"count":1}
        """;

    var page = subject().findTemplatesOfFolder("f1", 1, 100);

    assertEquals(1, page.getData().size());
    assertEquals("Fiche 2027 L3.pdf", page.getData().getFirst().getTitle());
    assertTrue(servedUris.getFirst().contains("folderId=f1"), servedUris.getFirst());
    assertTrue(servedUris.getFirst().contains("perPage=100"), servedUris.getFirst());
  }

  @Test
  void an_empty_folder_yields_an_empty_page_rather_than_a_failure() {
    json =
        """
        {"data":[],"count":0}
        """;

    assertEquals(List.of(), subject().findTemplatesOfFolder("f1", 1, 100).getData());
  }
}
