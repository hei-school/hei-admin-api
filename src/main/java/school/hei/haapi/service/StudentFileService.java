package school.hei.haapi.service;

import static java.time.LocalDate.now;
import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;
import static school.hei.haapi.service.utils.ScholarshipCertificateUtils.getAcademicYearPromotion;
import static school.hei.haapi.service.utils.ScholarshipCertificateUtils.getAcademicYearSentence;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;

@Service
@AllArgsConstructor
public class StudentFileService {
  private final UserRepository userRepository;
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;

  public byte[] generatePdf(String studentId, String template) {
    Context context = loadContext(studentId);
    String html = htmlParser.apply(template, context);
    return pdfRenderer.apply(html);
  }

  private Context loadContext(String studentId) {
    ClassPathResourceResolver pathResolver = new ClassPathResourceResolver();
    ClassPathResource logo = pathResolver.apply("HEI_logo", ".png");
    ClassPathResource signature = pathResolver.apply("signature", ".png");
    Base64Converter base64Converter = new Base64Converter();
    Context context = new Context();
    User student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found"));

    context.setVariable("student", student);
    context.setVariable("now", formatLocalDate(now()));
    context.setVariable("academic_sentence", getAcademicYearSentence(student));
    context.setVariable("academic_promotion", getAcademicYearPromotion(student));
    context.setVariable("birthday", formatLocalDate(student.getBirthDate(), "dd/MM/yyyy"));
    context.setVariable("logo", base64Converter.apply(logo));
    context.setVariable("signature", base64Converter.apply(signature));

    return context;
  }
}
