package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.ScholarshipCertificateUtils.getStudentIntroductionSentence;

import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class StudentFileService {
  private final UserRepository userRepository;

  public byte[] generatePdf(String studentId, String template) {
    Context context = loadContext(studentId);
    String html = parseTemplateToString(context, template);
    return renderPdf(html);
  }

  private byte[] renderPdf(String pdf) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();

    renderer.setDocumentFromString(pdf);
    renderer.layout();

    try {
      renderer.createPDF(outputStream);
    } catch (DocumentException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
    return outputStream.toByteArray();
  }

  private Context loadContext(String studentId) {
    Context context = new Context();
    User student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found"));
    context.setVariable("student", student);
    context.setVariable("introduction", getStudentIntroductionSentence(student));
    return context;
  }

  private String parseTemplateToString(Context context, String template) {
    TemplateEngine templateEngine = configureTemplate();
    return templateEngine.process(template, context);
  }

  private TemplateEngine configureTemplate() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("/templates/");
    resolver.setSuffix(".html");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setTemplateMode(TemplateMode.HTML);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);
    return templateEngine;
  }
}
