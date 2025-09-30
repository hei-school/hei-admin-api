package school.hei.haapi.service;

import static java.time.LocalDate.now;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.repository.dao.FileInfoDao;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;
import school.hei.haapi.service.utils.ScholarshipCertificateDataProvider;

@Service
@AllArgsConstructor
public class StudentFileService {
  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;
  private final UserService userService;
  private final ScholarshipCertificateDataProvider certificateDataProvider;
  private final FileInfoRepository fileInfoRepository;
  private final FileInfoService fileInfoService;
  private final WorkDocumentService workDocumentService;
  private final FileInfoDao fileInfoDao;

  public WorkDocument uploadStudentWorkFile(
      String studentId,
      String filename,
      Instant creationDatetime,
      Instant commitmentBegin,
      Instant commitmentEnd,
      MultipartFile workFile,
      ProfessionalExperienceFileTypeEnum professionalExperience) {
    return workDocumentService.uploadStudentWorkFile(
        studentId,
        filename,
        creationDatetime,
        commitmentBegin,
        commitmentEnd,
        workFile,
        professionalExperience);
  }

  public List<WorkDocument> getStudentWorkFiles(
      String studentId,
      ProfessionalExperienceFileTypeEnum professionalExperience,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return workDocumentService.getStudentWorkFiles(studentId, professionalExperience, pageable);
  }

  public WorkDocument getStudentWorkFileById(String workFileId) {
    return workDocumentService.getStudentWorkFileById(workFileId);
  }

  public FileInfo uploadUserFile(
      String fileName, FileType fileType, String userId, MultipartFile fileToUpload) {
    return fileInfoService.uploadFile(fileName, fileType, userId, fileToUpload);
  }

  public List<FileInfo> getUserFiles(
      String userId, FileType fileType, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return fileInfoDao.findAllByCriteria(userId, fileType, pageable);
  }

  public FileInfo getUserFileById(String userId, String id) {
    return fileInfoRepository.getByUserIdAndId(userId, id);
  }

  public byte[] generateScholarshipCertificate(String studentId, String template) {
    Context context = loadContext(studentId);
    String html = htmlParser.apply(template, context);
    return pdfRenderer.apply(html);
  }

  private Context loadContext(String studentId) {
    Resource logo = classPathResourceResolver.apply("HEI_logo", ".png");
    Resource signature = classPathResourceResolver.apply("signature", ".png");
    var student = userService.getById(studentId);
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
