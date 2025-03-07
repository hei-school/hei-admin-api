package school.hei.haapi.service;

import static java.time.LocalDate.now;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.file.FileZipper.ZIP_FILE_EXTENSION;
import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToReadable;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToWords;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SendReceiptZipToEmail;
import school.hei.haapi.endpoint.rest.model.FileType;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.endpoint.rest.model.ZipReceiptsRequest;
import school.hei.haapi.endpoint.rest.model.ZipReceiptsStatistic;
import school.hei.haapi.file.FileZipper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.PaymentNumberSequence;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.repository.FileInfoRepository;
import school.hei.haapi.repository.PaymentRepository;
import school.hei.haapi.repository.dao.FileInfoDao;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PaidFeeReceiptDataProvider;
import school.hei.haapi.service.utils.PdfRenderer;
import school.hei.haapi.service.utils.ScholarshipCertificateDataProvider;

@Service
@AllArgsConstructor
@Slf4j
public class StudentFileService {
  /*
  if pdf=27.3Ko and take 0.38s to be created
  => the total is approximately 43 Mo and 9 min 53 sec
  */
  public static final int MAX_RECEIPT_PDF_IN_ZIP_FILE = 1_560;
  private final String RECEIPT_FILENAME_PREFIX = "reçu-";
  public static final String RECEIPT_FOLDER = "RECEIPT";

  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;
  private final UserService userService;
  private final PaymentService paymentService;
  private final ScholarshipCertificateDataProvider certificateDataProvider;
  private final FileInfoRepository fileInfoRepository;
  private final FileInfoService fileInfoService;
  private final WorkDocumentService workDocumentService;
  private final FileInfoDao fileInfoDao;
  private final EventProducer<SendReceiptZipToEmail> eventProducer;
  private final PaymentNumberSequenceService paymentNumberSequenceService;
  private final FileService fileService;
  private final PaymentRepository paymentRepository;
  private final FileZipper fileZipper;

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

  public byte[] generatePaidFeeReceiptByPaymentId(String paymentId, String template) {
    Payment payment = paymentService.getById(paymentId);
    try {
      File receipt = generatePaidFeeReceipt(payment, template, Files.createTempDirectory("tmp"));
      return Files.readAllBytes(receipt.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public long generatePaidFeeReceiptsBetween(Instant from, Instant to) throws IOException {
    List<Payment> allPaymentBetween = paymentService.getAllPayementBetween(from, to);

    Path tempWorkingDirectory = Files.createTempDirectory(String.format("%s-%s", from, to));

    List<Path> yearMonthFolders = new ArrayList<>();

    for (Payment payment : allPaymentBetween) {
      LocalDate startOfThMonth =
          payment.getCreationDatetime().atZone(ZoneId.of("UTC+3")).toLocalDate().withDayOfMonth(1);
      String yearMonthFolderName =
          String.format("%s-%s", startOfThMonth.getYear(), startOfThMonth.getMonth());
      Path yearMonthFolder = tempWorkingDirectory.resolve(yearMonthFolderName);
      if (!yearMonthFolder.toFile().exists()) {
        yearMonthFolders.add(Files.createDirectories(yearMonthFolder));
      }
      generatePaidFeeReceipt(payment, "paidFeeReceipt", yearMonthFolder);
    }

    yearMonthFolders.forEach(
        folder -> {
          File zip = fileZipper.apply(Arrays.stream(folder.toFile().listFiles()).toList());
          zip.renameTo(new File(folder.toAbsolutePath() + ZIP_FILE_EXTENSION));
          saveReceipt(zip);
        });

    return allPaymentBetween.size();
  }

  private void saveReceipt(File toSave) {
    String bucketKey = String.format("%s/%s", RECEIPT_FOLDER, toSave.getName());
    fileService.uploadObjectToS3Bucket(bucketKey, toSave);
    log.info("zip: '{}' saved successfully", bucketKey);
  }

  private File generatePaidFeeReceipt(Payment payment, String template, Path tempWorkingDirectory) {
    if (payment.getSequence() == null) {
      LocalDate localPaymentDate =
          payment.getCreationDatetime().atZone(ZoneId.of("UTC+3")).toLocalDate();
      PaymentNumberSequence localPaymentSequence =
          paymentNumberSequenceService.getNextSequence(localPaymentDate);
      paymentService.updateSequence(payment.getId(), localPaymentSequence);
    }

    Context context = loadPaymentReceiptContext(payment.getFee(), payment);
    String html = htmlParser.apply(template, context);
    String filename = RECEIPT_FILENAME_PREFIX + payment.getSequence();
    return createFileFromBytes(pdfRenderer.apply(html), filename, tempWorkingDirectory);
  }

  private File createFileFromBytes(byte[] bytes, String filename, Path tempWorkingDirectory) {
    try {
      File file = tempWorkingDirectory.resolve(filename).toFile();
      file.createNewFile();
      FileUtils.writeByteArrayToFile(file, bytes);
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ZipReceiptsStatistic getZipFeeReceipts(ZipReceiptsRequest zipReceiptsRequest) {
    eventProducer.accept(
        Collections.singleton(
            SendReceiptZipToEmail.builder()
                .startRequest(Instant.now())
                .request(zipReceiptsRequest)
                .build()));

    return new ZipReceiptsStatistic()
        .fileCountInZip(
            paymentRepository.countByCreationDatetimeBetweenOrderByCreationDatetimeAsc(
                zipReceiptsRequest.getFrom(), zipReceiptsRequest.getTo()));
  }

  private Context loadPaymentReceiptContext(Fee fee, Payment payment) {
    Resource logo = classPathResourceResolver.apply("HEI_logo", ".png");
    Context context = new Context();
    List<Payment> paidPaymentsBefore =
        paymentService.getByFeeIdOrderByCreationDatetimeAsc(fee.getId());
    PaidFeeReceiptDataProvider dataProvider =
        new PaidFeeReceiptDataProvider(fee.getStudent(), fee, payment, paidPaymentsBefore);

    context.setVariable("logo", base64Converter.apply(logo));
    context.setVariable("paymentAuthorName", dataProvider.getEntirePaymentAuthorName());
    context.setVariable("receiptNumber", payment.getSequence().getStringSequence());
    context.setVariable("totalAmount", numberToReadable(dataProvider.getFeeTotalAmount()));
    context.setVariable("paymentDate", dataProvider.getPaymentDate());
    context.setVariable("paymentAmount", numberToReadable(dataProvider.getTotalPaymentAmount()));
    context.setVariable("remainingAmount", numberToReadable(dataProvider.getRemainingAmount()));
    context.setVariable(
        "paymentAmountAsWords", numberToWords(dataProvider.getTotalPaymentAmount()));
    context.setVariable("paymentReason", dataProvider.getFeeComment());
    context.setVariable("paymentType", paymentType(dataProvider.getPaymentType()));

    return context;
  }

  private String paymentType(school.hei.haapi.endpoint.rest.model.Payment.TypeEnum typeEnum) {
    return switch (typeEnum) {
      case BANK_TRANSFER -> "Virement bancaire";
      case CASH -> "En espèce";
      case MOBILE_MONEY -> "Mobile Money";
      case SCHOLARSHIP, FIX -> null;
    };
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
