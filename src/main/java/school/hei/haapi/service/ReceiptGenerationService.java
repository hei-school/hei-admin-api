package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;
import static school.hei.haapi.service.event.StudentsWithOverdueFeesReminderService.internetAddress;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToReadable;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToWords;
import static school.hei.haapi.service.utils.InstantUtils.UTC3;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.HandleReceiptGenerationRequest;
import school.hei.haapi.endpoint.rest.model.GeneratedReceiptsStatistic;
import school.hei.haapi.endpoint.rest.model.GenerationReceiptsRequest;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.dto.PaymentDto;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PaidFeeReceiptDataProvider;
import school.hei.haapi.service.utils.PdfRenderer;

@Service
@AllArgsConstructor
@Slf4j
public class ReceiptGenerationService {
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
  private final PaymentService paymentService;
  private final EventProducer<HandleReceiptGenerationRequest> eventProducer;
  private final FileService fileService;
  private final Mailer mailer;

  public byte[] generatePaidFeeReceiptByPaymentId(String paymentId) {
    Payment payment =
        paymentService.updateSequence(PaymentDto.from(paymentService.getById(paymentId)));
    try {
      File receipt = generatePaidFeeReceipt(PaymentDto.from(payment));
      return Files.readAllBytes(receipt.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private LocalDate getStartOfTheMonthOf(Payment payment) {
    return payment.getCreationDatetime().atZone(UTC3).toLocalDate().withDayOfMonth(1);
  }

  public void saveReceipt(File toSave, PaymentDto paymentDto) {
    String fileKey =
        RECEIPT_FILENAME_PREFIX + paymentDto.getSequence().getStringSequence() + ".pdf";
    String bucketKey =
        String.format("%s/%s/%s", RECEIPT_FOLDER, paymentDto.getSequence().getYearMonth(), fileKey);
    fileService.uploadObjectToS3Bucket(bucketKey, toSave);
    log.info("zip: '{}' saved successfully", bucketKey);
  }

  public File generatePaidFeeReceipt(PaymentDto paymentDto) {
    Context context = loadPaymentReceiptContext(paymentDto);
    String html = htmlParser.apply("paidFeeReceipt", context);
    String filename = RECEIPT_FILENAME_PREFIX + paymentDto.getSequence().getStringSequence();
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

  private void sendResultEmail(long fileCount, String presignedUrl, String destinationEmail) {
    Context initial = new Context();
    initial.setVariable("fileCount", fileCount);
    initial.setVariable("resultUrl", presignedUrl);

    mailer.accept(
        new Email(
            internetAddress(destinationEmail),
            List.of(),
            List.of(),
            "HEI - receipts of fee - started at " + Instant.now(),
            htmlToString("feeReceiptEmail", initial),
            List.of()));

    log.info(
        "{} file(s) are upload as zip in S3: {} and email has been sent to {}",
        fileCount,
        presignedUrl,
        destinationEmail);
  }

  public GeneratedReceiptsStatistic getZipFeeReceipts(
      GenerationReceiptsRequest generationReceiptsRequest) {
    List<PaymentDto> allPayments =
        paymentService
            .getAllPaymentBetween(
                generationReceiptsRequest.getFrom(), generationReceiptsRequest.getTo())
            .stream()
            .map(PaymentDto::from)
            .collect(toUnmodifiableList());

    eventProducer.accept(
        List.of(
            HandleReceiptGenerationRequest.builder()
                .notifyEmail(getPrincipal().getUser().getEmail())
                // TODO: Put a limit on how many payment should be handled by each event
                .payments(allPayments)
                .build()));

    log.info("Pdf to be generated: {}", allPayments.size());
    return new GeneratedReceiptsStatistic().processedFileCount(allPayments.size());
  }

  private Context loadPaymentReceiptContext(PaymentDto paymentDto) {
    Resource logo = classPathResourceResolver.apply("HEI_logo", ".png");
    Context context = new Context();
    List<PaymentDto> paidPaymentsBefore =
        paymentService.getByFeeIdOrderByCreationDatetimeAsc(paymentDto.getFee().getId()).stream()
            .map(PaymentDto::from)
            .collect(toUnmodifiableList());
    PaidFeeReceiptDataProvider dataProvider =
        new PaidFeeReceiptDataProvider(paymentDto, paidPaymentsBefore);

    context.setVariable("logo", base64Converter.apply(logo));
    context.setVariable("paymentAuthorName", dataProvider.getEntirePaymentAuthorName());
    context.setVariable("receiptNumber", paymentDto.getSequence().getStringSequence());
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
      case SCHOLARSHIP, FIX ->
          throw new IllegalArgumentException(
              String.format("Payment type must not be %s", typeEnum));
    };
  }
}
