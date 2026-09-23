package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.SmsRecipientSource;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.SmsContactGroupRepository;
import school.hei.haapi.repository.SmsContactRepository;
import school.hei.haapi.service.sms.SmsRecipientResolver;

class SmsRecipientResolverTest {
  private final SmsContactRepository smsContactRepositoryMock = mock();
  private final SmsContactGroupRepository smsContactGroupRepositoryMock = mock();
  private final BucketComponent bucketComponentMock = mock();

  private final SmsRecipientResolver subject =
      new SmsRecipientResolver(
          smsContactRepositoryMock, smsContactGroupRepositoryMock, bucketComponentMock);

  @TempDir Path tempDir;

  @Test
  void no_source_at_all_is_rejected() {
    assertThrows(BadRequestException.class, () -> subject.resolve(null, null, null, null, null));
  }

  @Test
  void resolves_recipients_from_a_contact_group() {
    var member1 = SmsContact.builder().id("c1").phoneNumber("321111111").build();
    var member2 = SmsContact.builder().id("c2").phoneNumber("321111112").build();
    var group =
        SmsContactGroup.builder()
            .id("g1")
            .name("Promo 2026")
            .members(List.of(member1, member2))
            .build();
    when(smsContactGroupRepositoryMock.findAllByIdInAndIsDeletedFalse(List.of("g1")))
        .thenReturn(List.of(group));

    var resolved = subject.resolve(List.of("g1"), null, null, null, null);

    assertEquals(2, resolved.recipients().size());
    assertEquals(List.of(group), resolved.contactGroups());
    assertTrue(
        resolved.recipients().stream()
            .allMatch(r -> r.source() == SmsRecipientSource.CONTACT_GROUP));
  }

  @Test
  void resolves_recipients_from_manually_selected_contacts() {
    var contact = SmsContact.builder().id("c1").phoneNumber("321111111").build();
    when(smsContactRepositoryMock.findAllByIdInAndIsDeletedFalse(List.of("c1")))
        .thenReturn(List.of(contact));

    var resolved = subject.resolve(null, List.of("c1"), null, null, null);

    assertEquals(1, resolved.recipients().size());
    assertEquals(SmsRecipientSource.MANUAL_SELECTION, resolved.recipients().get(0).source());
    assertEquals(List.of(contact), resolved.manuallySelectedContacts());
  }

  @Test
  void normalizes_and_dedupes_manual_phone_numbers() {
    var resolved =
        subject.resolve(null, null, List.of("0321111111", "321111111", "12"), null, null);

    // "0321111111" and "321111111" normalize to the same number -> deduplicated to one; "12" is
    // too short to be a real number and is dropped.
    assertEquals(1, resolved.recipients().size());
    assertEquals("321111111", resolved.recipients().get(0).phoneNumber());
    assertEquals(SmsRecipientSource.MANUAL_NUMBER, resolved.recipients().get(0).source());
  }

  @Test
  void a_contact_group_member_wins_over_the_same_number_typed_manually() {
    var member = SmsContact.builder().id("c1").phoneNumber("321111111").build();
    var group = SmsContactGroup.builder().id("g1").members(List.of(member)).build();
    when(smsContactGroupRepositoryMock.findAllByIdInAndIsDeletedFalse(List.of("g1")))
        .thenReturn(List.of(group));

    var resolved = subject.resolve(List.of("g1"), null, List.of("321111111"), null, null);

    assertEquals(1, resolved.recipients().size());
    assertEquals(SmsRecipientSource.CONTACT_GROUP, resolved.recipients().get(0).source());
  }

  @Test
  void plain_file_with_only_phone_numbers_is_not_personalized() throws IOException {
    var file = xlsxWith(List.of("321111111", "321111112"), null);

    var resolved = subject.resolve(null, null, null, file, "numbers.xlsx");

    assertEquals(2, resolved.fileImportCount());
    assertTrue(resolved.recipients().stream().allMatch(r -> r.personalizedMessage() == null));
    assertTrue(
        resolved.recipients().stream()
            .allMatch(r -> r.source() == SmsRecipientSource.IMPORTED_FILE));
  }

  @Test
  void file_with_a_message_column_is_personalized() throws IOException {
    var file = xlsxWith(List.of("321111111", "321111112"), List.of("Bonjour A", "Bonjour B"));

    var resolved = subject.resolve(null, null, null, file, "personalized.xlsx");

    assertEquals(2, resolved.fileImportCount());
    assertEquals(
        "Bonjour A",
        resolved.recipients().stream()
            .filter(r -> r.phoneNumber().equals("321111111"))
            .findFirst()
            .orElseThrow()
            .personalizedMessage());
  }

  @Test
  void invalid_file_rows_are_reported_without_blocking_the_valid_ones() throws IOException {
    var file = xlsxWithRawFirstColumn(List.of("321111111", "not-a-number"));

    var resolved = subject.resolve(null, null, null, file, "mixed.xlsx");

    assertEquals(1, resolved.recipients().size());
    assertEquals(1, resolved.rejectedRows().size());
  }

  @Test
  void uploads_the_file_to_the_bucket() throws IOException {
    var file = xlsxWith(List.of("321111111"), null);

    subject.resolve(null, null, null, file, "numbers.xlsx");

    verify(bucketComponentMock).upload(any(), any());
  }

  @Test
  void falls_back_to_the_file_name_when_no_original_filename_is_given() throws IOException {
    var file = xlsxWith(List.of("321111111"), null);

    var resolved = subject.resolve(null, null, null, file, null);

    assertEquals(1, resolved.fileImportCount());
    assertTrue(resolved.fileBucketKey().endsWith("_" + file.getName()));
  }

  @Test
  void empty_group_and_contact_id_lists_are_treated_like_no_source() {
    var resolved = subject.resolve(List.of(), List.of(), List.of("321111111"), null, null);

    assertEquals(1, resolved.recipients().size());
    assertEquals(List.of(), resolved.contactGroups());
    assertEquals(List.of(), resolved.manuallySelectedContacts());
  }

  private File xlsxWith(List<String> phoneNumbers, List<String> messages) throws IOException {
    try (var workbook = new XSSFWorkbook()) {
      var sheet = workbook.createSheet();
      for (var i = 0; i < phoneNumbers.size(); i++) {
        var row = sheet.createRow(i);
        row.createCell(0).setCellValue(phoneNumbers.get(i));
        if (messages != null) {
          row.createCell(1).setCellValue(messages.get(i));
        }
      }
      var file = tempDir.resolve("test-" + System.nanoTime() + ".xlsx").toFile();
      try (var out = new FileOutputStream(file)) {
        workbook.write(out);
      }
      return file;
    }
  }

  private File xlsxWithRawFirstColumn(List<String> rawValues) throws IOException {
    try (var workbook = new XSSFWorkbook()) {
      var sheet = workbook.createSheet();
      for (var i = 0; i < rawValues.size(); i++) {
        sheet.createRow(i).createCell(0).setCellValue(rawValues.get(i));
      }
      var file = tempDir.resolve("mixed-" + System.nanoTime() + ".xlsx").toFile();
      try (var out = new FileOutputStream(file)) {
        workbook.write(out);
      }
      return file;
    }
  }
}
