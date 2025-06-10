package school.hei.haapi.service.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.COMMON_CORE;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.EL;
import static school.hei.haapi.endpoint.rest.model.SpecializationField.TN;

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
import school.hei.haapi.service.PromotionService;

class ScholarshipCertificateDataProviderTest extends FacadeITMockedThirdParties {
  @MockBean private SchoolYearGetter schoolYearGetter;
  @MockBean private PromotionService promotionService;
  private ScholarshipCertificateDataProvider subject;

  @BeforeEach
  void setUp() {
    subject = new ScholarshipCertificateDataProvider(schoolYearGetter, promotionService);
  }

  @Test
  void correct_academic_year_sentence() {
    int actualYear = LocalDate.now().getYear();
    var firstYear = instantOf(actualYear - 1, 11, 1);
    var secondYear = instantOf(actualYear - 2, 11, 1);
    var thirdYear = instantOf(actualYear - 3, 11, 1);
    var fourthYear = instantOf(actualYear - 4, 11, 1);
    var fifthYear = instantOf(actualYear - 5, 11, 1);
    var invalidYear = instantOf(actualYear - 6, 11, 1);
    User studentFirstYear = randomUser(firstYear, COMMON_CORE);
    User studentSecondYear = randomUser(secondYear, TN);
    User studentThirdYear = randomUser(thirdYear, COMMON_CORE);
    User studentFourthYear = randomUser(fourthYear, COMMON_CORE);
    User studentFifthYear = randomUser(fifthYear, EL);
    User invalidStudentYear = randomUser(invalidYear, COMMON_CORE);
    when(promotionService.getAllStudentPromotions(studentFirstYear.getId()))
        .thenReturn(linkedHashSetOf(promotionWithStartDateTime(firstYear)));
    when(promotionService.getAllStudentPromotions(studentSecondYear.getId()))
        .thenReturn(linkedHashSetOf(promotionWithStartDateTime(secondYear)));
    when(promotionService.getAllStudentPromotions(studentThirdYear.getId()))
        .thenReturn(linkedHashSetOf(promotionWithStartDateTime(thirdYear)));
    when(promotionService.getAllStudentPromotions(studentFourthYear.getId()))
        .thenReturn(linkedHashSetOf(promotionWithStartDateTime(fourthYear)));
    when(promotionService.getAllStudentPromotions(studentFifthYear.getId()))
        .thenReturn(linkedHashSetOf(promotionWithStartDateTime(fifthYear)));
    when(promotionService.getAllStudentPromotions(invalidStudentYear.getId()))
        .thenReturn(linkedHashSetOf(promotionWithStartDateTime(invalidYear)));

    assertEquals(
        "Première année d'informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(studentFirstYear));
    assertEquals(
        "Deuxième année d'informatique - parcours Transformation Numérique",
        subject.getAcademicYearSentence(studentSecondYear));
    assertEquals(
        "Troisième année d'informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(studentThirdYear));
    assertEquals(
        "Quatrième année d'informatique - parcours Tronc commun",
        subject.getAcademicYearSentence(studentFourthYear));
    assertEquals(
        "Cinquième année d'informatique - parcours Écosystème Logiciel",
        subject.getAcademicYearSentence(studentFifthYear));
    assertEquals(
        "Non defini année d'informatique - parcours Tronc commun",
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

  private static Promotion promotionWithStartDateTime(Instant firstYear) {
    return new Promotion("", Instant.now(), "", "", firstYear, List.of());
  }

  private static Instant instantOf(int year, int month, int day) {
    return LocalDate.of(year, month, day).atStartOfDay().toInstant(ZoneOffset.ofHours(3));
  }
}
