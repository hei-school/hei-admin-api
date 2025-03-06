package school.hei.haapi.service.event;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.service.event.StudentsWithOverdueFeesReminderService.internetAddress;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.SendReceiptZipToEmail;
import school.hei.haapi.file.FileZipper;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.service.StudentFileService;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
@Slf4j
public class SendReceiptZipToEmailService implements Consumer<SendReceiptZipToEmail> {
  private final Mailer mailer;
  private final FileZipper fileZipper;
  private final StudentFileService studentFileService;
  private final FileService fileService;

  private Context getMailContext(SendReceiptZipToEmail sendReceiptZipToEmail) {
    Context initial = new Context();

    initial.setVariable("fileCount", sendReceiptZipToEmail.getPaymentIdsToZip().size());
    return initial;
  }

  @Override
  public void accept(SendReceiptZipToEmail sendReceiptZipToEmail) {
    String htmlBody = htmlToString("feeReceiptEmail", getMailContext(sendReceiptZipToEmail));

    File zip =
        fileZipper.apply(
            sendReceiptZipToEmail.getPaymentIdsToZip().stream()
                .map(studentFileService::paymentToReceiptPdf)
                .collect(toUnmodifiableList()));

    saveFile(LocalDate.from(sendReceiptZipToEmail.getStartRequest()), zip);

    mailer.accept(
        new Email(
            internetAddress("contact@mail.hei.school"),
            List.of(internetAddress(sendReceiptZipToEmail.getEmailRecipient())),
            List.of(),
            "HEI - receipts of fee - start at "
                + sendReceiptZipToEmail.getStartRequest()
                + " - Number - "
                + sendReceiptZipToEmail.getIdWork(),
            htmlBody,
            List.of(zip)));
    log.info("Send email...");
  }

  private void saveFile(LocalDate date, File toSave) {
    String bucketKey =
        String.format("RECEIPT/%s-%s/%s.zip", date.getYear(), date.getMonth(), toSave.getName());
    fileService.uploadObjectToS3Bucket(bucketKey, toSave);
    log.info("zip: '{}' saved successfully", bucketKey);
  }
}
