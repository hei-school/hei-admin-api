package school.hei.haapi.service.documenso;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.repository.DocumensoDocumentRecipientRepository;
import school.hei.haapi.repository.DocumensoDocumentRepository;

@Component
@AllArgsConstructor
public class DocumensoDocumentBuilder {
  private final DocumensoDocumentRepository documensoDocumentRepository;
  private final DocumensoDocumentRecipientRepository documensoDocumentRecipientRepository;
}
