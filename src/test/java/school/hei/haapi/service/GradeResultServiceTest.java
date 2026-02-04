package school.hei.haapi.service;

import static java.time.Instant.now;
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
import static org.mockito.ArgumentMatchers.eq;
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
import static school.hei.haapi.integration.conf.TestUtils.assertThrowsDomainBadRequestException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.YearlyResultTranscriptGeneration;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
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
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResultGenerationRequest;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.repository.YearlyResultGenerationRequestRepository;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.event.YearlyResultTranscriptGenerationService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.CollectionUtils;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;

class GradeResultServiceTest {
  private final GradeDao gradeDao = mock();
  private final CourseService courseService = mock();
  private final ExamService examService = mock();
  private final UserService userService = mock();
  private final BucketComponent bucketComponent = mock();
  private final FileInfoService fileInfoService = mock();
  private final EventProducer eventProducer = mock();
  private final Mailer mailer = mock();
  private final YearlyResultGenerationRequestRepository yearlyResultGenerationRequestRepository =
      mock();
  private final YearlyResultGenerationService yearlyResultGenerationService =
      new YearlyResultGenerationService(
          new HtmlParser(),
          new PdfRenderer(),
          new Base64Converter(),
          yearlyResultGenerationRequestRepository,
          new ClassPathResourceResolver());
  private final GradeResultService subject =
      new GradeResultService(
          new CourseResultService(
              courseService,
              gradeDao,
              new CourseMapper(),
              examService,
              userService,
              new CollectionUtils()),
          yearlyResultGenerationService,
          bucketComponent,
          userService,
          fileInfoService,
          eventProducer);
  private final YearlyResultTranscriptGenerationService yearlyResultTranscriptGenerationService =
      new YearlyResultTranscriptGenerationService(subject, mailer, bucketComponent, userService);

  private static final String STUDENT1_ID = "id";
  private static final String STUDENT2_ID = "bad student";
  private static final String STUDENT3_ID = "Student with missing grade";
  private static final String STUDENT1_FIRST_NAME = "Student";
  private static final String STUDENT1_LAST_NAME = "One";
  private static final String STUDENT1_REF = "STD1";
  private static final String STUDENT1_SPECIALIZATION_FIELD_STRING = "Transformation Numérique";
  private static final User student1 = mock();
  private static final User student2 = mock();
  private static final User student3 = mock();

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
    return mockCourseAssignment(MGT1_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment prog1CourseAssignment() {
    return mockCourseAssignment(PROG1_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment donne1CourseAssignment() {
    return mockCourseAssignment(DONNE1_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment web1CourseAssignment() {
    return mockCourseAssignment(WEB1_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment sys1CourseAssignment() {
    return mockCourseAssignment(SYS1_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment lv1CourseAssignment() {
    return mockCourseAssignment(LV1_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment secu3CourseAssignment() {
    return mockCourseAssignment(SECU3_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment l2CourseAssignment() {
    return mockCourseAssignment(L2_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment l3CourseAssignment() {
    return mockCourseAssignment(L3_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static CourseAssignment badCourseAssignment() {
    return mockCourseAssignment(BAD_COURSE_ASSIGNMENT_ID, teacher());
  }

  private static final String MGT1_EXAM_ID = "mgt1 exam";
  private static final String PROG1_EXAM_ID = "prog1 exam";
  private static final String DONNEES1_EXAM_ID = "donnees1 exam";
  private static final String WEB1_EXAM_ID = "web1 exam";
  private static final String SYS1_EXAM_ID = "sys1 exam";
  private static final String LV1_EXAM_ID = "lv1 exam";
  private static final String L2_EXAM_ID = "l2 exam";
  private static final String L3_EXAM_ID = "l3 exam";
  private static final String BAD_EXAM_ID = "bad exam";

  private static Exam mgt1Exam() {
    return mockExam(MGT1_EXAM_ID, 1, 1, mgt1CourseAssignment());
  }

  private static Exam prog1Exam() {
    return mockExam(PROG1_EXAM_ID, 1, 1, prog1CourseAssignment());
  }

  private static Exam donnees1Exam() {
    return mockExam(DONNEES1_EXAM_ID, 1, 1, donne1CourseAssignment());
  }

  private static Exam web1Exam() {
    return mockExam(WEB1_EXAM_ID, 1, 1, web1CourseAssignment());
  }

  private static Exam sys1Exam() {
    return mockExam(SYS1_EXAM_ID, 1, 1, sys1CourseAssignment());
  }

  private static Exam lv1Exam() {
    return mockExam(LV1_EXAM_ID, 1, 1, lv1CourseAssignment());
  }

  private static Exam l2Exam() {
    return mockExam(L2_EXAM_ID, 1, 1, l2CourseAssignment());
  }

  private static Exam l3Exam() {
    return mockExam(L3_EXAM_ID, 1, 1, l3CourseAssignment());
  }

  private static Exam badExam() {
    return mockExam(BAD_EXAM_ID, 0, 1, badCourseAssignment());
  }

  private static Group group() {
    return mockGroup(promotion());
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

  private static Grade student3GradeForBadExam() {
    return mockGrade(badExam(), 13.59);
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
  private static final String BAD1_COURSE_ID = "bad course";
  private static final String BAD1_COURSE_CODE = "bad course";
  private static final String BAD1_COURSE_NAME = "Bad course";

  private static Course mgt1Course() {
    return mockCourse(
        MGT1_COURSE_ID, MGT1_COURSE_CODE, MGT1_COURSE_NAME, 8, mgt1CourseAssignment(), L1);
  }

  private static Course prog1Course() {
    return mockCourse(
        PROG1_COURSE_ID, PROG1_COURSE_CODE, PROG1_COURSE_NAME, 12, prog1CourseAssignment(), L1);
  }

  private static Course donne1Course() {
    return mockCourse(
        DONNE1_COURSE_ID, DONNE1_COURSE_CODE, DONNE1_COURSE_NAME, 8, donne1CourseAssignment(), L1);
  }

  private static Course web1Course() {
    return mockCourse(
        WEB1_COURSE_ID, WEB1_COURSE_CODE, WEB1_COURSE_NAME, 12, web1CourseAssignment(), L1);
  }

  private static Course sys1Course() {
    return mockCourse(
        SYS1_COURSE_ID, SYS1_COURSE_CODE, SYS1_COURSE_NAME, 12, sys1CourseAssignment(), L1);
  }

  private static Course lv1Course() {
    return mockCourse(
        LV1_COURSE_ID, LV1_COURSE_CODE, LV1_COURSE_NAME, 8, lv1CourseAssignment(), L1);
  }

  private static Course secu3Course() {
    return mockCourse(
        SECU3_COURSE_ID, SECU3_COURSE_CODE, SECU3_COURSE_NAME, 8, secu3CourseAssignment(), L1);
  }

  private static Course l2Course() {
    return mockCourse(L2_COURSE_ID, L2_COURSE_CODE, L2_COURSE_NAME, 60, l2CourseAssignment(), L2);
  }

  private static Course l3Course() {
    return mockCourse(L3_COURSE_ID, L3_COURSE_CODE, L3_COURSE_NAME, 60, l3CourseAssignment(), L3);
  }

  private static Course badCourse() {
    return mockCourse(
        BAD1_COURSE_ID, BAD1_COURSE_CODE, BAD1_COURSE_NAME, 0, badCourseAssignment(), L1);
  }

  @BeforeEach
  void setUp() {
    // Mock student1 grades
    when(student1.getId()).thenReturn(STUDENT1_ID);
    when(student1.getFirstName()).thenReturn(STUDENT1_FIRST_NAME);
    when(student1.getLastName()).thenReturn(STUDENT1_LAST_NAME);
    when(student1.getRef()).thenReturn(STUDENT1_REF);
    when(student1.getSpecializationFieldString()).thenReturn(STUDENT1_SPECIALIZATION_FIELD_STRING);
    when(student1.findCurrentGroup()).thenReturn(Optional.of(group()));

    // Mock student Ids
    when(student2.getId()).thenReturn(STUDENT2_ID);
    when(student3.getId()).thenReturn(STUDENT3_ID);

    // Mock student1 grades
    when(gradeDao.getStudentGradesByCourseId(MGT1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Mgt1Grade()));
    when(gradeDao.getStudentGradesByCourseId(PROG1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Prog1Grade()));
    when(gradeDao.getStudentGradesByCourseId(DONNE1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Donnees1Grade()));
    when(gradeDao.getStudentGradesByCourseId(WEB1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Web1Grade()));
    when(gradeDao.getStudentGradesByCourseId(SYS1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Sys1Grade()));
    when(gradeDao.getStudentGradesByCourseId(LV1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Lv1Grade()));
    when(gradeDao.getStudentGradesByCourseId(L2_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1L2Grade()));
    when(gradeDao.getStudentGradesByCourseId(L3_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1L3Grade()));

    // Mock student2 grades
    when(gradeDao.getStudentGradesByCourseId(MGT1_COURSE_ID, STUDENT2_ID))
        .thenReturn(List.of(student2Mgt1Grade()));
    when(gradeDao.getStudentGradesByCourseId(PROG1_COURSE_ID, STUDENT2_ID))
        .thenReturn(List.of(student2Prog1Grade()));
    when(gradeDao.getStudentGradesByCourseId(DONNE1_COURSE_ID, STUDENT2_ID))
        .thenReturn(List.of(student2Donnees1Grade()));
    when(gradeDao.getStudentGradesByCourseId(WEB1_COURSE_ID, STUDENT2_ID))
        .thenReturn(List.of(student2Web1Grade()));
    when(gradeDao.getStudentGradesByCourseId(SYS1_COURSE_ID, STUDENT2_ID))
        .thenReturn(List.of(student2Sys1Grade()));
    when(gradeDao.getStudentGradesByCourseId(LV1_COURSE_ID, STUDENT2_ID))
        .thenReturn(List.of(student2Lv1Grade()));

    // Mock student3 grades: LV1 is missing
    when(gradeDao.getStudentGradesByCourseId(MGT1_COURSE_ID, STUDENT3_ID))
        .thenReturn(List.of(student3Mgt1Grade()));
    when(gradeDao.getStudentGradesByCourseId(PROG1_COURSE_ID, STUDENT3_ID))
        .thenReturn(List.of(student3Prog1Grade()));
    when(gradeDao.getStudentGradesByCourseId(DONNE1_COURSE_ID, STUDENT3_ID))
        .thenReturn(List.of(student3Donnees1Grade()));
    when(gradeDao.getStudentGradesByCourseId(WEB1_COURSE_ID, STUDENT3_ID))
        .thenReturn(List.of(student3Web1Grade()));
    when(gradeDao.getStudentGradesByCourseId(SYS1_COURSE_ID, STUDENT3_ID))
        .thenReturn(List.of(student3Sys1Grade()));

    // Mock exam from course assignment
    when(examService.getExamsByCourseId(MGT1_COURSE_ID)).thenReturn(List.of(mgt1Exam()));
    when(examService.getExamsByCourseId(PROG1_COURSE_ID)).thenReturn(List.of(prog1Exam()));
    when(examService.getExamsByCourseId(DONNE1_COURSE_ID)).thenReturn(List.of(donnees1Exam()));
    when(examService.getExamsByCourseId(WEB1_COURSE_ID)).thenReturn(List.of(web1Exam()));
    when(examService.getExamsByCourseId(SYS1_COURSE_ID)).thenReturn(List.of(sys1Exam()));
    when(examService.getExamsByCourseId(LV1_COURSE_ID)).thenReturn(List.of(lv1Exam()));
    when(examService.getExamsByCourseId(L2_COURSE_ID)).thenReturn(List.of(l2Exam()));
    when(examService.getExamsByCourseId(L3_COURSE_ID)).thenReturn(List.of(l3Exam()));

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
    when(student1.findGroupAt(any())).thenReturn(Optional.of(group()));
    when(student2.findGroupAt(any())).thenReturn(Optional.of(group()));
    when(student3.findGroupAt(any())).thenReturn(Optional.of(group()));

    when(examService.getExamsByCourseAssignmentIds(eq(List.of(MGT1_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of(mgt1Exam()));
    when(examService.getExamsByCourseAssignmentIds(eq(List.of(PROG1_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of(prog1Exam()));
    when(examService.getExamsByCourseAssignmentIds(eq(List.of(DONNE1_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of(donnees1Exam()));
    when(examService.getExamsByCourseAssignmentIds(eq(List.of(WEB1_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of(web1Exam()));
    when(examService.getExamsByCourseAssignmentIds(eq(List.of(SYS1_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of(sys1Exam()));
    when(examService.getExamsByCourseAssignmentIds(eq(List.of(LV1_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of(lv1Exam()));
    when(examService.getExamsByCourseAssignmentIds(eq(List.of(SECU3_COURSE_ASSIGNMENT_ID))))
        .thenReturn(List.of());
    when(examService.getExamsByCourseAssignmentIds(List.of(L2_COURSE_ASSIGNMENT_ID)))
        .thenReturn(List.of(l2Exam()));
    when(examService.getExamsByCourseAssignmentIds(List.of(L3_COURSE_ASSIGNMENT_ID)))
        .thenReturn(List.of(l3Exam()));
    when(examService.getExamsByCourseId(eq(MGT1_COURSE_ID))).thenReturn(List.of(mgt1Exam()));
    when(examService.getExamsByCourseId(eq(PROG1_COURSE_ID))).thenReturn(List.of(prog1Exam()));
    when(examService.getExamsByCourseId(eq(DONNE1_COURSE_ID))).thenReturn(List.of(donnees1Exam()));
    when(examService.getExamsByCourseId(eq(WEB1_COURSE_ID))).thenReturn(List.of(web1Exam()));
    when(examService.getExamsByCourseId(eq(SYS1_COURSE_ID))).thenReturn(List.of(sys1Exam()));
    when(examService.getExamsByCourseId(eq(LV1_COURSE_ID))).thenReturn(List.of(lv1Exam()));
    when(examService.getExamsByCourseId(eq(SECU3_COURSE_ID))).thenReturn(List.of());
    when(examService.getExamsByCourseId(L2_COURSE_ID)).thenReturn(List.of(l2Exam()));
    when(examService.getExamsByCourseId(L3_COURSE_ID)).thenReturn(List.of(l3Exam()));
  }

  @Test
  void correct_result_yearly_result_student1_L1_validate() throws CoursesCreditSumZero {
    var targetLevel = L1;

    var result = subject.getLeveledYearlyResultByStudentId(targetLevel, student1.getId());

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

    var result = subject.getLeveledYearlyResultByStudentId(targetLevel, STUDENT2_ID);

    assertEquals(targetLevel, result.getLevel());
    assertEquals(20., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(7.68, result.getWeightedAverage().doubleValue());
    assertEquals(INVALIDATED, result.getStatus());
    assertEquals(60., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_yearly_result_student2_M1_notStated() throws CoursesCreditSumZero {
    var targetLevel = M1;

    var result = subject.getLeveledYearlyResultByStudentId(targetLevel, STUDENT2_ID);

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

    var result = subject.getLeveledYearlyResultByStudentId(targetLevel, STUDENT3_ID);

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
    var result = subject.getLeveledYearlyResultByStudentId(L1, STUDENT1_ID);
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
    var result = subject.getLeveledYearlyResultByStudentId(L1, STUDENT3_ID);
    var resultFile = yearlyResultGenerationService.generateYearlyResultTranscript(student1, result);
    assertTrue(resultFile.isFile());
  }

  @Test
  void correct_result_yearly_result_M2_empty_notStarted() {
    var expectedLevel = M2;
    var yearlyResult = subject.getLeveledYearlyResultByStudentId(expectedLevel, STUDENT1_ID);

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
    when(gradeDao.getStudentGradesByCourseId(MGT1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student1Mgt1Grade()));
    when(examService.getExamsByCourseId(BAD1_COURSE_ID)).thenReturn(List.of(mgt1Exam()));
    when(courseService.getByStudentLevel(eq(L1))).thenReturn(List.of(badCourse()));

    assertThrows(
        CoursesCreditSumZero.class,
        () -> subject.getLeveledYearlyResultByStudentId(L1, STUDENT1_ID));
  }

  @Test
  void course_result_with_exams_coefficient_sum_zero_is_inProgress() {
    when(gradeDao.getStudentGradesByCourseId(MGT1_COURSE_ID, STUDENT1_ID))
        .thenReturn(List.of(student3GradeForBadExam()));
    when(examService.getExamsByCourseId(MGT1_COURSE_ID)).thenReturn(List.of(badExam()));
    when(courseService.getByStudentLevel(eq(L1))).thenReturn(List.of(mgt1Course()));

    var result = subject.getLeveledYearlyResultByStudentId(L1, STUDENT1_ID);

    assertEquals(NOT_STARTED, result.getStatus());
    assertNull(result.getWeightedAverage());
    assertEquals(1, result.getCourseResults().size());
    assertEquals(0, result.getObtainedCredits().doubleValue());
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
    YearlyResult student1YearlyResult = subject.getLeveledYearlyResultByStudentId(L1, STUDENT1_ID);

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

  private static Exam mockExam(
      String id, int coefficientNumerator, int coefficientDenominator, CourseAssignment ca) {
    return Exam.builder()
        .id(id)
        .coefficientNumerator(coefficientNumerator)
        .coefficientDenominator(coefficientDenominator)
        .courseAssignment(ca)
        .build();
  }

  private static User mockUser(String id) {
    return User.builder().id(id).build();
  }

  private static Promotion mockPromotion() {
    return Promotion.builder().ref("prom1").name("Promotion de test").startDatetime(now()).build();
  }

  private static final String GROUP_ID = "test-grp";
  private static final String GROUP_NAME = "Groupe test";
  private static final String GROUP_REF = "GRP_TST";

  private static Group mockGroup(Promotion promotion) {
    return Group.builder()
        .id(GROUP_ID)
        .name(GROUP_NAME)
        .ref(GROUP_REF)
        .promotion(promotion)
        .build();
  }

  private static Grade mockGrade(Exam exam, double score) {
    return Grade.builder().score(score).exam(exam).build();
  }

  private static Course mockCourse(
      String id, String code, String name, int credits, CourseAssignment ca, StudentLevel level) {
    var course =
        Course.builder()
            .id(id)
            .courseAssignments(List.of(ca))
            .code(code)
            .name(name)
            .credits(credits)
            .studentLevel(level)
            .build();
    ca.setCourse(course);
    return course;
  }

  private static CourseAssignment mockCourseAssignment(String id, User teacher) {
    return CourseAssignment.builder().id(id).mainTeacher(teacher).groups(List.of(group())).build();
  }
}
