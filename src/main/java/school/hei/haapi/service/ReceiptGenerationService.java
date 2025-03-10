package school.hei.haapi.service;

import static java.nio.file.Files.createDirectories;
import static java.util.stream.Collectors.groupingBy;
import static school.hei.haapi.file.FileZipper.ZIP_FILE_EXTENSION;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToReadable;
import static school.hei.haapi.service.utils.DataFormatterUtils.numberToWords;
import static school.hei.haapi.service.utils.InstantUtils.UTC3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SendReceiptZipToEmail;
import school.hei.haapi.endpoint.rest.model.ZipReceiptsRequest;
import school.hei.haapi.endpoint.rest.model.ZipReceiptsStatistic;
import school.hei.haapi.file.FileZipper;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.PaymentNumberSequence;
import school.hei.haapi.repository.PaymentRepository;
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
  private final EventProducer<SendReceiptZipToEmail> eventProducer;
  private final PaymentNumberSequenceService paymentNumberSequenceService;
  private final FileService fileService;
  private final PaymentRepository paymentRepository;
  private final FileZipper fileZipper;

  public byte[] generatePaidFeeReceiptByPaymentId(String paymentId, String template) {
    Payment payment = paymentService.getById(paymentId);
    try {
      File receipt = generatePaidFeeReceipt(payment, template, Files.createTempDirectory("tmp"));
      return Files.readAllBytes(receipt.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<LocalDate, List<Payment>> groupPaymentByMonthInDateRange(List<Payment> payments) {
    return payments.stream()
        .collect(
            groupingBy(
                payment ->
                    payment.getCreationDatetime().atZone(UTC3).toLocalDate().withDayOfMonth(1)));
  }

  public long generatePaidFeeReceiptsBetween(Instant from, Instant to) throws IOException {
    // Create the main temp folder
    Path tempWorkingDirectory = Files.createTempDirectory(String.format("%s-%s", from, to));

    List<Payment> allPaymentBetween = paymentService.getAllPaymentBetween(from, to);
    List<Path> yearMonthFolders = new ArrayList<>();

    groupPaymentByMonthInDateRange(allPaymentBetween)
        .forEach(
            (startOfAMonth, payments) -> {
              String yearMonthFolderName =
                  String.format("%s-%s", startOfAMonth.getYear(), startOfAMonth.getMonth());
              try {
                // Create a sub folder for each month
                Path yearMonthFolder =
                    createDirectories(tempWorkingDirectory.resolve(yearMonthFolderName));
                payments.forEach(
                    payment -> {
                      // Generation each pdf
                      generatePaidFeeReceipt(payment, "paidFeeReceipt", yearMonthFolder);
                    });
                yearMonthFolders.add(yearMonthFolder);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });

    yearMonthFolders.forEach(
        folder -> {
          File zip = fileZipper.apply(Arrays.stream(folder.toFile().listFiles()).toList());
          zip.renameTo(new File(folder.toAbsolutePath() + ZIP_FILE_EXTENSION));
        });

    saveReceipt(tempWorkingDirectory.toFile());

    return allPaymentBetween.size();
  }

  private void saveReceipt(File toSave) {
    String bucketKey = String.format("%s/%s", RECEIPT_FOLDER, toSave.getName());
    fileService.uploadObjectToS3Bucket(bucketKey, toSave);
    log.info("zip: '{}' saved successfully", bucketKey);
  }

  private File generatePaidFeeReceipt(Payment payment, String template, Path tempWorkingDirectory) {
    if (payment.getSequence() == null) {
      LocalDate localPaymentDate = payment.getCreationDatetime().atZone(UTC3).toLocalDate();
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
        List.of(
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
      case SCHOLARSHIP, FIX ->
          throw new IllegalArgumentException(
              String.format("Payment type must not be %s", typeEnum));
    };
  }
}
