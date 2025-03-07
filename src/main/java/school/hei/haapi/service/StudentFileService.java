package school.hei.haapi.service;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToReadable;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToWords;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    File receipt = generatePaidFeeReceipt(payment, template);
    try {
      return Files.readAllBytes(receipt.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<File> generatePaidFeeReceiptsBetween(Instant from, Instant to) {
    List<Payment> allPaymentBetween = paymentService.getAllPayementBetween(from, to);

    return allPaymentBetween.stream()
        .map(
            payment -> {
              File paymentFile = generatePaidFeeReceipt(payment, "paidFeeReceipt");
              saveReceipt(
                  payment.getCreationDatetime().atZone(ZoneId.systemDefault()).toLocalDate(),
                  paymentFile);
              return paymentFile;
            })
        .collect(toUnmodifiableList());
  }

  private String getFormatedBucketKeyForReceipt(LocalDate date, File toSave) {
    return String.format(
        "%s/%s-%s/%s", RECEIPT_FOLDER, date.getYear(), date.getMonth(), toSave.getName());
  }

  private void saveReceipt(LocalDate date, File toSave) {
    String bucketKey = getFormatedBucketKeyForReceipt(date, toSave);
    fileService.uploadObjectToS3Bucket(bucketKey, toSave);
    log.info("zip: '{}' saved successfully", bucketKey);
  }

  private File generatePaidFeeReceipt(Payment payment, String template) {
    Payment localPayment =
        Payment.builder()
            .id(payment.getId())
            .type(payment.getType())
            .fee(payment.getFee())
            .amount(payment.getAmount())
            .isDeleted(payment.isDeleted())
            .creationDatetime(payment.getCreationDatetime())
            .comment(payment.getComment())
            .sequence(payment.getSequence())
            .build();

    if (localPayment.getSequence() == null) {
      LocalDate localPaymentDate =
          localPayment.getCreationDatetime().atZone(ZoneId.systemDefault()).toLocalDate();
      PaymentNumberSequence localPaymentSequence =
          paymentNumberSequenceService.getNextSequence(localPaymentDate);
      localPayment.setSequence(localPaymentSequence);
    }
    paymentService.updateSequence(List.of(localPayment));

    Context context = loadPaymentReceiptContext(localPayment.getFee(), localPayment);
    String html = htmlParser.apply(template, context);
    String filename = RECEIPT_FILENAME_PREFIX + localPayment.getSequence();
    return createFileFromBytes(pdfRenderer.apply(html), filename, ".pdf");
  }

  private File createFileFromBytes(byte[] bytes, String filename, String suffix) {
    try {
      File file = File.createTempFile(filename, suffix);
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
    context.setVariable("receiptNumber", payment.getSequence());
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
