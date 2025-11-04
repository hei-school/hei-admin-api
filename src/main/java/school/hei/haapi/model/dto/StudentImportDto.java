package school.hei.haapi.model.dto;

import static java.util.Map.entry;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.PaymentFrequency;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.service.utils.excel.CellMap;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class StudentImportDto {
  private String ref;
  private String firstName;
  private String lastName;
  private String email;
  private Sex sex;
  private String nic;
  private LocalDate birthDate;
  private String birthPlace;
  private String address;
  private String phone;
  private Instant entranceDatetime;
  private PaymentFrequency paymentFrequency;

  private static final String REF_REGEX = "^STD[\\w-]+$";

  private static String getRefFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return validateRefCell(cell);
  }

  private static String getFirstNameFromCell(Cell cell) {
    return getNullableStringFromCell(cell);
  }

  private static String getLastNameFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return cell.getStringCellValue().trim();
  }

  private static String getEmailFromCell(Cell cell) {
    return validateEmailCell(cell);
  }

  private static Sex getSexFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return Sex.valueOf(cell.getStringCellValue().trim());
  }

  private static LocalDate getBirthDateFromCell(Cell cell) {
    try {
      var value = cell.getLocalDateTimeCellValue();
      if (value != null) {
        return value.toLocalDate();
      }
      return null;
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
          "ligne %d ignorée en raison d'une valeur incovertible en date"
              .formatted(cell.getRowIndex()));
    }
  }

  private static String getAddressFromCell(Cell cell) {
    return getNullableStringFromCell(cell);
  }

  @Nullable
  private static String getNullableStringFromCell(Cell cell) {
    var cellValue = cell.getStringCellValue();
    if (StringUtil.isNotBlank(cellValue)) {
      return cellValue.trim();
    } else {
      return null;
    }
  }

  private static String getNicFromCell(Cell cell) {
    return getNullableStringFromCell(cell);
  }

  private static String getBirthPlaceFromCell(Cell cell) {
    return getNullableStringFromCell(cell);
  }

  private static String getPhoneFromCell(Cell cell) {
    return getNullableStringFromCell(cell);
  }

  private static Instant getEntranceDatetimeFromCell(Cell cell) {
    try {
      var value = cell.getLocalDateTimeCellValue();
      if (value != null) {
        return value.toInstant(ZoneOffset.of("+3"));
      }
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un champ obligatoire vide".formatted(cell.getRowIndex()));
    } catch (NumberFormatException e) {
      throw new IllegalStateException(
          "Ligne %d ignorée en raison d'une valeur incovertible en date"
              .formatted(cell.getRowIndex()));
    }
  }

  private static PaymentFrequency getPaymentFrequencyFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return PaymentFrequency.valueOf(cell.getStringCellValue().trim());
  }

  public CrupdateStudent toCrupdateStudent() {
    return new CrupdateStudent()
        .ref(ref)
        .status(ENABLED)
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .sex(sex)
        .nic(nic)
        .birthPlace(birthPlace)
        .birthDate(birthDate)
        .address(address)
        .phone(phone)
        .entranceDatetime(entranceDatetime)
        .paymentFrequency(paymentFrequency);
  }

  private static void verifyEmptyCell(Cell cell) {
    if (StringUtil.isBlank(cell.getStringCellValue())) {
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un champ obligatoire vide".formatted(cell.getRowIndex()));
    }
  }

  private static String validateRefCell(Cell cell) {
    var ref = cell.getStringCellValue().trim();
    if (!ref.matches(REF_REGEX)) {
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un format de référence étudiant invalide"
              .formatted(cell.getRowIndex()));
    }
    return ref;
  }

  private static String validateEmailCell(Cell cell) {
    var email = cell.getStringCellValue().trim();
    if (StringUtil.isNotBlank(email)) {
      try {
        new InternetAddress(email);
        return email;
      } catch (AddressException e) {
        throw new IllegalArgumentException(
            "Ligne %d ignorée en raison d'un format d'email invalide"
                .formatted(cell.getRowIndex()));
      }
    } else {
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un email vide".formatted(cell.getRowIndex()));
    }
  }

  public static Map<String, CellMap<?>> getCellMap() {
    return Map.ofEntries(
        entry("ref", new CellMap<String>(0, StudentImportDto::getRefFromCell)),
        entry("firstName", new CellMap<String>(1, StudentImportDto::getFirstNameFromCell)),
        entry("lastName", new CellMap<String>(2, StudentImportDto::getLastNameFromCell)),
        entry("email", new CellMap<String>(3, StudentImportDto::getEmailFromCell)),
        entry("sex", new CellMap<Sex>(4, StudentImportDto::getSexFromCell)),
        entry("nic", new CellMap<String>(5, StudentImportDto::getNicFromCell)),
        entry("birthDate", new CellMap<LocalDate>(6, StudentImportDto::getBirthDateFromCell)),
        entry("birthPlace", new CellMap<String>(7, StudentImportDto::getBirthPlaceFromCell)),
        entry("address", new CellMap<String>(8, StudentImportDto::getAddressFromCell)),
        entry("phone", new CellMap<String>(9, StudentImportDto::getPhoneFromCell)),
        entry(
            "entranceDatetime",
            new CellMap<Instant>(10, StudentImportDto::getEntranceDatetimeFromCell)),
        entry(
            "paymentFrequency",
            new CellMap<PaymentFrequency>(11, StudentImportDto::getPaymentFrequencyFromCell)));
  }
}
