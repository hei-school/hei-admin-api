package school.hei.haapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static school.hei.haapi.model.User.Status.ALUMNI;

import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Testcontainers
@AutoConfigureMockMvc
class StudentElevationServiceTest extends FacadeITMockedThirdParties {
    @Autowired private StudentElevationService studentElevationService;
    @MockBean private UserRepository userRepository;
    @MockBean private UserService userService;
    @MockBean private GradeResultService gradeResultService;

    private User user;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("STD25000");
        user.setStatus(User.Status.ENABLED);
        user.setRole(User.Role.STUDENT);
        user.setRef("REF001");
        user.setEmail("student@mail.com");
        user.setLastName("Doe");
        user.setFirstName("J");
        user1 = new User();
        user1.setId("STD25001");
        user1.setStatus(User.Status.ENABLED);
        user1.setRole(User.Role.STUDENT);
        user1.setRef("REF002");
        user1.setEmail("student1@mail.com");
        user1.setLastName("Bob");
        user1.setFirstName("J");
        user2 = new User();
        user2.setId("STD25002");
        user2.setStatus(User.Status.ENABLED);
        user2.setRole(User.Role.STUDENT);
        user2.setRef("REF003");
        user2.setEmail("student2@mail.com");
        user2.setLastName("Alice");
        user2.setFirstName("J");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void elevate_single_student_success() {
        ResultSummary summary = mock(ResultSummary.class);
        when(summary.getObtainedCredits()).thenReturn(BigDecimal.valueOf(180));
        // when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.findById(user.getId())).thenReturn(user);
        when(gradeResultService.getStudentResultSummary(user.getId())).thenReturn(summary);
        List<Map<String, Object>> results = studentElevationService.elevateStudentByUserIdOrGroupId(List.of(user.getId()));
        assertEquals(1, results.size());
        Map<String, Object> result = results.getFirst();
        assertEquals(user.getId(), result.get("id"));
        assertEquals(Boolean.TRUE, result.get("elevated"));
        assertNull(result.get("error"));
        assertEquals(ALUMNI, user.getStatus());
    }

    @Test
    void elevate_single_non_student_returns_error() {
        user.setRole(User.Role.STAFF_MEMBER);
        when(userService.findById(user.getId())).thenReturn(user);
        List<Map<String, Object>> results = studentElevationService.elevateStudentByUserIdOrGroupId(List.of(user.getId()));
        assertEquals(1, results.size());
        Map<String, Object> result = results.getFirst();
        assertEquals(user.getId(), result.get("id"));
        assertEquals(Boolean.FALSE, result.get("elevated"));
        assertEquals("Not a student", result.get("error"));
    }

    @Test
    void elevate_single_student_with_not_enough_credits_returns_error() {
        ResultSummary summary = mock(ResultSummary.class);
        when(summary.getObtainedCredits()).thenReturn(BigDecimal.valueOf(150));
        when(userService.findById(user.getId())).thenReturn(user);
        when(gradeResultService.getStudentResultSummary(user.getId())).thenReturn(summary);
        List<Map<String, Object>> results = studentElevationService.elevateStudentByUserIdOrGroupId(List.of(user.getId()));
        assertEquals(1, results.size());
        Map<String, Object> result = results.getFirst();
        assertEquals(user.getId(), result.get("id"));
        assertEquals(Boolean.FALSE, result.get("elevated"));
        assertEquals("Insufficient credits", result.get("error"));
    }

    @Test
    void elevate_group_notFoundOrEmpty_returns_error(){
        String groupId = "A1";
        when(userService.findById(groupId)).thenThrow(new NotFoundException("not found"));
        when(userRepository.findAllRemainingStudentsByGroupIds(List.of(groupId), Pageable.unpaged())).thenReturn(List.of());
        List<Map<String, Object>> results = studentElevationService.elevateStudentByUserIdOrGroupId(List.of(groupId));
        assertEquals(1, results.size());
        Map<String, Object> result = results.getFirst();
        assertEquals(groupId, result.get("id"));
        assertEquals(Boolean.FALSE, result.get("elevated"));
        assertEquals("GroupId not found or group contains no students", result.get("error"));
    }

    @Test
    void elevate_group_processes_each_student_and_avoid_duplicates(){
        String groupeId = "B1";
        when(userService.findById(groupeId)).thenThrow(new NotFoundException("not a user"));
        when(userRepository.findAllRemainingStudentsByGroupIds(List.of(groupeId), Pageable.unpaged())).thenReturn(List.of(user1,user2));
        ResultSummary summary1 = mock(ResultSummary.class);
        when(summary1.getObtainedCredits()).thenReturn(BigDecimal.valueOf(180));
        ResultSummary summary2 = mock(ResultSummary.class);
        when(summary2.getObtainedCredits()).thenReturn(BigDecimal.valueOf(170));
        when(gradeResultService.getStudentResultSummary(user1.getId())).thenReturn(summary1);
        when(gradeResultService.getStudentResultSummary(user2.getId())).thenReturn(summary2);
        List<Map<String, Object>> results = studentElevationService.elevateStudentByUserIdOrGroupId(List.of(groupeId));
        assertEquals(2, results.size());
        Map<String, Object> result1 = results.stream().filter(m -> m.get("id").equals(user1.getId())).findFirst().get();
        Map<String, Object> result2 = results.stream().filter(m -> m.get("id").equals(user2.getId())).findFirst().get();
        assertEquals(Boolean.TRUE, result1.get("elevated"));
        assertNull(result1.get("error"));
        assertEquals(ALUMNI, user1.getStatus());
        assertEquals(Boolean.FALSE, result2.get("elevated"));
        assertEquals("Insufficient credits", result2.get("error"));
    }

}


