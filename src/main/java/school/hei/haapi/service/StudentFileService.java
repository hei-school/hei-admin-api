package school.hei.haapi.service;

import static java.time.LocalDate.now;
import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.model.File;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FileRepository;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;
import school.hei.haapi.service.utils.ScholarshipCertificateDataProvider;
import java.util.List;

@Service
@AllArgsConstructor
public class StudentFileService {
  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;
  private final UserService userService;
  private final ScholarshipCertificateDataProvider certificateDataProvider;
  private final FileRepository fileRepository;
  private final FileService fileService;

  public File uploadStudentFile(String fileName, FileType fileType, String studentId, byte[] fileToUpload) {
    return fileService.uploadFile(fileName, fileType, studentId, fileToUpload);
  }

  public List<File> getStudentFiles(String userId) {
    User student = userService.findById(userId);
    return fileRepository.findAllByUser(student);
  }

  public File getStudentFileById(String studentId, String id) {
    return fileRepository.getByUserIdAndId(studentId, id);
  }

  public byte[] generatePdf(String studentId, String template) {
    Context context = loadContext(studentId);
    String html = htmlParser.apply(template, context);
    return pdfRenderer.apply(html);
  }

  private Context loadContext(String studentId) {
    Resource logo = classPathResourceResolver.apply("HEI_logo", ".png");
    Resource signature = classPathResourceResolver.apply("signature", ".png");
    User student = userService.findById(studentId);
    Context context = new Context();

    context.setVariable("student", student);
    context.setVariable("now", formatLocalDate(now()));
    context.setVariable(
        "academic_sentence", certificateDataProvider.getAcademicYearSentence(student));
    context.setVariable(
        "academic_promotion", certificateDataProvider.getAcademicYearPromotion(student));
    context.setVariable("birthday", formatLocalDate(student.getBirthDate(), "dd/MM/yyyy"));
    context.setVariable("logo", base64Converter.apply(logo));
    context.setVariable("signature", base64Converter.apply(signature));

    return context;
  }
}
