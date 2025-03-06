package school.hei.haapi.service.event;

import static school.hei.haapi.service.event.StudentsWithOverdueFeesReminderService.internetAddress;
import static school.hei.haapi.service.utils.TemplateUtils.htmlToString;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.event.model.SendReceiptZipToEmail;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.service.StudentFileService;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
@Slf4j
public class SendReceiptZipToEmailService implements Consumer<SendReceiptZipToEmail> {
  private final Mailer mailer;
  private final StudentFileService studentFileService;
  private final FileService fileService;

  private Context getMailContext(
      SendReceiptZipToEmail sendReceiptZipToEmail, long fileCount, String presignedUrl) {
    Context initial = new Context();

    initial.setVariable("fileCount", fileCount);
    initial.setVariable("resultUrl", presignedUrl);
    return initial;
  }

  @Override
  public void accept(SendReceiptZipToEmail sendReceiptZipToEmail) {
    List<File> files =
        studentFileService.generatePaidFeeReceiptsBetween(
            sendReceiptZipToEmail.getRequest().getFrom(),
            sendReceiptZipToEmail.getRequest().getTo());

    String presignedUrl =
        fileService.getPresignedUrl(StudentFileService.RECEIPT_FOLDER, 60 * 60 * 24L);

    String htmlBody =
        htmlToString(
            "feeReceiptEmail", getMailContext(sendReceiptZipToEmail, files.size(), presignedUrl));

    mailer.accept(
        new Email(
            internetAddress(sendReceiptZipToEmail.getRequest().getDestinationEmail()),
            List.of(),
            List.of(),
            "HEI - receipts of fee - started at "
                + sendReceiptZipToEmail.getStartRequest(),
            htmlBody,
            List.of()));
    log.info("Send email...");
  }
}
