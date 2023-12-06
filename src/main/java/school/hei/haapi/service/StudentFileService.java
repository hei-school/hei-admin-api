package school.hei.haapi.service;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class StudentFileService {
  private final UserRepository userRepository;
  private SpringTemplateEngine templateEngine;
  private final String PDF_SOURCE = "templates/";
  private final UserMapper mapper;

  public byte[] generatePdf(String studentId) {
    Context context = loadContext(studentId);
    String html = parseTemplateToString(context);
    return renderPdf(html);
  }

  private byte[] renderPdf(String pdf) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();
    try {
      renderer.setDocumentFromString(
          pdf, new ClassPathResource(PDF_SOURCE).getURL().toExternalForm());
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
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
    return context;
  }

  private String parseTemplateToString(Context context) {
    return templateEngine.process("scolarity", context);
  }
}
