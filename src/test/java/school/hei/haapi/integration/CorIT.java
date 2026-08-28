package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.CorStatus.LEAVE;
import static school.hei.haapi.integration.conf.ApiAssertions.assertBadRequestException;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.FakeDataProvider.*;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCor;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCorComment;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCorCommentInfo;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.model.User.Role.MANAGER;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.CorNotificationRequested;
import school.hei.haapi.endpoint.rest.api.CorApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CorRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.CorCommentService;

class CorIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private CorRepository corRepository;
  @Autowired private CorCommentService corCommentService;
  @Autowired private CorMapper corMapper;
  @Autowired private UserMapper userMapper;
  @MockBean private EventProducer<CorNotificationRequested> corNotificationMock;
  private static final Faker faker = new Faker();

  private User axelWithCor;
  private User tolotraWithoutCor;
  private User manager;
  private Cor corAxel;
  private String axelToken;
  private String tolotraToken;
  private String managerToken;

  @BeforeEach
  void setUp() {
    axelWithCor = userRepository.save(someStudent("axel"));
    tolotraWithoutCor = userRepository.save(someStudent("tolotra"));
    manager = userRepository.save(someUser("manager", MANAGER));

    corAxel =
        corRepository.save(
            someCor(axelWithCor, Instant.parse("2025-01-01T10:00:00Z"), List.of(manager)));

    someCorComment(faker.number().numberBetween(0, 2))
        .forEach(c -> corCommentService.addCommentByCorId(corAxel.getId(), c));
    corAxel = corRepository.findById(corAxel.getId()).get();

    axelToken = tokenFor(casdoorAuthServiceMock, axelWithCor);
    tolotraToken = tokenFor(casdoorAuthServiceMock, tolotraWithoutCor);
    managerToken = tokenFor(casdoorAuthServiceMock, manager);
    when(cognitoComponentMock.getEmailByIdToken(axelToken)).thenReturn(axelWithCor.getEmail());
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
  void student_get_own_cor_by_id_ok() throws ApiException {
    var api = new CorApi(anApiClient(axelToken));

    var studentCor = api.getCorById(corAxel.getId());

    assertEquals(corMapper.toRest(corAxel), studentCor);
  }

  @Test
  void student_get_other_cor_ko() {
    var api = new CorApi(anApiClient(tolotraToken));

    assertThrowsForbiddenException(() -> api.getStudentCors(axelWithCor.getId(), null, null));
  }

  @Test
  void student_create_cor_ko() {
    var api = new CorApi(anApiClient(axelToken));
    var cor = someCreatableCor(tolotraWithoutCor.getId());

    assertThrowsForbiddenException(() -> api.crupdateStudentCors(tolotraWithoutCor.getId(), cor));
  }

  @Test
  void manager_create_cor_and_notify_student_ok() throws ApiException {
    var api = new CorApi(anApiClient(managerToken));
    var cor =
        someCreatableCor(
            tolotraWithoutCor.getId(),
            faker.options().option(school.hei.haapi.endpoint.rest.model.CorStatus.class),
            List.of(manager.getId()));

    var createdCor = api.crupdateStudentCors(tolotraWithoutCor.getId(), cor);

    var interviewers = createdCor.getInterviewers();
    assertNotNull(interviewers);
    assertEquals(1, interviewers.size());
    assertEquals(userMapper.toIdentifier(manager), interviewers.getFirst());
    assertEquals(
        corMapper
            .toRest(corMapper.toDomain(cor))
            .id(createdCor.getId())
            .creationDatetime(createdCor.getCreationDatetime()),
        createdCor);

    ArgumentCaptor<List<CorNotificationRequested>> notificationBodyCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(corNotificationMock, times(1)).accept(notificationBodyCaptor.capture());
    var notifications = notificationBodyCaptor.getValue();
    assertEquals(1, notifications.size());
    assertEquals(createdCor.getId(), notifications.getFirst().getCorId());
  }

  @Test
  void manager_update_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(managerToken));
    var updateCor = someCreatableCor(axelWithCor.getId(), LEAVE, List.of(manager.getId()));
    updateCor.setId(corAxel.getId());
    var expectedLengthOfInterviewers = 1;

    var updatedCor = api.crupdateStudentCors(axelWithCor.getId(), updateCor);

    assertEquals(updateCor.getDescription(), updatedCor.getDescription());
    assertEquals(userMapper.toIdentifier(axelWithCor), updatedCor.getConcernedStudent());
    assertEquals(updateCor.getInterviewDate(), updatedCor.getInterviewDate());
    assertEquals(updateCor.getStatus(), updatedCor.getStatus());

    assertNotNull(updateCor.getInterviewerIds());
    assertNotNull(updatedCor.getInterviewers());
    assertEquals(expectedLengthOfInterviewers, updatedCor.getInterviewers().size());
    assertEquals(userMapper.toIdentifier(manager), updatedCor.getInterviewers().getFirst());
  }

  @Test
  void manager_create_cor_without_status_ko() {
    var api = new CorApi(anApiClient(managerToken));
    var cor = someCreatableCor(tolotraWithoutCor.getId(), null, List.of());

    assertBadRequestException(
        "Status is mandatory", () -> api.crupdateStudentCors(tolotraWithoutCor.getId(), cor));
  }

  @Test
  void manager_filter_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(managerToken));

    var cors = api.getCors(null, null, null, null, null, null, null);
    assertTrue(cors.contains(corMapper.toRest(corAxel)));

    var corsFilterByStudentRef =
        api.getCors(null, null, null, null, axelWithCor.getRef(), null, null);
    assertTrue(corsFilterByStudentRef.contains(corMapper.toRest(corAxel)));
    assertFalse(
        corsFilterByStudentRef.contains(
            corMapper.toRest(Cor.builder().student(tolotraWithoutCor).build())));
    corAxel = corRepository.save(corAxel.toBuilder().status(CorStatus.LEAVE).build());

    var corsFilterByStatus = api.getCors(1, 1, null, null, null, null, List.of(LEAVE));
    assertEquals(1, corsFilterByStatus.size());
    assertEquals(LEAVE, corsFilterByStatus.getFirst().getStatus());
  }

  @Test
  void manager_comment_cor_ok() throws ApiException {
    var api = new CorApi(anApiClient(managerToken));
    var corId = corAxel.getId();
    var newCorComment = someCorCommentInfo();
    var initialCommentCount = corAxel.getComments().size();

    api.commentCorById(corId, newCorComment);

    var findCor = corRepository.findById(corId);
    assertTrue(findCor.isPresent());
    var cor = findCor.get();
    assertEquals(initialCommentCount + 1, cor.getComments().size());
    var lastCorComment = cor.getLastComment();
    assertTrue(lastCorComment.isPresent());
    assertEquals(newCorComment.getComment(), lastCorComment.get().getComment());
  }

  @Test
  void student_comment_cor_ko() {
    var api = new CorApi(anApiClient(tolotraToken));
    var corId = corAxel.getId();

    assertThrowsForbiddenException(() -> api.commentCorById(corId, someCorCommentInfo()));
  }

  @Test
  void manager_get_cor_by_id_ok() throws ApiException {
    var api = new CorApi(anApiClient(managerToken));
    var cor = api.getCorById(corAxel.getId());

    assertEquals(corMapper.toRest(corAxel), cor);
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }
}
