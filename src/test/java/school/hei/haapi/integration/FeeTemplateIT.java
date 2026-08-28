package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.HARDWARE;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.FeeTemplateTestData.aFeeTemplate;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.PayingApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdateFeeTemplate;
import school.hei.haapi.endpoint.rest.model.FeeCategory;
import school.hei.haapi.endpoint.rest.model.FeeFrequency;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.FeeTemplate;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.FeeTemplateRepository;
import school.hei.haapi.repository.UserRepository;

public class FeeTemplateIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private FeeTemplateRepository feeTemplateRepository;

  private User studentAxel;
  private User managerHasina;

  private FeeTemplate yearlyTemplate;
  private FeeTemplate hardwareTemplate;

  private String studentToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    managerHasina = userRepository.save(hasina());

    yearlyTemplate =
        feeTemplateRepository.save(aFeeTemplate("annuel x9 " + randomUUID(), 200_000, 9, TUITION));
    hardwareTemplate =
        feeTemplateRepository.save(aFeeTemplate("Keyboard " + randomUUID(), 1000, 1, HARDWARE));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    feeTemplateRepository.deleteAll(List.of(yearlyTemplate, hardwareTemplate));
    userRepository.deleteAll(List.of(studentAxel, managerHasina));
  }

  private PayingApi apiAs(String token) {
    return new PayingApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static List<String> idsOf(
      List<school.hei.haapi.endpoint.rest.model.FeeTemplate> templates) {
    return templates.stream().map(template -> template.getId()).toList();
  }

  @Test
  void manager_get_fee_templates() throws ApiException {
    var actual = apiAs(managerToken).getFeeTemplates(null, null, null, 1, 100);

    assertTrue(idsOf(actual).contains(yearlyTemplate.getId()));
    assertTrue(idsOf(actual).contains(hardwareTemplate.getId()));
  }

  @Test
  void student_get_fee_templates() throws ApiException {
    var actual = apiAs(studentToken).getFeeTemplates(null, null, null, 1, 100);

    assertTrue(idsOf(actual).contains(yearlyTemplate.getId()));
    assertTrue(idsOf(actual).contains(hardwareTemplate.getId()));
  }

  @Test
  void manager_create_fee_template() throws ApiException {
    var newId = randomUUID().toString();
    var toCreate =
        new CrupdateFeeTemplate()
            .id(newId)
            .name("Frais mensuel " + randomUUID())
            .amount(200_000)
            .numberOfPayments(9)
            .type(TUITION)
            .category(FeeCategory.UNKNOWN)
            .frequency(FeeFrequency.UNKNOWN);

    var created = apiAs(managerToken).crupdateFeeTemplate(newId, toCreate);

    // a creation mints its own id, the supplied one only matters for an update
    assertNotNull(created.getId());
    assertEquals(toCreate.getName(), created.getName());
    assertEquals(toCreate.getAmount(), created.getAmount());
    assertEquals(toCreate.getNumberOfPayments(), created.getNumberOfPayments());

    feeTemplateRepository.deleteById(created.getId());
  }

  @Test
  void manager_update_fee_template() throws ApiException {
    var toUpdate =
        new CrupdateFeeTemplate()
            .id(yearlyTemplate.getId())
            .name(yearlyTemplate.getName())
            .amount(1000)
            .numberOfPayments(1)
            .type(TUITION)
            .category(FeeCategory.UNKNOWN)
            .frequency(FeeFrequency.UNKNOWN);

    var actual = apiAs(managerToken).crupdateFeeTemplate(yearlyTemplate.getId(), toUpdate);

    assertEquals(yearlyTemplate.getId(), actual.getId());
    assertEquals(yearlyTemplate.getName(), actual.getName());
    assertEquals(1000, actual.getAmount());
    assertEquals(1, actual.getNumberOfPayments());
  }

  @Test
  void get_fee_template_by_id_existing() throws ApiException {
    var actual = apiAs(managerToken).getFeeTemplateById(yearlyTemplate.getId());

    assertEquals(yearlyTemplate.getId(), actual.getId());
    assertEquals(yearlyTemplate.getName(), actual.getName());
    assertEquals(yearlyTemplate.getAmount(), actual.getAmount());
  }

  @Test
  void student_get_fee_template_by_id_existing() throws ApiException {
    var actual = apiAs(studentToken).getFeeTemplateById(yearlyTemplate.getId());

    assertEquals(yearlyTemplate.getId(), actual.getId());
    assertEquals(yearlyTemplate.getName(), actual.getName());
  }
}
