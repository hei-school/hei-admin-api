package school.hei.haapi.integration;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CorStatus.IN_PROGRESS;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCor;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCorComment;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCorCommentInfo;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.List;
import org.casbin.casdoor.entity.CasdoorRole;
import org.casbin.casdoor.entity.CasdoorUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.CorApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.CorCommentMapper;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CorRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.CorService;

class CorIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private CorRepository corRepository;
  @Autowired private CorService corService;
  @Autowired private CorMapper corMapper;
  @Autowired private CorCommentMapper corCommentMapper;
  private final Faker faker = new Faker();

  private User axelWithCor;
  private User tolotraWithoutCor;
  private Cor corAxel;
  private final String axelToken = "AXEL_TOKEN";

  @BeforeEach
  void setUp() {
    axelWithCor = userRepository.save(someStudent("axel"));
    tolotraWithoutCor = userRepository.save(someStudent("tolotra"));
    corAxel = corRepository.save(someCor(axelWithCor, Instant.parse("2025-01-01T10:00:00Z")));
    someCorComment(faker.number().numberBetween(0, 2))
        .forEach(c -> corService.addComment(corAxel.getId(), c));
    corAxel = corRepository.findById(corAxel.getId()).get();

    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    when(cognitoComponentMock.getEmailByIdToken(axelToken)).thenReturn(axelWithCor.getEmail());
    when(casdoorAuthServiceMock.parseJwtToken(axelToken)).thenReturn(getCasdoorAxel());
  }

  @Test
  void student_get_own_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(axelToken));

    var studentCors = api.getStudentCors(axelWithCor.getId(), null, null);

    assertEquals(1, studentCors.size());
    var studentCor = studentCors.getFirst();
    assertEquals(corMapper.toRest(corAxel), studentCor);
  }

  @Test
  void student_get_other_cor_ko() {
    var api = new CorApi(anApiClient(STUDENT1_TOKEN));

    assertThrowsForbiddenException(() -> api.getStudentCors(axelWithCor.getId(), null, null));
  }

  @Test
  void student_create_cor_ko() {
    var api = new CorApi(anApiClient(axelToken));
    var cor =
        new CrupdateCor()
            .concernedStudentId(tolotraWithoutCor.getId())
            .description("tolotra don't practice enough")
            .interviewDate(Instant.now());

    assertThrowsForbiddenException(() -> api.crupdateStudentCors(tolotraWithoutCor.getId(), cor));
  }

  @Test
  void manager_create_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));
    var cor =
        new CrupdateCor()
            .concernedStudentId(tolotraWithoutCor.getId())
            .description("tolotra don't practice enough")
            .interviewDate(Instant.now());

    var createdCor = api.crupdateStudentCors(tolotraWithoutCor.getId(), cor);

    assertEquals(
        corMapper.toRest(corMapper.toDomain(cor, tolotraWithoutCor.getId())),
        createdCor.id(null).creationDatetime(null));
  }

  @Test
  void manager_filter_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));

    var cors = api.getCors(null, null, null, null, null, null, null);
    assertTrue(cors.contains(corMapper.toRest(corAxel)));

    var corsFilterByStudentRef =
        api.getCors(null, null, null, null, axelWithCor.getRef(), null, null);
    assertTrue(corsFilterByStudentRef.contains(corMapper.toRest(corAxel)));
    assertFalse(
        corsFilterByStudentRef.contains(
            corMapper.toRest(Cor.builder().student(tolotraWithoutCor).build())));
    corAxel = corService.addComment(corAxel.getId(), someCorComment(CorStatus.IN_PROGRESS));
    var corsFilterByStatus = api.getCors(1, 1, null, null, null, null, singletonList(IN_PROGRESS));
    assertEquals(1, corsFilterByStatus.size());
    assertEquals(IN_PROGRESS, corsFilterByStatus.getFirst().getStatus());
  }

  @Test
  void manager_comment_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));
    var corId = corAxel.getId();
    var newCorComment = someCorCommentInfo();
    var initialCommentCount = corAxel.getComments().size();

    api.commentCorById(corId, newCorComment);

    var findCor = corRepository.findById(corId);
    assertTrue(findCor.isPresent());
    var cor = findCor.get();
    assertEquals(corCommentMapper.toDomain(newCorComment.getStatus()), cor.getStatus());
    assertEquals(initialCommentCount + 1, cor.getComments().size());
    var lastCorComment = cor.getLastComment();
    assertTrue(lastCorComment.isPresent());
    assertEquals(newCorComment.getComment(), lastCorComment.get().getComment());
    assertEquals(
        corCommentMapper.toDomain(newCorComment.getStatus()), lastCorComment.get().getStatus());
  }

  @Test
  void student_comment_cor_ko() {
    var api = new CorApi(anApiClient(STUDENT1_TOKEN));
    var corId = corAxel.getId();

    assertThrowsForbiddenException(() -> api.commentCorById(corId, someCorCommentInfo()));
  }

  @Test
  void manager_get_cor_by_id_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));

    var cor = api.getCorById(corAxel.getId());

    assertEquals(corMapper.toRest(corAxel), cor);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  private CasdoorUser getCasdoorAxel() {
    var user = new CasdoorUser();
    user.setEmail(axelWithCor.getEmail());

    var casdoorRole = new CasdoorRole();
    casdoorRole.setOwner("dummy");
    casdoorRole.setName("student");
    var roleUsers = new String[] {"dummy/user"};
    casdoorRole.setUsers(roleUsers);
    user.setRoles(List.of(casdoorRole));

    return user;
  }
}
