package school.hei.haapi.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.api.RetakeExamApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.UpdateRetakeExamStatus;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.RetakeExamRepository;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.repository.UserRepository;

import java.time.Instant;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static school.hei.haapi.endpoint.rest.model.RetakeExamStatus.INVALIDATE;
import static school.hei.haapi.endpoint.rest.model.RetakeExamStatus.VALIDATE;
import static school.hei.haapi.integration.conf.TestUtils.ADMIN1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.GroupTestData.h1;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.ryan;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;
import static school.hei.haapi.model.RetakeExamStatus.REGISTERED;

class StudentRetakeExamIT extends FacadeITMockedThirdParties {
    @Autowired private CourseRepository courseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CourseAssignmentRepository courseAssignmentRepository;
    @Autowired private GroupFlowRepository groupFlowRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private RetakeExamRepository retakeExamRepository;
    @Autowired private RetakeExamSessionRepository retakeExamSessionRepository;
    @MockBean
    private EventProducer eventProducer;

    private Instant now;
    private User studentAxel;
    private User repeatingStudentTolojanahary;
    private User teacherRyan;
    private GroupFlow axelGroupFlow;
    private Course anL1Course;
    private CourseAssignment anL1Assignment;
    private CourseAssignment newL1Assignment;
    private Group group1;
    private Group group2;
    private Group group3;
    private GroupFlow tolojanaharyNewGroupFlow;
    private GroupFlow tolojanaharyJoinOldGroupFlow;
    private GroupFlow tolojanaharyLeaveOldGroupFlow;
    private Exam groupGProg1Exam;
    private Exam groupHProg1Exam;
    private Grade repeatingYearGrade;
    private Grade axelYearGrade;
    private Grade oldYearGrade;
    private RetakeExam axelRetakeExam;
    private RetakeExam tolojanaharyRetakeExam;
    private RetakeExamSession retakeExamSession;
    private List<UpdateRetakeExamStatus> updateRetakeExamStatuses;
    private List<RetakeExam> savedRetakeExams;
    private Course savedCourse;
    private RetakeExamSession savedRetakeExamSession;
    private List<User> savedUsers;

    private ApiClient anApiClient(String token){
       return TestUtils.anApiClient(token, localPort);
    }

    @BeforeEach
    void setUp() {
        setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
        setUpCognito(cognitoComponentMock);
        setUpTestData();
    }

    void setUpTestData() {
        now = Instant.now();
        studentAxel = axel();
        repeatingStudentTolojanahary = tolojanahary();
        teacherRyan = ryan();
        group1 = g1();
        group2 = g2();
        group3 = h1();
        anL1Course = prog1();
        axelGroupFlow =
                GroupFlow.builder()
                        .student(studentAxel)
                        .group(group1)
                        .groupFlowType(JOIN)
                        .flowDatetime(now.minus(365, DAYS))
                        .build();
        tolojanaharyJoinOldGroupFlow =
                GroupFlow.builder()
                        .student(repeatingStudentTolojanahary)
                        .group(group1)
                        .groupFlowType(JOIN)
                        .flowDatetime(now.minus(365, DAYS))
                        .build();
        tolojanaharyLeaveOldGroupFlow =
                GroupFlow.builder()
                        .student(repeatingStudentTolojanahary)
                        .group(group1)
                        .groupFlowType(LEAVE)
                        .flowDatetime(now.minus(50, DAYS))
                        .build();
        tolojanaharyNewGroupFlow =
                GroupFlow.builder()
                        .student(repeatingStudentTolojanahary)
                        .group(group3)
                        .groupFlowType(JOIN)
                        .flowDatetime(now.minus(40, DAYS))
                        .build();
        anL1Assignment =
                CourseAssignment.builder()
                        .course(anL1Course)
                        .groups(List.of(group1, group2))
                        .mainTeacher(teacherRyan)
                        .build();
        newL1Assignment =
                CourseAssignment.builder()
                        .course(anL1Course)
                        .groups(List.of(group3))
                        .mainTeacher(teacherRyan)
                        .build();
        groupGProg1Exam = createExam(now.minus(200, DAYS), anL1Assignment);
        groupHProg1Exam = createExam(now.minus(10, DAYS), newL1Assignment);
        oldYearGrade =
                Grade.builder()
                        .student(repeatingStudentTolojanahary)
                        .exam(groupGProg1Exam)
                        .score(12.)
                        .build();
        repeatingYearGrade =
                Grade.builder()
                        .student(repeatingStudentTolojanahary)
                        .exam(groupHProg1Exam)
                        .score(9.)
                        .build();
        axelYearGrade =
                Grade.builder()
                        .student(studentAxel)
                        .exam(groupGProg1Exam)
                        .score(8.)
                        .build();
        savedCourse = courseRepository.save(anL1Course);
        retakeExamSession = RetakeExamSession.builder().id("retake-exam-session-id").title("test session").dateFrom(now.minus(9, DAYS)).dateTo(now.minus(5, DAYS)).build();
        savedRetakeExamSession = retakeExamSessionRepository.save(retakeExamSession);
        savedUsers = userRepository.saveAll(List.of(studentAxel, repeatingStudentTolojanahary, teacherRyan));
        axelRetakeExam = RetakeExam.builder().id("re-axel-id").course(savedCourse).student(savedUsers.getFirst()).status(REGISTERED).session(savedRetakeExamSession).build();
        tolojanaharyRetakeExam = RetakeExam.builder().id("re-tolojanahary-id").student(savedUsers.get(1)).course(savedCourse).status(REGISTERED).session(savedRetakeExamSession).build();
        savedRetakeExams = retakeExamRepository.saveAll(List.of(axelRetakeExam, tolojanaharyRetakeExam));
        updateRetakeExamStatuses = List.of(
                new UpdateRetakeExamStatus().retakeExamId(savedRetakeExams.getFirst().getId()).status(INVALIDATE),
                new UpdateRetakeExamStatus().retakeExamId(savedRetakeExams.getLast().getId()).status(VALIDATE)
        );
        groupRepository.saveAll(List.of(group1, group2, group3));
        courseAssignmentRepository.saveAll(
                List.of(anL1Assignment, newL1Assignment));
        examRepository.saveAll(List.of(groupGProg1Exam, groupHProg1Exam));
        gradeRepository.saveAll(List.of(repeatingYearGrade, axelYearGrade, oldYearGrade));
        groupFlowRepository.saveAll(
                List.of(
                        axelGroupFlow,
                        tolojanaharyJoinOldGroupFlow,
                        tolojanaharyLeaveOldGroupFlow,
                        tolojanaharyNewGroupFlow));
    }

    @AfterEach
    void tearDown() {
        retakeExamRepository.deleteAll(savedRetakeExams);
        retakeExamSessionRepository.delete(retakeExamSession);
        retakeExamRepository.deleteAll(List.of(axelRetakeExam, tolojanaharyRetakeExam));
        gradeRepository.deleteAll(List.of(oldYearGrade, axelYearGrade, repeatingYearGrade));
        courseAssignmentRepository.deleteAll(
                List.of(anL1Assignment, newL1Assignment));
        courseRepository.deleteAll(List.of(anL1Course));
        groupFlowRepository.deleteAll(
                List.of(
                        axelGroupFlow,
                        tolojanaharyJoinOldGroupFlow,
                        tolojanaharyLeaveOldGroupFlow,
                        tolojanaharyNewGroupFlow));
        groupRepository.deleteAll(List.of(group1, group2, group3));
        userRepository.deleteAll(List.of(studentAxel, repeatingStudentTolojanahary, teacherRyan));
    }

//    @Test
//    void manager_validate_students_retake_exams_OK() throws ApiException {
//        var anApiClient = anApiClient(ADMIN1_TOKEN);
//        var api = new RetakeExamApi(anApiClient);
//        var actual = api.updateRetakeExamsStatus(updateRetakeExamStatuses);
//        assertNotNull(actual);
//    }
}
