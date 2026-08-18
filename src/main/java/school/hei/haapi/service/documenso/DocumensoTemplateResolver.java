package school.hei.haapi.service.documenso;

import java.text.Normalizer;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.TemplateDocumenso;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.ApiException.ExceptionType;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.TemplateDocumensoRepository;

@Component
@AllArgsConstructor
public class DocumensoTemplateResolver {
  private final TemplateDocumensoRepository templateDocumensoRepository;

  public TemplateDocumenso resolveByName(String templateName, StudentLevel level) {
    var candidates = templateDocumensoRepository.findAllByTitleContainingIgnoreCase(templateName);
    if (candidates.isEmpty()) {
      throw new NotFoundException("No synced Documenso template matching: " + templateName);
    }
    if (candidates.size() == 1) {
      return candidates.getFirst();
    }
    if (level != null) {
      var matchingLevel =
          candidates.stream()
              .filter(
                  candidate -> normalize(candidate.getTitle()).contains(normalize(level.name())))
              .toList();
      if (matchingLevel.size() == 1) {
        return matchingLevel.getFirst();
      }
    }
    throw new ApiException(
        ExceptionType.SERVER_EXCEPTION,
        "Several Documenso templates match \""
            + templateName
            + "\" and the student's level doesn't disambiguate them: "
            + candidates.stream().map(TemplateDocumenso::getTitle).toList());
  }

  private static String normalize(String value) {
    var withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return withoutAccents.toLowerCase(Locale.FRENCH);
  }
}
