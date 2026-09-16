package school.hei.haapi.service.documenso;

import static school.hei.haapi.service.utils.FileUtils.createFileFromBytes;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import school.hei.haapi.service.documenso.gen.api.DocumentApi;
import school.hei.haapi.service.documenso.gen.api.TemplateApi;
import school.hei.haapi.service.documenso.gen.invoker.ApiClient;
import school.hei.haapi.service.documenso.gen.model.DocumentGet200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplate200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateCreateDocumentFromTemplateRequest;
import school.hei.haapi.service.documenso.gen.model.TemplateFindTemplates200Response;
import school.hei.haapi.service.documenso.gen.model.TemplateGetTemplateById200Response;

public class DocumensoClient {
  private static final String DOWNLOAD_PATH = "/document/{documentId}/download";
  private static final String[] API_KEY_AUTH = {"apiKey"};
  private static final String DOCUMENSO_FILENAME_PREFIX = "documenso-";

  private final ApiClient apiClient;
  private final TemplateApi templateApi;
  private final DocumentApi documentApi;

  public DocumensoClient(String baseUrl, String apiKey) {
    this.apiClient = new ApiClient();
    apiClient.setBasePath(baseUrl);
    apiClient.setApiKey(apiKey);
    this.templateApi = new TemplateApi(apiClient);
    this.documentApi = new DocumentApi(apiClient);
  }

  public TemplateFindTemplates200Response findTemplates(String query, int page, int perPage)
      throws RestClientException {
    return templateApi.templateFindTemplates(
        query, BigDecimal.valueOf(page), BigDecimal.valueOf(perPage));
  }

  public TemplateGetTemplateById200Response getTemplate(long templateId)
      throws RestClientException {
    return templateApi.templateGetTemplateById(BigDecimal.valueOf(templateId));
  }

  public TemplateCreateDocumentFromTemplate200Response useTemplate(
      TemplateCreateDocumentFromTemplateRequest request) throws RestClientException {
    return templateApi.templateCreateDocumentFromTemplate(request);
  }

  public DocumentGet200Response getDocument(long documentId) throws RestClientException {
    return documentApi.documentGet(BigDecimal.valueOf(documentId));
  }

  public File downloadSignedDocument(long documentId) throws RestClientException {
    var pathParams = new HashMap<String, Object>();
    pathParams.put("documentId", BigDecimal.valueOf(documentId));
    var queryParams = new LinkedMultiValueMap<String, String>();
    queryParams.add("version", "signed");

    var signedPdf =
        apiClient
            .invokeAPI(
                DOWNLOAD_PATH,
                HttpMethod.GET,
                pathParams,
                queryParams,
                null,
                new HttpHeaders(),
                new LinkedMultiValueMap<>(),
                new LinkedMultiValueMap<>(),
                List.of(MediaType.APPLICATION_PDF),
                null,
                API_KEY_AUTH,
                new ParameterizedTypeReference<byte[]>() {})
            .getBody();
    if (signedPdf == null || signedPdf.length == 0) {
      throw new RestClientException("Documenso returned an empty signed document " + documentId);
    }
    return storeTemporarily(signedPdf, documentId);
  }

  private static File storeTemporarily(byte[] signedPdf, long documentId) {
    return createFileFromBytes(signedPdf, DOCUMENSO_FILENAME_PREFIX, ".pdf");
  }
}
