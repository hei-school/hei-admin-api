package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.EL;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.TN;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.CycleLevel.MASTER;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.model.SpecializationField;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.service.utils.ScholarshipCertificateDataProvider;
import school.hei.haapi.service.utils.SchoolYearSupplier;

class ScholarshipCertificateDataProviderTest extends FacadeITMockedThirdParties {
  @MockBean private SchoolYearSupplier schoolYearSupplier;
  @MockBean private PromotionService promotionService;
  private ScholarshipCertificateDataProvider subject;

  @BeforeEach
  void setUp() {
    subject = new ScholarshipCertificateDataProvider(schoolYearSupplier, promotionService);
  }

  @Test
  void get_academic_year_ok() {
    int actualYear = 2025;
    var licenseFirstYear = instantOf(actualYear, 11, 1);
    var licenseSecondYear = instantOf(actualYear - 1, 11, 1);
    var licenseThirdYear = instantOf(actualYear - 2, 11, 1);
    var masterFirstYear = instantOf(actualYear, 11, 1);
    var masterSecondYear = instantOf(actualYear - 1, 11, 1);
    var invalidYear = instantOf(actualYear - 5, 11, 1);
    User studentFirstYear = randomUser(licenseFirstYear, COMMON_CORE);
    User studentSecondYear = randomUser(licenseSecondYear, TN);
    User studentThirdYear = randomUser(licenseThirdYear, COMMON_CORE);
    User studentFourthYear = randomUser(masterFirstYear, COMMON_CORE);
    User studentFifthYear = randomUser(masterSecondYear, EL);
    User invalidStudentYear = randomUser(invalidYear, COMMON_CORE);
    when(promotionService.getAllStudentPromotions(studentFirstYear.getId()))
        .thenReturn(linkedHashSetOf(licensePromotionWithStartDateTime(licenseFirstYear)));
    when(promotionService.getAllStudentPromotions(studentSecondYear.getId()))
        .thenReturn(linkedHashSetOf(licensePromotionWithStartDateTime(licenseSecondYear)));
    when(promotionService.getAllStudentPromotions(studentThirdYear.getId()))
        .thenReturn(linkedHashSetOf(licensePromotionWithStartDateTime(licenseThirdYear)));
    when(promotionService.getAllStudentPromotions(studentFourthYear.getId()))
        .thenReturn(linkedHashSetOf(masterPromotionWithStartDateTime(masterFirstYear)));
    when(promotionService.getAllStudentPromotions(studentFifthYear.getId()))
        .thenReturn(linkedHashSetOf(masterPromotionWithStartDateTime(masterSecondYear)));
    when(promotionService.getAllStudentPromotions(invalidStudentYear.getId()))
        .thenReturn(linkedHashSetOf(licensePromotionWithStartDateTime(invalidYear)));

    assertEquals(
        "Première année de Licence en Informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(studentFirstYear));
    assertEquals(
        "Deuxième année de Licence en Informatique - parcours Transformation Numérique",
        subject.getAcademicYearSentence(studentSecondYear));
    assertEquals(
        "Troisième année de Licence en Informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(studentThirdYear));
    assertEquals(
        "Première année de Master en Informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(studentFourthYear));
    assertEquals(
        "Deuxième année de Master en Informatique - parcours Écosystème Logiciel",
        subject.getAcademicYearSentence(studentFifthYear));
    assertEquals(
        "Non défini en Informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(invalidStudentYear));
  }

  private static LinkedHashSet<Promotion> linkedHashSetOf(Promotion promotion) {
    return new LinkedHashSet<>(List.of(promotion));
  }

  private static User randomUser(Instant firstYear, SpecializationField specializationField) {
    return User.builder()
        .id(UUID.randomUUID().toString())
        .specializationField(specializationField)
        .entranceDatetime(firstYear)
        .build();
  }

  private static Promotion licensePromotionWithStartDateTime(Instant firstYear) {
    return new Promotion("", Instant.now(), "", "", firstYear, BACHELOR, List.of());
  }

  private static Promotion masterPromotionWithStartDateTime(Instant firstYear) {
    return new Promotion("", Instant.now(), "", "", firstYear, MASTER, List.of());
  }

  private static Instant instantOf(int year, int month, int day) {
    return LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.ofHours(3));
  }
}
