package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.ACCEPTED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DocumensoMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateDocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocumentStatus;
import school.hei.haapi.endpoint.rest.model.DocumensoFileUrl;
import school.hei.haapi.endpoint.rest.model.DocumensoGenerationLaunched;
import school.hei.haapi.endpoint.rest.model.DocumensoSigningToken;
import school.hei.haapi.endpoint.rest.model.GenerateDocumensoDocuments;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.DocumensoBulkGenerationService;
import school.hei.haapi.service.DocumensoDocumentService;

@RestController
@RequiredArgsConstructor
public class DocumensoDocumentController {
  private final DocumensoDocumentService documensoDocumentService;
  private final DocumensoBulkGenerationService documensoBulkGenerationService;
  private final DocumensoMapper documensoMapper;

  @PostMapping("/documenso-documents")
  public DocumensoDocument generateDocumensoDocument(
      @RequestBody CrupdateDocumensoDocument toCreate,
      @AuthenticationPrincipal Principal principal) {
    var document =
        documensoDocumentService.generateDocument(
            toCreate.getStudentId(), toCreate.getTemplateName(), principal.getUserId());
    return documensoMapper.toRest(document);
  }

  @PostMapping("/promotions/{id}/documenso-documents")
  @ResponseStatus(ACCEPTED)
  public DocumensoGenerationLaunched generateDocumensoDocumentsForPromotion(
      @PathVariable("id") String promotionId,
      @RequestBody GenerateDocumensoDocuments toGenerate,
      @AuthenticationPrincipal Principal principal) {
    var studentCount =
        documensoBulkGenerationService.generateForPromotion(
            promotionId, toGenerate.getTemplateName(), principal.getUserId());
    return new DocumensoGenerationLaunched()
        .promotionId(promotionId)
        .templateName(toGenerate.getTemplateName())
        .studentCount(studentCount);
  }

  @GetMapping("/promotions/{id}/documenso-documents")
  public List<DocumensoDocument> getPromotionDocumensoDocuments(
      @PathVariable("id") String promotionId,
      @RequestParam(name = "level", required = false) StudentLevel level,
      @RequestParam(name = "status", required = false) DocumensoDocumentStatus status,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return documensoDocumentService
        .getByPromotionId(promotionId, level, documensoMapper.toDomain(status), page, pageSize)
        .stream()
        .map(documensoMapper::toRest)
        .toList();
  }

  @GetMapping("/monitors/{id}/documenso-documents")
  public List<DocumensoDocument> getMonitorDocumensoDocuments(
      @PathVariable("id") String monitorId,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return documensoDocumentService.getByMonitorId(monitorId, page, pageSize).stream()
        .map(documensoMapper::toRest)
        .toList();
  }

  @GetMapping("/documenso-documents/{id}/file-url")
  public DocumensoFileUrl getDocumensoDocumentFileUrl(
      @PathVariable("id") String id, @AuthenticationPrincipal Principal principal) {
    return new DocumensoFileUrl()
        .fileUrl(documensoDocumentService.getSignedFileUrl(id, principal.getUserId()));
  }

  @GetMapping("/documenso-documents/{id}/signing-token")
  public DocumensoSigningToken getDocumensoDocumentSigningToken(
      @PathVariable("id") String id, @AuthenticationPrincipal Principal principal) {
    var token = documensoDocumentService.getSigningToken(id, principal.getUserId());
    return new DocumensoSigningToken().token(token);
  }
}
