package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.time.Instant.parse;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.AVAILABLE;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.GENERATING;
import static school.hei.haapi.integration.conf.ApiAssertions.assertThrowsDomainBadRequestException;
import static school.hei.haapi.model.CycleLevel.BACHELOR;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.YearlyResultTranscriptGeneration;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResultGenerationRequest;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.YearlyResultGenerationRequestRepository;
import school.hei.haapi.service.event.YearlyResultTranscriptGenerationService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GradeResultServiceTest {
  private final GradeRepository gradeRepository = mock();
  @Spy @InjectMocks private CourseAssignmentService courseAssignmentService;
  private final GroupFlowService groupFlowService = mock();
  private final CourseService courseService = mock();
  private final UserService userService = mock();
  private final BucketComponent bucketComponent = mock();
  private final FileInfoService fileInfoService = mock();
  private final EventProducer eventProducer = mock();
  private final Mailer mailer = mock();
  @Spy @InjectMocks private ExamService examService;
  private final YearlyResultGenerationRequestRepository yearlyResultGenerationRequestRepository =
      mock();
  private final YearlyResultGenerationService yearlyResultGenerationService =
      new YearlyResultGenerationService(
          new HtmlParser(),
          new PdfRenderer(),
          new Base64Converter(),
          yearlyResultGenerationRequestRepository,
          new ClassPathResourceResolver());
  private GradeResultService subject;

  private YearlyResultTranscriptGenerationService yearlyResultTranscriptGenerationService;
  private static final String STUDENT1_ID = "id";
  private static final String STUDENT2_ID = "student 2";
  private static final String STUDENT3_ID = "Student with missing grade";
  private static final String BAD_STUDENT_ID = "bad student";
  private static final String STUDENT1_FIRST_NAME = "Student";
  private static final String STUDENT1_LAST_NAME = "One";
  private static final String STUDENT1_REF = "STD1";
  private static final String STUDENT1_SPECIALIZATION_FIELD_STRING = "Transformation Numérique";
  private static final User student1 = mock();
  private static final User student2 = mock();
  private static final User student3 = mock();
  private static final User badStudent = mock();

  private static Promotion promotion() {
    return mockPromotion();
  }

  private static final String TEACHER_ID = "teacher";

  private static final String MGT1_COURSE_ASSIGNMENT_ID = "mgt1-ca";
  private static final String PROG1_COURSE_ASSIGNMENT_ID = "prog1-ca";
  private static final String DONNE1_COURSE_ASSIGNMENT_ID = "donne1-ca";
  private static final String WEB1_COURSE_ASSIGNMENT_ID = "web1-ca";
  private static final String SYS1_COURSE_ASSIGNMENT_ID = "sys1-ca";
  private static final String LV1_COURSE_ASSIGNMENT_ID = "lv1-ca";
  private static final String SECU3_COURSE_ASSIGNMENT_ID = "secu3-ca";
  private static final String L2_COURSE_ASSIGNMENT_ID = "l2-ca";
  private static final String L3_COURSE_ASSIGNMENT_ID = "l3-ca";
  private static final String BAD_COURSE_ASSIGNMENT_ID = "bad-ca";

  private static User teacher() {
    return mockUser(TEACHER_ID);
  }

  private static CourseAssignment mgt1CourseAssignment() {
    return mockCourseAssignment(
        MGT1_COURSE_ASSIGNMENT_ID, mgt1Course(), teacher(), List.of(mgt1Exam()));
  }

  private static CourseAssignment prog1CourseAssignment() {
    return mockCourseAssignment(
        PROG1_COURSE_ASSIGNMENT_ID, prog1Course(), teacher(), List.of(prog1Exam()));
  }

  private static CourseAssignment donne1CourseAssignment() {
    return mockCourseAssignment(
        DONNE1_COURSE_ASSIGNMENT_ID, donne1Course(), teacher(), List.of(donnees1Exam()));
  }

  private static CourseAssignment web1CourseAssignment() {
    return mockCourseAssignment(
        WEB1_COURSE_ASSIGNMENT_ID, web1Course(), teacher(), List.of(web1Exam()));
  }

  private static CourseAssignment sys1CourseAssignment() {
    return mockCourseAssignment(
        SYS1_COURSE_ASSIGNMENT_ID, sys1Course(), teacher(), List.of(sys1Exam()));
  }

  private static CourseAssignment lv1CourseAssignment() {
    return mockCourseAssignment(
        LV1_COURSE_ASSIGNMENT_ID, lv1Course(), teacher(), List.of(lv1Exam()));
  }

  private static CourseAssignment secu3CourseAssignment() {
    return mockCourseAssignment(SECU3_COURSE_ASSIGNMENT_ID, secu3Course(), teacher(), List.of());
  }

  private static CourseAssignment l2CourseAssignment() {
    return mockCourseAssignment(L2_COURSE_ASSIGNMENT_ID, l2Course(), teacher(), List.of(l2Exam()));
  }

  private static CourseAssignment l3CourseAssignment() {
    return mockCourseAssignment(L3_COURSE_ASSIGNMENT_ID, l3Course(), teacher(), List.of(l3Exam()));
  }

  private static CourseAssignment m1CourseAssignment() {
    return mockCourseAssignment(
        MGT1_COURSE_ASSIGNMENT_ID, m1Course(), teacher(), List.of(m1Exam()));
  }

  private static CourseAssignment badCourseAssignment() {
    return mockCourseAssignment(
        BAD_COURSE_ASSIGNMENT_ID, badCourse(), teacher(), List.of(badExam()));
  }

  private static final String MGT1_EXAM_ID = "mgt1 exam";
  private static final String PROG1_EXAM_ID = "prog1 exam";
  private static final String DONNEES1_EXAM_ID = "donnees1 exam";
  private static final String WEB1_EXAM_ID = "web1 exam";
  private static final String SYS1_EXAM_ID = "sys1 exam";
  private static final String LV1_EXAM_ID = "lv1 exam";
  private static final String L2_EXAM_ID = "l2 exam";
  private static final String L3_EXAM_ID = "l3 exam";
  private static final String M1_EXAM_ID = "m1 exam";
  private static final String BAD_EXAM_ID = "bad exam";

  private static Exam mgt1Exam() {
    return mockExam(MGT1_EXAM_ID, 1, 1);
  }

  private static Exam prog1Exam() {
    return mockExam(PROG1_EXAM_ID, 1, 1);
  }

  private static Exam donnees1Exam() {
    return mockExam(DONNEES1_EXAM_ID, 1, 1);
  }

  private static Exam web1Exam() {
    return mockExam(WEB1_EXAM_ID, 1, 1);
  }

  private static Exam sys1Exam() {
    return mockExam(SYS1_EXAM_ID, 1, 1);
  }

  private static Exam lv1Exam() {
    return mockExam(LV1_EXAM_ID, 1, 1);
  }

  private static Exam l2Exam() {
    return mockExam(L2_EXAM_ID, 1, 1);
  }

  private static Exam l3Exam() {
    return mockExam(L3_EXAM_ID, 1, 1);
  }

  private static Exam m1Exam() {
    return mockExam(M1_EXAM_ID, 1, 1);
  }

  private static Exam badExam() {
    return mockExam(BAD_EXAM_ID, 0, 1);
  }

  private static Group group() {
    return mockGroup(promotion(), GROUP_ID);
  }

  private static Group badGroup() {
    return mockGroup(promotion(), BAD_GROUP_ID);
  }

  private static Grade student1Mgt1Grade() {
    return mockGrade(mgt1Exam(), 17.75);
  }

  private static Grade student1Prog1Grade() {
    return mockGrade(prog1Exam(), 13.59);
  }

  private static Grade student1Donnees1Grade() {
    return mockGrade(donnees1Exam(), 15.4375);
  }

  private static Grade student1Web1Grade() {
    return mockGrade(web1Exam(), 18.75);
  }

  private static Grade student1Sys1Grade() {
    return mockGrade(sys1Exam(), 13.);
  }

  private static Grade student1Lv1Grade() {
    return mockGrade(lv1Exam(), 13.91);
  }

  private static Grade student1L2Grade() {
    return mockGrade(l2Exam(), 15);
  }

  private static Grade student1L3Grade() {
    return mockGrade(l3Exam(), 15);
  }

  private static Grade student2Mgt1Grade() {
    return mockGrade(mgt1Exam(), 14.75);
  }

  private static Grade student2Prog1Grade() {
    return mockGrade(prog1Exam(), 4.46);
  }

  private static Grade student2Donnees1Grade() {
    return mockGrade(donnees1Exam(), 6.);
  }

  private static Grade student2Web1Grade() {
    return mockGrade(web1Exam(), 7.5);
  }

  private static Grade student2Sys1Grade() {
    return mockGrade(sys1Exam(), 10.00);
  }

  private static Grade student2Lv1Grade() {
    return mockGrade(lv1Exam(), 3.91);
  }

  private static Grade student3Mgt1Grade() {
    return mockGrade(mgt1Exam(), 17.75);
  }

  private static Grade student3Sys1Grade() {
    return mockGrade(sys1Exam(), 13.);
  }

  private static Grade student3Prog1Grade() {
    return mockGrade(prog1Exam(), 13.59);
  }

  private static Grade student3Donnees1Grade() {
    return mockGrade(donnees1Exam(), 15.4375);
  }

  private static Grade student3Web1Grade() {
    return mockGrade(web1Exam(), 18.75);
  }

  private static final String MGT1_COURSE_ID = "mgt1";
  private static final String MGT1_COURSE_CODE = "MGT1";
  private static final String MGT1_COURSE_NAME = "Mgt 1";
  private static final String PROG1_COURSE_ID = "prog1";
  private static final String PROG1_COURSE_CODE = "PROG1";
  private static final String PROG1_COURSE_NAME = "Programation 1";
  private static final String DONNE1_COURSE_ID = "donne1";
  private static final String DONNE1_COURSE_CODE = "DONNES1";
  private static final String DONNE1_COURSE_NAME = "Donnees 1";
  private static final String WEB1_COURSE_ID = "web1";
  private static final String WEB1_COURSE_CODE = "WEB1";
  private static final String WEB1_COURSE_NAME = "Web 1";
  private static final String SYS1_COURSE_ID = "sys1";
  private static final String SYS1_COURSE_CODE = "SYS1";
  private static final String SYS1_COURSE_NAME = "Systeme et reseau 1";
  private static final String LV1_COURSE_ID = "lv1";
  private static final String LV1_COURSE_CODE = "LV1";
  private static final String LV1_COURSE_NAME = "Langue vivante 1";
  private static final String SECU3_COURSE_ID = "secu3";
  private static final String SECU3_COURSE_CODE = "SECU1";
  private static final String SECU3_COURSE_NAME = "Securite 3";
  private static final String L2_COURSE_ID = "l2";
  private static final String L2_COURSE_CODE = "L2";
  private static final String L2_COURSE_NAME = "Cours L2";
  private static final String L3_COURSE_ID = "l3";
  private static final String L3_COURSE_CODE = "L3";
  private static final String L3_COURSE_NAME = "Cours L3";
  private static final String M1_COURSE_ID = "m1";
  private static final String M1_COURSE_CODE = "m1";
  private static final String M1_COURSE_NAME = "Cours m1";
  private static final String BAD1_COURSE_ID = "bad course";
  private static final String BAD1_COURSE_CODE = "bad course";
  private static final String BAD1_COURSE_NAME = "Bad course";

  private static GroupFlow groupFlow() {
    return GroupFlow.builder().group(group()).build();
  }

  private static GroupFlow badGroupFlow() {
    return GroupFlow.builder().group(badGroup()).build();
  }

  private static GroupFlowPeriod groupFLowPeriod() {
    return new GroupFlowPeriod(
        group(), parse("2021-02-01T00:00:00Z"), parse("2029-01-01T00:00:00Z"));
  }

  private static GroupFlowPeriod badGroupFLowPeriod() {
    return new GroupFlowPeriod(
        badGroup(), parse("2021-02-01T00:00:00Z"), parse("2029-01-01T00:00:00Z"));
  }

  private static GroupFlowPeriod groupFLowPeriodWithNullEnd() {
    return new GroupFlowPeriod(group(), parse("2021-02-01T00:00:00Z"), null);
  }

  private static Course mgt1Course() {
    return mockCourse(MGT1_COURSE_ID, MGT1_COURSE_CODE, MGT1_COURSE_NAME, 8, L1);
  }

  private static Course prog1Course() {
    return mockCourse(PROG1_COURSE_ID, PROG1_COURSE_CODE, PROG1_COURSE_NAME, 12, L1);
  }

  private static Course donne1Course() {
    return mockCourse(DONNE1_COURSE_ID, DONNE1_COURSE_CODE, DONNE1_COURSE_NAME, 8, L1);
  }

  private static Course web1Course() {
    return mockCourse(WEB1_COURSE_ID, WEB1_COURSE_CODE, WEB1_COURSE_NAME, 12, L1);
  }

  private static Course sys1Course() {
    return mockCourse(SYS1_COURSE_ID, SYS1_COURSE_CODE, SYS1_COURSE_NAME, 12, L1);
  }

  private static Course lv1Course() {
    return mockCourse(LV1_COURSE_ID, LV1_COURSE_CODE, LV1_COURSE_NAME, 8, L1);
  }

  private static Course secu3Course() {
    return mockCourse(SECU3_COURSE_ID, SECU3_COURSE_CODE, SECU3_COURSE_NAME, 8, M1);
  }

  private static Course l2Course() {
    return mockCourse(L2_COURSE_ID, L2_COURSE_CODE, L2_COURSE_NAME, 60, L2);
  }

  private static Course l3Course() {
    return mockCourse(L3_COURSE_ID, L3_COURSE_CODE, L3_COURSE_NAME, 60, L3);
  }

  private static Course badCourse() {
    return mockCourse(BAD1_COURSE_ID, BAD1_COURSE_CODE, BAD1_COURSE_NAME, 0, L1);
  }

  private static Course m1Course() {
    return mockCourse(M1_COURSE_ID, M1_COURSE_CODE, M1_COURSE_NAME, 8, M1);
  }

  @BeforeEach
  void setUp() {
    subject =
        new GradeResultService(
            new CourseResultService(
                new CourseMapper(),
                courseAssignmentService,
                groupFlowService,
                gradeRepository,
                examService),
            yearlyResultGenerationService,
            bucketComponent,
            userService,
            fileInfoService,
            eventProducer);

    yearlyResultTranscriptGenerationService =
        new YearlyResultTranscriptGenerationService(subject, mailer, bucketComponent, userService);
    // Mock student1 grades
    when(student1.getId()).thenReturn(STUDENT1_ID);
    when(student1.getFirstName()).thenReturn(STUDENT1_FIRST_NAME);
    when(student1.getLastName()).thenReturn(STUDENT1_LAST_NAME);
    when(student1.getRef()).thenReturn(STUDENT1_REF);
    when(student1.getSpecializationFieldString()).thenReturn(STUDENT1_SPECIALIZATION_FIELD_STRING);
    when(student1.findCurrentGroup()).thenReturn(Optional.of(group()));
    doReturn(List.of(groupFlow())).when(student1).getGroupFlows();
    doReturn(List.of(groupFlow())).when(student2).getGroupFlows();
    doReturn(List.of(groupFlow())).when(student3).getGroupFlows();
    doReturn(List.of(badGroupFlow())).when(badStudent).getGroupFlows();

    // Mock student Ids
    when(student2.getId()).thenReturn(STUDENT2_ID);
    when(student3.getId()).thenReturn(STUDENT3_ID);
    when(badStudent.getId()).thenReturn(BAD_STUDENT_ID);

    // Mock student1 grades
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(mgt1CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1Mgt1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(prog1CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1Prog1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(donne1CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1Donnees1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(web1CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1Web1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(sys1CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1Sys1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(lv1CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1Lv1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(l2CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1L2Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(l3CourseAssignment().getId()), STUDENT1_ID))
        .thenReturn(List.of(student1L3Grade()));
    // Mock student2 grades
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(mgt1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of(student2Mgt1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(prog1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of(student2Prog1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(donne1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of(student2Donnees1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(web1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of(student2Web1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(sys1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of(student2Sys1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(lv1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of(student2Lv1Grade()));
    // Mock student3 grades: LV1 is missing
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(mgt1CourseAssignment().getId()), STUDENT3_ID))
        .thenReturn(List.of(student3Mgt1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(prog1CourseAssignment().getId()), STUDENT3_ID))
        .thenReturn(List.of(student3Prog1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(donne1CourseAssignment().getId()), STUDENT3_ID))
        .thenReturn(List.of(student3Donnees1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(web1CourseAssignment().getId()), STUDENT3_ID))
        .thenReturn(List.of(student3Web1Grade()));
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(sys1CourseAssignment().getId()), STUDENT3_ID))
        .thenReturn(List.of(student3Sys1Grade()));
    when(courseService.getByStudentLevel(L1))
        .thenReturn(
            List.of(
                mgt1Course(),
                prog1Course(),
                donne1Course(),
                web1Course(),
                sys1Course(),
                lv1Course()));
    when(courseService.getByStudentLevel(L2)).thenReturn(List.of(l2Course()));
    when(courseService.getByStudentLevel(L3)).thenReturn(List.of(l3Course()));
    when(courseService.getByStudentLevel(M1)).thenReturn(List.of(secu3Course()));

    when(userService.getById(STUDENT1_ID)).thenReturn(student1);
    when(userService.getById(STUDENT2_ID)).thenReturn(student2);
    when(userService.getById(STUDENT3_ID)).thenReturn(student3);
    when(userService.getById(BAD_STUDENT_ID)).thenReturn(badStudent);
    when(student1.findGroupAt(any())).thenReturn(Optional.of(group()));
    when(student2.findGroupAt(any())).thenReturn(Optional.of(group()));
    when(student3.findGroupAt(any())).thenReturn(Optional.of(group()));
    when(badStudent.findGroupAt(any())).thenReturn(Optional.of(badGroup()));

    doAnswer(
            invocation -> {
              var studentId = invocation.getArgument(0);
              var studentLevel = invocation.getArgument(1);
              if (BAD_STUDENT_ID.equals(studentId) && L1.equals(studentLevel)) {
                return List.of(badGroupFLowPeriod());
              }
              return List.of(groupFLowPeriod());
            })
        .when(groupFlowService)
        .findStudentLatestGroupFlowPeriodsAtLevel(any(), any());
    doAnswer(
            invocation -> {
              var argument = invocation.getArgument(0);
              if (argument.equals(group().getId())) {
                return List.of(
                    mgt1CourseAssignment(),
                    prog1CourseAssignment(),
                    donne1CourseAssignment(),
                    web1CourseAssignment(),
                    sys1CourseAssignment(),
                    lv1CourseAssignment(),
                    secu3CourseAssignment(),
                    l2CourseAssignment(),
                    l3CourseAssignment());
              }
              return List.of(badCourseAssignment());
            })
        .when(courseAssignmentService)
        .getByGroupId(anyString());
  }

  @Test
  void correct_result_yearly_result_student1_L1_validate() throws CoursesCreditSumZero {
    var targetLevel = L1;

    var result = subject.getYearlyResultByStudentIdAndByLevel(STUDENT1_ID, targetLevel);

    assertEquals(targetLevel, result.getLevel());
    assertEquals(60., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(15.347666666666667, result.getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, result.getStatus());
    assertEquals(60., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_yearly_result_student2_L1_invalidate() throws CoursesCreditSumZero {
    var targetLevel = L1;

    var result = subject.getYearlyResultByStudentIdAndByLevel(STUDENT2_ID, targetLevel);

    assertEquals(targetLevel, result.getLevel());
    assertEquals(20., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(7.68, result.getWeightedAverage().doubleValue());
    assertEquals(INVALIDATED, result.getStatus());
    assertEquals(60., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_yearly_result_student2_M1_notStarted() throws CoursesCreditSumZero {
    var targetLevel = M1;
    var m1CourseAssignment = m1CourseAssignment();
    m1CourseAssignment.setExams(List.of());
    when(gradeRepository.findGradesByCourseAssignmentIdsAndStudentId(
            List.of(m1CourseAssignment().getId()), STUDENT2_ID))
        .thenReturn(List.of());
    when(groupFlowService.findStudentLatestGroupFlowPeriodsAtLevel(any(), any()))
        .thenReturn(Collections.singletonList(groupFLowPeriodWithNullEnd()));
    var result = subject.getYearlyResultByStudentIdAndByLevel(STUDENT2_ID, targetLevel);

    assertEquals(targetLevel, result.getLevel());
    assertEquals(0, result.getObtainedCredits().doubleValue());
    assertEquals(1, result.getCourseResults().size());
    assertEquals(CourseResultStatus.NOT_STARTED, result.getCourseResults().getFirst().getStatus());
    assertNull(result.getWeightedAverage());
    assertEquals(NOT_STARTED, result.getStatus());
    assertEquals(8., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_yearly_result_student3_L1_inProgress() throws CoursesCreditSumZero {
    var targetLevel = L1;

    var result = subject.getYearlyResultByStudentIdAndByLevel(STUDENT3_ID, targetLevel);

    assertEquals(targetLevel, result.getLevel());
    assertEquals(52., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    var lv1Result = result.getCourseResults().get(5);
    assertEquals(LV1_COURSE_ID, lv1Result.getCourse().getId());
    assertEquals(CourseResultStatus.NOT_STARTED, lv1Result.getStatus());
    assertNull(lv1Result.getWeightedAverage());
    assertEquals(13.493, result.getWeightedAverage().doubleValue());
    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(60., result.getTotalCredits().doubleValue());
  }

  @Test
  void generate_result_pdf_okay() throws CoursesCreditSumZero {
    var result = subject.getYearlyResultByStudentIdAndByLevel(STUDENT1_ID, L1);
    var resultFile = yearlyResultGenerationService.generateYearlyResultTranscript(student1, result);
    assertTrue(resultFile.isFile());
  }

  @Test
  void generate_result_summary_pdf_okay() {
    var result = subject.getStudentResultSummary(STUDENT1_ID);
    var resultFile =
        yearlyResultGenerationService.generateResultSummaryTranscript(student1, result);
    assertTrue(resultFile.isFile());
  }

  @Test
  void generate_result_pdf_year_in_progress_okay() throws CoursesCreditSumZero {
    var result = subject.getYearlyResultByStudentIdAndByLevel(STUDENT3_ID, L1);
    var resultFile = yearlyResultGenerationService.generateYearlyResultTranscript(student3, result);
    assertTrue(resultFile.isFile());
  }

  @Test
  void correct_result_yearly_result_M2_empty_notStarted() {
    var expectedLevel = M2;
    var yearlyResult = subject.getYearlyResultByStudentIdAndByLevel(STUDENT1_ID, expectedLevel);

    assertEquals(expectedLevel, yearlyResult.getLevel());
    assertEquals(NOT_STARTED, yearlyResult.getStatus());
    assertNull(yearlyResult.getWeightedAverage());
  }

  @Test
  void correct_result_result_summary_student1_validated() {
    var result = subject.getStudentResultSummary(STUDENT1_ID);

    assertEquals(5, result.getYearlyResults().size());
    assertEquals(180., result.getObtainedCredits().doubleValue());
    assertEquals(15.11588888888889, result.getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, result.getStatus());
    assertEquals(180., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_result_summary_student2_invalidated() {
    var result = subject.getStudentResultSummary(STUDENT2_ID);

    assertEquals(5, result.getYearlyResults().size());
    assertEquals(20., result.getObtainedCredits().doubleValue());
    assertEquals(7.68, result.getWeightedAverage().doubleValue());
    assertEquals(INVALIDATED, result.getStatus());
    assertEquals(60., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_result_summary_student3_in_progress() {
    var result = subject.getStudentResultSummary(STUDENT3_ID);

    assertEquals(5, result.getYearlyResults().size());
    assertEquals(52., result.getObtainedCredits().doubleValue());
    assertEquals(13.493, result.getWeightedAverage().doubleValue());
    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(60., result.getTotalCredits().doubleValue());
  }

  @Test
  void yearly_result_with_course_credits_sum_zero_ko() {
    assertThrows(
        CoursesCreditSumZero.class,
        () -> subject.getYearlyResultByStudentIdAndByLevel(BAD_STUDENT_ID, L1));
  }

  @Test
  void yearly_result_generation_should_return_available_transcript_when_file_exists()
      throws MalformedURLException {

    when(userService.getById(anyString())).thenReturn(student1);
    when(yearlyResultGenerationService.findGenerationRequestByFileName(anyString()))
        .thenReturn(
            Optional.of(
                YearlyResultGenerationRequest.builder()
                    .fileInfo(
                        FileInfo.builder()
                            .id(randomUUID().toString())
                            .user(student1)
                            .filePath("dummy_path")
                            .build())
                    .status(AVAILABLE)
                    .datetime(now())
                    .build()));
    when(bucketComponent.presign(anyString(), any()))
        .thenReturn(URL.of(URI.create("https://example.com/transcript.pdf"), null));

    var result = subject.getYearlyResultTranscript(student1.getId(), L1);
    assertEquals(AVAILABLE, result.getStatus());
    assertFalse(result.getLink().isEmpty());
  }

  @Test
  void yearly_result_generation_should_return_generating_status_when_file_is_missing() {
    try (var mockedAuthProvider = mockStatic(AuthProvider.class)) {
      mockedAuthProvider
          .when(AuthProvider::getPrincipal)
          .thenReturn(new Principal(student1, "dummy"));
      when(userService.getById(anyString())).thenReturn(student1);
      when(yearlyResultGenerationService.findGenerationRequestByFileName(anyString()))
          .thenReturn(empty());

      var result = subject.getYearlyResultTranscript(student1.getId(), L1);

      assertEquals(GENERATING, result.getStatus());
      assertNull(result.getLink());
    }
  }

  @Test
  void yearly_result_generation_should_regenerate_when_generation_times_out() {
    try (var mockedAuthProvider = mockStatic(AuthProvider.class)) {
      mockedAuthProvider
          .when(AuthProvider::getPrincipal)
          .thenReturn(new Principal(student1, "dummy"));
      when(userService.getById(anyString())).thenReturn(student1);
      when(yearlyResultGenerationService.findGenerationRequestByFileName(anyString()))
          .thenReturn(
              Optional.of(
                  YearlyResultGenerationRequest.builder()
                      .status(GENERATING)
                      .datetime(now().minus(Duration.ofHours(1L)))
                      .build()));
      var result = subject.getYearlyResultTranscript(STUDENT1_ID, L1);
      assertEquals(GENERATING, result.getStatus());
      verify(eventProducer, only()).accept(anyList());
    }
  }

  @Test
  void yearly_result_generation_should_return_bad_request_when_level_in_notStarted() {
    assertThrowsDomainBadRequestException(
        "Cannot generate transcript for this level. This level has not yet been started",
        () -> subject.getYearlyResultTranscript(STUDENT3_ID, L2));
  }

  @Test
  void yearly_result_event_handler_ok() {
    var student1YearlyResult = subject.getYearlyResultByStudentIdAndByLevel(STUDENT1_ID, L1);

    when(userService.getById(anyString())).thenReturn(student1);
    when(yearlyResultGenerationRequestRepository.save(any())).thenAnswer(e -> e.getArgument(0));
    assertDoesNotThrow(
        () -> {
          yearlyResultTranscriptGenerationService.accept(
              YearlyResultTranscriptGeneration.builder()
                  .principalId(student1.getId())
                  .yearlyResult(student1YearlyResult)
                  .userId(STUDENT1_ID)
                  .build());
        });
  }

  private static Exam mockExam(String id, int coefficientNumerator, int coefficientDenominator) {
    return Exam.builder()
        .id(id)
        .coefficientNumerator(coefficientNumerator)
        .coefficientDenominator(coefficientDenominator)
        .examinationDate(parse("2025-01-01T10:00:00Z"))
        .build();
  }

  private static User mockUser(String id) {
    return User.builder().id(id).build();
  }

  private static Promotion mockPromotion() {
    return Promotion.builder()
        .ref("prom1")
        .cycleLevel(BACHELOR)
        .name("Promotion de test")
        .startDatetime(now())
        .build();
  }

  private static final String GROUP_ID = "test-grp";
  private static final String GROUP_NAME = "Groupe test";
  private static final String GROUP_REF = "GRP_TST";
  private static final String BAD_GROUP_ID = "bad-test-grp";

  private static Group mockGroup(Promotion promotion, String groupId) {
    return Group.builder().id(groupId).name(GROUP_NAME).ref(GROUP_REF).promotion(promotion).build();
  }

  private static Grade mockGrade(Exam exam, double score) {
    return Grade.builder().score(score).exam(exam).build();
  }

  private static Course mockCourse(
      String id, String code, String name, int credits, StudentLevel level) {
    return Course.builder()
        .id(id)
        .code(code)
        .name(name)
        .credits(credits)
        .studentLevel(level)
        .build();
  }

  private static CourseAssignment mockCourseAssignment(
      String id, Course course, User teacher, List<Exam> exams) {
    return CourseAssignment.builder()
        .id(id)
        .course(course)
        .mainTeacher(teacher)
        .groups(List.of(group()))
        .exams(exams)
        .build();
  }
}
