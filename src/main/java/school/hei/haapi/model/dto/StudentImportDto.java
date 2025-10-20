package school.hei.haapi.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.util.StringUtil;
import school.hei.haapi.endpoint.rest.model.CrupdateStudent;
import school.hei.haapi.endpoint.rest.model.PaymentFrequency;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.service.utils.excel.CellMap;

public record StudentImportDto(
    String ref,
    String firstName,
    String lastName,
    String email,
    Sex sex,
    LocalDate birthDate,
    String address,
    String phone,
    Instant entranceDatetime,
    PaymentFrequency paymentFrequency) {
  private static final String REF_REGEX = "^STD[\\w-]+$";
  private static final String EMAIL_REGEX = "^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$";

  private static String getRefFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return validateRefCell(cell);
  }

  private static String getFirstNameFromCell(Cell cell) {
    var cellValue = cell.getStringCellValue();
    if (StringUtil.isNotBlank(cellValue)) {
      return cellValue.trim();
    } else {
      return null;
    }
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
    var value = cell.getStringCellValue();
    if (StringUtil.isNotBlank(value)) {
      return LocalDate.parse(value.trim());
    }
    return null;
  }

  private static String getAddressFromCell(Cell cell) {
    var cellValue = cell.getStringCellValue();
    if (StringUtil.isNotBlank(cellValue)) {
      return cellValue.trim();
    } else {
      return null;
    }
  }

  private static String getPhoneFromCell(Cell cell) {
    var cellValue = cell.getStringCellValue();
    if (StringUtil.isNotBlank(cellValue)) {
      return cellValue.trim();
    } else {
      return null;
    }
  }

  private static Instant getEntranceDatetimeFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return Instant.parse(cell.getStringCellValue().trim());
  }

  private static PaymentFrequency getPaymentFrequencyFromCell(Cell cell) {
    verifyEmptyCell(cell);
    return PaymentFrequency.valueOf(cell.getStringCellValue().trim());
  }

  public CrupdateStudent toCrupdateStudent() {
    return new CrupdateStudent()
        .ref(ref)
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .sex(sex)
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
      if (!email.matches(EMAIL_REGEX)) {
        throw new IllegalArgumentException(
            "Ligne %d ignorée en raison d'un format d'email invalide"
                .formatted(cell.getRowIndex()));
      } else {
        return email;
      }
    } else {
      throw new IllegalArgumentException(
          "Ligne %d ignorée en raison d'un email vide".formatted(cell.getRowIndex()));
    }
  }

  public static Map<String, CellMap<?>> getCellMap() {
    return Map.of(
        "ref", new CellMap<String>(1, StudentImportDto::getRefFromCell),
        "firstName", new CellMap<String>(2, StudentImportDto::getFirstNameFromCell),
        "lastName", new CellMap<String>(3, StudentImportDto::getLastNameFromCell),
        "email", new CellMap<String>(4, StudentImportDto::getEmailFromCell),
        "sex", new CellMap<Sex>(5, StudentImportDto::getSexFromCell),
        "birthDate", new CellMap<LocalDate>(6, StudentImportDto::getBirthDateFromCell),
        "address", new CellMap<String>(7, StudentImportDto::getAddressFromCell),
        "phone", new CellMap<String>(8, StudentImportDto::getPhoneFromCell),
        "entranceDatetime", new CellMap<Instant>(9, StudentImportDto::getEntranceDatetimeFromCell),
        "paymentFrequency",
            new CellMap<PaymentFrequency>(10, StudentImportDto::getPaymentFrequencyFromCell));
  }
}
