package school.hei.haapi.endpoint.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.DocumensoMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateDocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoDocument;
import school.hei.haapi.endpoint.rest.model.DocumensoSigningToken;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.service.DocumensoDocumentService;

@RestController
@RequiredArgsConstructor
public class DocumensoDocumentController {
  private final DocumensoDocumentService documensoDocumentService;
  private final DocumensoMapper documensoMapper;

  @PostMapping("/documenso-documents")
  public DocumensoDocument generateDocumensoDocument(
      @RequestBody CrupdateDocumensoDocument toCreate) {
    var document =
        documensoDocumentService.generateDocument(
            toCreate.getStudentId(), toCreate.getTemplateName());
    return documensoMapper.toRest(document);
  }

  @GetMapping("/documenso-documents/{id}/signing-token")
  public DocumensoSigningToken getDocumensoDocumentSigningToken(
      @PathVariable("id") String id, @AuthenticationPrincipal Principal principal) {
    var token = documensoDocumentService.getSigningToken(id, principal.getUserId());
    return new DocumensoSigningToken().token(token);
  }
}
