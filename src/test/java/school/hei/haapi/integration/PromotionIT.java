package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.CycleEnum.BACHELOR;
import static school.hei.haapi.endpoint.rest.model.UpdatePromotionSGroup.TypeEnum.ADD;
import static school.hei.haapi.endpoint.rest.model.UpdatePromotionSGroup.TypeEnum.REMOVE;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.GroupTestData.g2;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.PromotionTestData.aCrupdatePromotion;
import static school.hei.haapi.integration.testData.PromotionTestData.aPromotion;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.rest.api.PromotionsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CrupdatePromotion;
import school.hei.haapi.endpoint.rest.model.Promotion;
import school.hei.haapi.endpoint.rest.model.UpdatePromotionSGroup;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.UserRepository;

public class PromotionIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private PromotionRepository promotionRepository;

  private User studentAxel;
  private User teacherToky;
  private User managerHasina;

  private school.hei.haapi.model.Promotion promotionWithGroup;
  private school.hei.haapi.model.Promotion promotionWithoutGroup;
  private school.hei.haapi.model.Promotion promotionForGroupMoves;
  private Group groupInPromotion;
  private Group movableGroup;

  /** Promotions the tests create through the API, swept in tearDown. */
  private final List<String> createdPromotionIds = new ArrayList<>();

  private String studentToken;
  private String teacherToken;
  private String managerToken;

  private void setUpTestData() {
    studentAxel = userRepository.save(axel());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());

    promotionWithGroup =
        promotionRepository.save(aPromotion("Promotion 2021-2022", "PROM" + randomUUID()));
    promotionWithoutGroup =
        promotionRepository.save(aPromotion("Promotion 2022-2023", "PROM" + randomUUID()));
    promotionForGroupMoves =
        promotionRepository.save(aPromotion("Promotion 2023-2024", "PROM" + randomUUID()));

    groupInPromotion = g1();
    groupInPromotion.setPromotion(promotionWithGroup);
    groupInPromotion = groupRepository.save(groupInPromotion);

    movableGroup = groupRepository.save(g2());
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    studentToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    // groups point at promotions, so they go first
    groupInPromotion.setPromotion(null);
    movableGroup.setPromotion(null);
    groupRepository.saveAll(List.of(groupInPromotion, movableGroup));
    groupRepository.deleteAll(List.of(groupInPromotion, movableGroup));

    promotionRepository.deleteAllById(createdPromotionIds);
    createdPromotionIds.clear();
    promotionRepository.deleteAll(
        List.of(promotionWithGroup, promotionWithoutGroup, promotionForGroupMoves));
    userRepository.deleteAll(List.of(studentAxel, teacherToky, managerHasina));
  }

  private PromotionsApi apiAs(String token) {
    return new PromotionsApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private static List<String> idsOf(List<Promotion> promotions) {
    return promotions.stream().map(Promotion::getId).toList();
  }

  private UpdatePromotionSGroup groupMove(Group group, UpdatePromotionSGroup.TypeEnum type) {
    return new UpdatePromotionSGroup().type(type).groupIds(List.of(group.getId()));
  }

  @Test
  void manager_generate_promotion_students_ok() throws IOException, InterruptedException {
    var response =
        HttpClient.newBuilder()
            .build()
            .send(
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            "http://localhost:"
                                + localPort
                                + "/promotions/"
                                + promotionWithGroup.getId()
                                + "/students"))
                    .GET()
                    .header("Authorization", "Bearer " + managerToken)
                    .build(),
                HttpResponse.BodyHandlers.ofByteArray());

    assertEquals(HttpStatus.OK.value(), response.statusCode());
    assertNotNull(response.body());
  }

  @Test
  void manager_read_promotion_ok() throws ApiException {
    var api = apiAs(managerToken);

    var allPromotions = api.getPromotions(1, 250, null, null, null);
    assertTrue(idsOf(allPromotions).contains(promotionWithGroup.getId()));
    assertTrue(idsOf(allPromotions).contains(promotionWithoutGroup.getId()));

    var byName = api.getPromotions(1, 250, promotionWithGroup.getName(), null, null);
    assertTrue(idsOf(byName).contains(promotionWithGroup.getId()));
    assertFalse(idsOf(byName).contains(promotionWithoutGroup.getId()));

    var byRef = api.getPromotions(1, 250, null, promotionWithoutGroup.getRef(), null);
    assertTrue(idsOf(byRef).contains(promotionWithoutGroup.getId()));
    assertFalse(idsOf(byRef).contains(promotionWithGroup.getId()));

    var byGroupRef = api.getPromotions(1, 250, null, null, groupInPromotion.getRef());
    assertTrue(idsOf(byGroupRef).contains(promotionWithGroup.getId()));
    assertFalse(idsOf(byGroupRef).contains(promotionWithoutGroup.getId()));
  }

  @Test
  void teacher_read_promotion_ok() throws ApiException {
    var allPromotions = apiAs(teacherToken).getPromotions(1, 250, null, null, null);

    assertTrue(idsOf(allPromotions).contains(promotionWithGroup.getId()));
    assertTrue(idsOf(allPromotions).contains(promotionWithoutGroup.getId()));
  }

  @Test
  void student_read_promotion_ok() throws ApiException {
    var allPromotions = apiAs(studentToken).getPromotions(1, 250, null, null, null);

    assertTrue(idsOf(allPromotions).contains(promotionWithGroup.getId()));
    assertTrue(idsOf(allPromotions).contains(promotionWithoutGroup.getId()));
  }

  @Test
  void manager_create_or_update_promotion_ok() throws ApiException {
    var api = apiAs(managerToken);
    var toCreate = aCrupdatePromotion("Promotion 2024-2025", "PROM" + randomUUID());

    var created = api.crupdatePromotion(toCreate);
    createdPromotionIds.add(created.getId());
    assertEquals(toCreate.getRef(), created.getRef());
    assertEquals(toCreate.getName(), created.getName());

    var toUpdate =
        new CrupdatePromotion()
            .id(created.getId())
            .ref(created.getRef())
            .name("Nom de la promotion modifiée")
            .cycleLevel(BACHELOR);

    var updated = api.crupdatePromotion(toUpdate);
    assertEquals(toUpdate.getId(), updated.getId());
    assertEquals(toUpdate.getRef(), updated.getRef());
    assertEquals(toUpdate.getName(), updated.getName());
    assertEquals(created.getCreationDatetime(), updated.getCreationDatetime());
  }

  @Test
  void manager_read_promotion_by_id_ok() throws ApiException {
    var actual = apiAs(managerToken).getPromotionById(promotionWithGroup.getId());

    assertEquals(promotionWithGroup.getId(), actual.getId());
    assertEquals(promotionWithGroup.getRef(), actual.getRef());
    assertEquals(promotionWithGroup.getName(), actual.getName());
  }

  @Test
  void teacher_read_promotion_by_id_ok() throws ApiException {
    var actual = apiAs(teacherToken).getPromotionById(promotionWithGroup.getId());

    assertEquals(promotionWithGroup.getId(), actual.getId());
  }

  @Test
  void student_read_promotion_by_id_ok() throws ApiException {
    var actual = apiAs(studentToken).getPromotionById(promotionWithGroup.getId());

    assertEquals(promotionWithGroup.getId(), actual.getId());
  }

  @Test
  void manager_add_or_remove_groups_ok() throws ApiException {
    var api = apiAs(managerToken);

    var withAddedGroup =
        api.updatePromotionGroups(promotionForGroupMoves.getId(), groupMove(movableGroup, ADD));
    assertEquals(1, withAddedGroup.getGroups().size());
    assertEquals(movableGroup.getId(), withAddedGroup.getGroups().getFirst().getId());

    var withRemovedGroup =
        api.updatePromotionGroups(promotionForGroupMoves.getId(), groupMove(movableGroup, REMOVE));
    assertEquals(0, withRemovedGroup.getGroups().size());
  }

  @Test
  void update_promotion_forbidden_ok() {
    var studentApi = apiAs(studentToken);
    var teacherApi = apiAs(teacherToken);
    var toCreate = aCrupdatePromotion("Promotion X", "PROM" + randomUUID());

    assertThrowsForbiddenException(() -> studentApi.crupdatePromotion(toCreate));
    assertThrowsForbiddenException(
        () ->
            studentApi.updatePromotionGroups(
                promotionForGroupMoves.getId(), groupMove(movableGroup, ADD)));

    assertThrowsForbiddenException(() -> teacherApi.crupdatePromotion(toCreate));
    assertThrowsForbiddenException(
        () ->
            teacherApi.updatePromotionGroups(
                promotionForGroupMoves.getId(), groupMove(movableGroup, ADD)));
  }
}
