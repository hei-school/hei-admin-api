package school.hei.haapi.service.sms;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.hei.haapi.endpoint.rest.model.SmsFileImportRejectedRow;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.SmsRecipientSource;
import school.hei.haapi.model.dto.SmsFileImportRowDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.SmsFileRowsRejectedException;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.service.utils.excel.ExcelParser;

@Slf4j
@org.springframework.stereotype.Component
@AllArgsConstructor
public class SmsRecipientResolver {
  private static final String BUCKET_FOLDER = "SMS_CAMPAIGN";
  private static final DateTimeFormatter MONTH_FOLDER =
      DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);

  private final SmsContactRepository smsContactRepository;
  private final SmsContactGroupRepository smsContactGroupRepository;
  private final BucketComponent bucketComponent;

  public record Resolved(
      List<ResolvedRecipient> recipients,
      List<SmsContactGroup> contactGroups,
      List<SmsContact> manuallySelectedContacts,
      int fileImportCount,
      String fileBucketKey) {}

  public Resolved resolve(
      List<String> contactGroupIds,
      List<String> contactIds,
      List<String> manualPhoneNumbers,
      File file,
      String originalFilename) {
    var byPhoneNumber = new LinkedHashMap<String, ResolvedRecipient>();

    var groups = addContactGroups(contactGroupIds, byPhoneNumber);
    var manuallySelectedContacts = addManualSelection(contactIds, byPhoneNumber);
    addManualNumbers(manualPhoneNumbers, byPhoneNumber);
    var fileOutcome = addFile(file, originalFilename, byPhoneNumber);

    if (byPhoneNumber.isEmpty()) {
      throw new BadRequestException(
          "At least one recipient source (contactGroupIds, contactIds, manualPhoneNumbers or file)"
              + " is required");
    }

    return new Resolved(
        List.copyOf(byPhoneNumber.values()),
        groups,
        manuallySelectedContacts,
        fileOutcome.count(),
        fileOutcome.bucketKey());
  }

  private List<SmsContactGroup> addContactGroups(
      List<String> contactGroupIds, LinkedHashMap<String, ResolvedRecipient> byPhoneNumber) {
    if (contactGroupIds == null || contactGroupIds.isEmpty()) {
      return List.of();
    }
    var groups = smsContactGroupRepository.findAllByIdInAndIsDeletedFalse(contactGroupIds);
    for (var group : groups) {
      for (var contact : group.getMembers()) {
        byPhoneNumber.putIfAbsent(
            contact.getPhoneNumber(),
            new ResolvedRecipient(
                contact.getPhoneNumber(), SmsRecipientSource.CONTACT_GROUP, contact.getId(), null));
      }
    }
    return groups;
  }

  private List<SmsContact> addManualSelection(
      List<String> contactIds, LinkedHashMap<String, ResolvedRecipient> byPhoneNumber) {
    if (contactIds == null || contactIds.isEmpty()) {
      return List.of();
    }
    var contacts = smsContactRepository.findAllByIdInAndIsDeletedFalse(contactIds);
    for (var contact : contacts) {
      byPhoneNumber.putIfAbsent(
          contact.getPhoneNumber(),
          new ResolvedRecipient(
              contact.getPhoneNumber(),
              SmsRecipientSource.MANUAL_SELECTION,
              contact.getId(),
              null));
    }
    return contacts;
  }

  private void addManualNumbers(
      List<String> manualPhoneNumbers, LinkedHashMap<String, ResolvedRecipient> byPhoneNumber) {
    if (manualPhoneNumbers == null || manualPhoneNumbers.isEmpty()) {
      return;
    }
    for (var raw : manualPhoneNumbers) {
      var normalized = SmsFileImportRowDto.normalizePhoneNumber(raw);
      if (normalized.length() < 6) {
        continue;
      }
      byPhoneNumber.putIfAbsent(
          normalized,
          new ResolvedRecipient(normalized, SmsRecipientSource.MANUAL_NUMBER, null, null));
    }
  }

  private record FileOutcome(int count, String bucketKey) {}

  private FileOutcome addFile(
      File file, String originalFilename, LinkedHashMap<String, ResolvedRecipient> byPhoneNumber) {
    if (file == null) {
      return new FileOutcome(0, null);
    }
    try {
      var parser = new ExcelParser<>(SmsFileImportRowDto.class, SmsFileImportRowDto.getCellMap());
      var parseResult = parser.parseFile(file, 0, CREATE_NULL_AS_BLANK);

      var bucketKey =
          BUCKET_FOLDER
              + "/"
              + MONTH_FOLDER.format(Instant.now())
              + "/"
              + UUID.randomUUID()
              + "_"
              + (originalFilename == null ? file.getName() : originalFilename);
      bucketComponent.upload(file, bucketKey);

      var personalized =
          parseResult.parsedResult().stream().anyMatch(row -> row.getMessage() != null);

      var addedCount = 0;
      for (var row : parseResult.parsedResult()) {
        var isNew =
            byPhoneNumber.putIfAbsent(
                    row.getPhoneNumber(),
                    new ResolvedRecipient(
                        row.getPhoneNumber(),
                        SmsRecipientSource.IMPORTED_FILE,
                        null,
                        personalized ? row.getMessage() : null))
                == null;
        if (isNew) {
          addedCount++;
        }
      }

      var rejectedRows =
          parseResult.skippedRows().entrySet().stream()
              .map(
                  entry ->
                      new SmsFileImportRejectedRow()
                          .row(entry.getKey().getRowNum())
                          .value(readRawFirstCell(entry.getKey()))
                          .reason(entry.getValue().getMessage()))
              .toList();

      if (!rejectedRows.isEmpty()) {
        throw new SmsFileRowsRejectedException(rejectedRows);
      }

      return new FileOutcome(addedCount, bucketKey);
    } catch (IOException e) {
      throw new BadRequestException("Fichier illisible : " + e.getMessage());
    }
  }

  private String readRawFirstCell(org.apache.poi.ss.usermodel.Row row) {
    try {
      var cell = row.getCell(0, CREATE_NULL_AS_BLANK);
      return switch (cell.getCellType()) {
        case STRING -> cell.getStringCellValue();
        case NUMERIC -> String.valueOf(cell.getNumericCellValue());
        default -> "";
      };
    } catch (Exception e) {
      return "";
    }
  }
}
