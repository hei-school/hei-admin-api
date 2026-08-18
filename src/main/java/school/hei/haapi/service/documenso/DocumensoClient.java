package school.hei.haapi.service.documenso;

import java.io.File;
import java.math.BigDecimal;
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
  private final TemplateApi templateApi;
  private final DocumentApi documentApi;

  public DocumensoClient(String baseUrl, String apiKey) {
    var apiClient = new ApiClient();
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
    return documentApi.documentDownload(BigDecimal.valueOf(documentId), "signed");
  }
}
