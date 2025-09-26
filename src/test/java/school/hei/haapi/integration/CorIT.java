package school.hei.haapi.integration;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CorStatus.IN_PROGRESS;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCor;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCorCommentInfo;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;

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
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CorRepository;
import school.hei.haapi.repository.UserRepository;

public class CorIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private CorRepository corRepository;
  @Autowired private CorMapper corMapper;

  private User axelWithCor;
  private User tolotraWithoutCor;
  private Cor corAxel;
  private final String AXEL_TOKEN = "AXEL_TOKEN";

  @BeforeEach
  void setUp() {
    axelWithCor = userRepository.save(someStudent("axel"));
    tolotraWithoutCor = userRepository.save(someStudent("tolotra"));
    corAxel = corRepository.save(someCor(axelWithCor, Instant.parse("2025-01-01T10:00:00Z")));

    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    when(cognitoComponentMock.getEmailByIdToken(AXEL_TOKEN)).thenReturn(axelWithCor.getEmail());
    when(casdoorAuthServiceMock.parseJwtToken(AXEL_TOKEN)).thenReturn(getCasdoorAxel());
  }

  @Test
  public void student_get_own_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(AXEL_TOKEN));

    var studentCors = api.getStudentCors(axelWithCor.getId(), null, null);

    assertEquals(1, studentCors.size());
    var studentCor = studentCors.getFirst();
    assertEquals(corMapper.toRest(corAxel), studentCor);
  }

  @Test
  public void student_get_other_cor_ko() {
    var api = new CorApi(anApiClient(STUDENT1_TOKEN));

    assertThrowsForbiddenException(() -> api.getStudentCors(axelWithCor.getId(), null, null));
  }

  @Test
  public void student_create_cor_ko() {
    var api = new CorApi(anApiClient(AXEL_TOKEN));
    var cor =
        new CrupdateCor()
            .concernedStudentId(tolotraWithoutCor.getId())
            .description("tolotra don't practice enough")
            .interviewDate(Instant.now());

    assertThrowsForbiddenException(() -> api.crupdateStudentCors(tolotraWithoutCor.getId(), cor));
  }

  @Test
  public void manager_create_cor_ok() throws ApiException {
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
  public void manager_filter_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));

    var cors = api.getCors(null, null, null, null, null, null, null);
    System.out.println(cors);
    assertTrue(cors.contains(corMapper.toRest(corAxel)));

    var corsFilterByStudentRef =
        api.getCors(null, null, null, null, axelWithCor.getRef(), null, null);
    assertTrue(corsFilterByStudentRef.contains(corMapper.toRest(corAxel)));
    assertFalse(
        corsFilterByStudentRef.contains(
            corMapper.toRest(Cor.builder().concernedStudent(tolotraWithoutCor).build())));

    var corsFilterByStatus = api.getCors(1, 10, null, null, null, null, singletonList(IN_PROGRESS));
    assertTrue(corsFilterByStatus.stream().allMatch(cor -> IN_PROGRESS.equals(cor.getStatus())));
  }

  @Test
  public void manager_comment_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));
    var corId = corAxel.getId();
    var newCorComment = someCorCommentInfo();

    api.commentCorById(corId, newCorComment);

    var findCor = corRepository.findById(corId);
    assertTrue(findCor.isPresent());
    var cor = findCor.get();
    assertEquals(corMapper.toDomain(newCorComment.getStatus()), cor.getStatus());
    assertEquals(1, cor.getComments().size());
    var corComment = cor.getComments().getFirst();
    assertEquals(newCorComment.getComment(), corComment.getComment());
    assertEquals(corMapper.toDomain(newCorComment.getStatus()), corComment.getStatus());
  }

  @Test
  public void student_comment_cor_ko() {
    var api = new CorApi(anApiClient(STUDENT1_TOKEN));
    var corId = corAxel.getId();

    assertThrowsForbiddenException(() -> api.commentCorById(corId, someCorCommentInfo()));
  }

  @Test
  public void manager_get_cor_by_id_ok() throws ApiException {
    var api = new CorApi(anApiClient(MANAGER1_TOKEN));

    var cor = api.getCorById(corAxel.getId());

    assertEquals(corMapper.toRest(corAxel), cor);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  public CasdoorUser getCasdoorAxel() {
    CasdoorUser user = new CasdoorUser();
    user.setEmail(axelWithCor.getEmail());

    CasdoorRole casdoorRole = new CasdoorRole();
    casdoorRole.setOwner("dummy");
    casdoorRole.setName("student");
    String[] roleUsers = List.of("dummy/user").toArray(new String[0]);
    casdoorRole.setUsers(roleUsers);
    user.setRoles(List.of(casdoorRole));

    return user;
  }
}
