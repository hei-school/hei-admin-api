package school.hei.haapi.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.rest.api.CommentsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.Comment;
import school.hei.haapi.integration.conf.AbstractContextInitializer;
import school.hei.haapi.integration.conf.MockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT2_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_ID;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.anAvailableRandomPort;
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsForbiddenException;
import static school.hei.haapi.integration.conf.TestUtils.comment1;
import static school.hei.haapi.integration.conf.TestUtils.comment2;
import static school.hei.haapi.integration.conf.TestUtils.commentCreatedByManager;
import static school.hei.haapi.integration.conf.TestUtils.commentCreatedByTeacher;
import static school.hei.haapi.integration.conf.TestUtils.createCommentByManager;
import static school.hei.haapi.integration.conf.TestUtils.createCommentByTeacher;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = CommentIT.ContextInitializer.class)
@AutoConfigureMockMvc
@Slf4j
class CommentIT extends MockedThirdParties {

    @BeforeEach
    void setUp() {
        setUpCognito(cognitoComponentMock);
        setUpS3Service(fileService, student1());
    }

    @Test
    void manager_read_comment_about_student1_ok() throws ApiException {
        ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
        CommentsApi api = new CommentsApi(apiClient);

        List<Comment> actual = api.getComments(STUDENT1_ID, null, 1, 15);

        log.info(actual.toString());

        assertTrue(actual.contains(comment1()));
        assertTrue(actual.contains(comment2()));
    }

    @Test
    void teacher_read_comment_about_student1_ok() throws ApiException {
        ApiClient apiClient = anApiClient(TEACHER1_TOKEN);
        CommentsApi api = new CommentsApi(apiClient);

        List<Comment> actual = api.getComments(STUDENT1_ID, null, 1, 15);

        log.info(actual.toString());

        assertTrue(actual.contains(comment1()));
        assertTrue(actual.contains(comment2()));
    }

    @Test
    void student1_read_comment_about_him_ok() throws ApiException{
        ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
        CommentsApi api = new CommentsApi(apiClient);

        List<Comment> actual = api.getComments(STUDENT1_ID, null, 1, 15);

        assertTrue(actual.contains(comment1()));
        assertTrue(actual.contains(comment2()));
    }

    @Test
    void student1_read_comment_about_other_student_ko() throws ApiException {
        ApiClient apiClient = anApiClient(STUDENT1_TOKEN);
        CommentsApi api = new CommentsApi(apiClient);

       assertThrowsForbiddenException(() -> api.getComments(STUDENT2_ID, null, 1, 15));
    }

    @Test
    void manager_comment_about_student1_ok() throws ApiException{
        ApiClient apiClient = anApiClient(MANAGER1_TOKEN);
        CommentsApi api = new CommentsApi(apiClient);

        Comment createdComment = api.postComment(STUDENT1_ID, MANAGER_ID, createCommentByManager());

        assertEquals(commentCreatedByManager().getContent(), createdComment.getContent());
        assertEquals(commentCreatedByManager().getObserver().getRef(), createdComment.getObserver().getRef());
        assertEquals(commentCreatedByManager().getSubject().getRef(), createdComment.getSubject().getRef());
    }

    @Test
    void teacher_comment_about_student1_ok() throws ApiException{
        ApiClient apiClient = anApiClient(TEACHER1_TOKEN);
        CommentsApi api = new CommentsApi(apiClient);

        Comment createdComment = api.postComment(STUDENT1_ID, TEACHER1_ID, createCommentByTeacher());

        assertEquals(commentCreatedByTeacher().getContent(), createdComment.getContent());
        assertEquals(commentCreatedByTeacher().getObserver().getRef(), createdComment.getObserver().getRef());
        assertEquals(commentCreatedByTeacher().getSubject().getRef(), createdComment.getSubject().getRef());
    }



    private static ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, CommentIT.ContextInitializer.SERVER_PORT);
    }

    static class ContextInitializer extends AbstractContextInitializer {
        public static final int SERVER_PORT = anAvailableRandomPort();

        @Override
        public int getServerPort() {
            return SERVER_PORT;
        }
    }

}
