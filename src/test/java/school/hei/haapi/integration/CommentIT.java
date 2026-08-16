package school.hei.haapi.integration;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.haapi.endpoint.rest.model.OrderDirection.ASC;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestAuth.tokenFor;
import static school.hei.haapi.integration.conf.TestMocks.setUpS3Service;
import static school.hei.haapi.integration.testData.CommentTestData.aComment;
import static school.hei.haapi.integration.testData.ManagerTestData.hasina;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.integration.testData.StudentTestData.freddy;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.api.CommentsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CreateComment;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Comment;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CommentRepository;
import school.hei.haapi.repository.UserRepository;

class CommentIT extends FacadeITMockedThirdParties {
  @Autowired private UserRepository userRepository;
  @Autowired private CommentRepository commentRepository;

  private User studentAxel;
  private User studentFreddy;
  private User teacherToky;
  private User managerHasina;
  private Comment axelCommentByTeacher;
  private Comment axelCommentByManager;
  private Comment freddyCommentByTeacher;

  private String axelToken;
  private String teacherToken;
  private String managerToken;

  void setUpTestData() {
    studentAxel = userRepository.save(axel());
    studentFreddy = userRepository.save(freddy());
    teacherToky = userRepository.save(toky());
    managerHasina = userRepository.save(hasina());

    axelCommentByTeacher =
        commentRepository.save(aComment(studentAxel, teacherToky, "Good student"));
    axelCommentByManager =
        commentRepository.save(aComment(studentAxel, managerHasina, "Disruptive student"));
    freddyCommentByTeacher =
        commentRepository.save(aComment(studentFreddy, teacherToky, "Nothing to say here"));
  }

  @BeforeEach
  void setUp() {
    setUpTestData();
    setUpS3Service(fileService, studentAxel);

    axelToken = tokenFor(casdoorAuthServiceMock, studentAxel);
    teacherToken = tokenFor(casdoorAuthServiceMock, teacherToky);
    managerToken = tokenFor(casdoorAuthServiceMock, managerHasina);
  }

  @AfterEach
  void tearDown() {
    commentRepository.deleteAll(
        List.of(axelCommentByTeacher, axelCommentByManager, freddyCommentByTeacher));
    userRepository.deleteAll(List.of(studentAxel, studentFreddy, teacherToky, managerHasina));
  }

  private CommentsApi apiAs(String token) {
    return new CommentsApi(anApiClient(token));
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, localPort);
  }

  @Test
  void manager_read_comments_by_student_ref_ok() throws ApiException {
    var actual = apiAs(managerToken).getComments(1, 10, null, studentAxel.getRef());

    assertEquals(2, actual.size());
    var ids = actual.stream().map(comment -> comment.getId()).toList();
    assertTrue(ids.contains(axelCommentByTeacher.getId()));
    assertTrue(ids.contains(axelCommentByManager.getId()));
  }

  @Test
  void teacher_read_comments_ok() throws ApiException {
    var api = apiAs(teacherToken);

    var ascendant = api.getComments(1, 10, ASC, studentAxel.getRef());
    var descendant = api.getComments(1, 10, null, studentAxel.getRef());

    assertEquals(axelCommentByTeacher.getId(), ascendant.getFirst().getId());
    assertEquals(axelCommentByTeacher.getContent(), ascendant.getFirst().getContent());
    assertEquals(axelCommentByManager.getId(), descendant.getFirst().getId());
  }

  @Test
  void manager_read_comment_about_a_student_ok() throws ApiException {
    var actual = apiAs(managerToken).getStudentComments(studentAxel.getId(), null, 1, 15);

    var ids = actual.stream().map(comment -> comment.getId()).toList();
    assertTrue(ids.contains(axelCommentByTeacher.getId()));
    assertTrue(ids.contains(axelCommentByManager.getId()));
  }

  @Test
  void teacher_read_comment_about_a_student_ok() throws ApiException {
    var actual = apiAs(teacherToken).getStudentComments(studentAxel.getId(), null, 1, 15);

    var ids = actual.stream().map(comment -> comment.getId()).toList();
    assertTrue(ids.contains(axelCommentByTeacher.getId()));
    assertTrue(ids.contains(axelCommentByManager.getId()));
  }

  @Test
  void student_read_comment_about_him_ok() throws ApiException {
    var actual = apiAs(axelToken).getStudentComments(studentAxel.getId(), null, 1, 15);

    var ids = actual.stream().map(comment -> comment.getId()).toList();
    assertTrue(ids.contains(axelCommentByTeacher.getId()));
    assertTrue(ids.contains(axelCommentByManager.getId()));
  }

  @Test
  void student_read_comment_about_other_student_ko() {
    var api = apiAs(axelToken);
    assertThrowsForbiddenException(
        () -> api.getStudentComments(studentFreddy.getId(), null, 1, 15));
  }

  @Test
  void manager_comment_about_a_student_ok() throws ApiException {
    var createdId = randomCommentId();
    var created =
        apiAs(managerToken)
            .postComment(
                studentAxel.getId(),
                managerHasina.getId(),
                new CreateComment()
                    .id(createdId)
                    .content("Comment about axel")
                    .studentId(studentAxel.getId())
                    .observerId(managerHasina.getId()));

    assertEquals("Comment about axel", created.getContent());
    assertEquals(managerHasina.getRef(), created.getObserver().getRef());
    assertEquals(studentAxel.getRef(), created.getSubject().getRef());

    commentRepository.deleteById(created.getId());
  }

  @Test
  void teacher_comment_about_a_student_ok() throws ApiException {
    var createdId = randomCommentId();
    var created =
        apiAs(teacherToken)
            .postComment(
                studentAxel.getId(),
                teacherToky.getId(),
                new CreateComment()
                    .id(createdId)
                    .content("Comment about axel")
                    .studentId(studentAxel.getId())
                    .observerId(teacherToky.getId()));

    assertEquals("Comment about axel", created.getContent());
    assertEquals(teacherToky.getRef(), created.getObserver().getRef());
    assertEquals(studentAxel.getRef(), created.getSubject().getRef());

    commentRepository.deleteById(created.getId());
  }

  private static String randomCommentId() {
    return randomUUID().toString();
  }
}
