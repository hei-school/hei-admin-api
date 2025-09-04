package school.hei.haapi.service;

import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.NOT_STARTED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.AVAILABLE;
import static school.hei.haapi.endpoint.rest.model.YearlyResultGenerationStatus.GENERATING;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.YearlyResultTranscriptGeneration;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.endpoint.rest.model.YearlyResultGenerationTranscript;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.YearlyResultGenerationRequest;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.repository.YearlyResultGenerationRequestRepository;
import school.hei.haapi.repository.dao.CourseAssignmentDao;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.event.YearlyResultTranscriptGenerationService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;

class GradeResultServiceTest {
  private static final GradeDao gradeDao = mock();
  private static final CourseAssignmentDao courseAssignmentDao = mock();
  private static final CourseService courseService = mock();
  private static final ExamService examService = mock();
  private static final UserService userService = mock();
  private static final BucketComponent bucketComponent = mock();
  private static final FileInfoService fileInfoService = mock();
  private static final EventProducer eventProducer = mock();
  private static final YearlyResultGenerationRequestRepository
      yearlyResultGenerationRequestRepository = mock();
  private static final YearlyResultGenerationService yearlyResultGenerationService =
      new YearlyResultGenerationService(
          new HtmlParser(),
          new PdfRenderer(),
          new Base64Converter(),
          yearlyResultGenerationRequestRepository,
          new ClassPathResourceResolver());
  private static final GradeResultService subject =
      new GradeResultService(
          new CourseResultService(
              courseService, gradeDao, new CourseMapper(), examService, userService),
          yearlyResultGenerationService,
          bucketComponent,
          userService,
          fileInfoService,
          eventProducer);
  private static final YearlyResultTranscriptGenerationService
      yearlyResultTranscriptGenerationService =
          new YearlyResultTranscriptGenerationService(subject);

  private static final User student2 = mockUser("bad student");
  private static final User student3 = mockUser("student with missing grade");

  private static final User student1 = mock();
  private static final Promotion promotion = mockPromotion();

  private static final Group group = mockGroup(promotion);

  private static final Exam mgt1Exam = mockExam("mgt1 exam", 1, 1);
  private static final Exam prog1Exam = mockExam("prog1 exam", 1, 1);
  private static final Exam donnees1Exam = mockExam("donnees1 exam", 1, 1);
  private static final Exam web1Exam = mockExam("web1 exam", 1, 1);
  private static final Exam sys1Exam = mockExam("sys1 exam", 1, 1);
  private static final Exam lv1Exam = mockExam("lv1 exam", 1, 1);
  private static final Exam badExam = mockExam("bad exam", 0, 1);

  private static final Grade student1Mgt1Grade = mockGrade(mgt1Exam, 17.75);
  private static final Grade student1Prog1Grade = mockGrade(prog1Exam, 13.59);
  private static final Grade student1Donnees1Grade = mockGrade(donnees1Exam, 15.4375);
  private static final Grade student1Web1Grade = mockGrade(web1Exam, 18.75);
  private static final Grade student1Sys1Grade = mockGrade(sys1Exam, 13.);
  private static final Grade student1Lv1Grade = mockGrade(lv1Exam, 13.91);
  private static final Grade student2Mgt1Grade = mockGrade(mgt1Exam, 14.75);
  private static final Grade student2Prog1Grade = mockGrade(prog1Exam, 4.46);
  private static final Grade student2Donnees1Grade = mockGrade(donnees1Exam, 6.);
  private static final Grade student2Web1Grade = mockGrade(web1Exam, 7.5);
  private static final Grade student2Sys1Grade = mockGrade(sys1Exam, 10.00);
  private static final Grade student2Lv1Grade = mockGrade(lv1Exam, 3.91);
  private static final Grade student3Mgt1Grade = mockGrade(mgt1Exam, 17.75);
  private static final Grade student3Sys1Grade = mockGrade(sys1Exam, 13.);
  private static final Grade student3Prog1Grade = mockGrade(prog1Exam, 13.59);
  private static final Grade student3Donnees1Grade = mockGrade(donnees1Exam, 15.4375);
  private static final Grade student3Web1Grade = mockGrade(web1Exam, 18.75);
  private static final Grade student3GradeForBadExam = mockGrade(badExam, 13.59);

  private static final Course mgt1Course = mockCourse("mgt1", "MGT1", "Mgt 1", 4);
  private static final Course prog1Course = mockCourse("prog1", "PROG1", "Programation 1", 6);
  private static final Course donne1Course = mockCourse("donne1", "DONNES1", "Donnees 1", 4);
  private static final Course web1Course = mockCourse("web1", "WEB1", "Web 1", 6);
  private static final Course sys1Course = mockCourse("sys1", "SYS1", "Systeme et reseau 1", 6);
  private static final Course lv1Course = mockCourse("lv1", "LV1", "Langue vivante 1", 4);
  private static final Course badCourse = mockCourse("bad course", "bad course", "Bad course", 0);

  private static final CourseAssignment mgt1CourseAssignment =
      mockCourseAssignment("mgt1 courseAssignment", mgt1Course);
  private static final CourseAssignment prog1CourseAssignment =
      mockCourseAssignment("prog1 courseAssignment", prog1Course);
  private static final CourseAssignment donnee1CourseAssignment =
      mockCourseAssignment("donnee1 courseAssignment", donne1Course);
  private static final CourseAssignment web1CourseAssignment =
      mockCourseAssignment("web1 courseAssignment", web1Course);
  private static final CourseAssignment sys1CourseAssignment =
      mockCourseAssignment("sys1 courseAssignment", sys1Course);
  private static final CourseAssignment lv1CourseAssignment =
      mockCourseAssignment("lv1 courseAssignment", lv1Course);
  private static final CourseAssignment badCourseAssignment =
      mockCourseAssignment("lv1 courseAssignment", badCourse);

  @BeforeEach
  void setUp() {
    // Mock student1 grades
    when(student1.getId()).thenReturn("id");
    when(student1.getFirstName()).thenReturn("Student");
    when(student1.getLastName()).thenReturn("One");
    when(student1.getRef()).thenReturn("STD1");
    when(student1.getSpecializationFieldString()).thenReturn("Transformation Numérique");
    when(student1.findCurrentGroup()).thenReturn(Optional.of(group));

    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Mgt1Grade));
    when(gradeDao.getStudentGradesByCourseId(prog1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Prog1Grade));
    when(gradeDao.getStudentGradesByCourseId(donne1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Donnees1Grade));
    when(gradeDao.getStudentGradesByCourseId(web1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Web1Grade));
    when(gradeDao.getStudentGradesByCourseId(sys1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Sys1Grade));
    when(gradeDao.getStudentGradesByCourseId(lv1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Lv1Grade));

    // Mock student2 grades
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student2.getId()))
        .thenReturn(List.of(student2Mgt1Grade));
    when(gradeDao.getStudentGradesByCourseId(prog1Course.getId(), student2.getId()))
        .thenReturn(List.of(student2Prog1Grade));
    when(gradeDao.getStudentGradesByCourseId(donne1Course.getId(), student2.getId()))
        .thenReturn(List.of(student2Donnees1Grade));
    when(gradeDao.getStudentGradesByCourseId(web1Course.getId(), student2.getId()))
        .thenReturn(List.of(student2Web1Grade));
    when(gradeDao.getStudentGradesByCourseId(sys1Course.getId(), student2.getId()))
        .thenReturn(List.of(student2Sys1Grade));
    when(gradeDao.getStudentGradesByCourseId(lv1Course.getId(), student2.getId()))
        .thenReturn(List.of(student2Lv1Grade));

    // Mock student3 grades: LV1 is missing
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student3.getId()))
        .thenReturn(List.of(student3Mgt1Grade));
    when(gradeDao.getStudentGradesByCourseId(prog1Course.getId(), student3.getId()))
        .thenReturn(List.of(student3Prog1Grade));
    when(gradeDao.getStudentGradesByCourseId(donne1Course.getId(), student3.getId()))
        .thenReturn(List.of(student3Donnees1Grade));
    when(gradeDao.getStudentGradesByCourseId(web1Course.getId(), student3.getId()))
        .thenReturn(List.of(student3Web1Grade));
    when(gradeDao.getStudentGradesByCourseId(sys1Course.getId(), student3.getId()))
        .thenReturn(List.of(student3Sys1Grade));

    // Mock exam from course assignment
    when(examService.getExamsByCourseId(mgt1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(mgt1Exam));
    when(examService.getExamsByCourseId(prog1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(prog1Exam));
    when(examService.getExamsByCourseId(donnee1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(donnees1Exam));
    when(examService.getExamsByCourseId(web1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(web1Exam));
    when(examService.getExamsByCourseId(sys1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(sys1Exam));
    when(examService.getExamsByCourseId(lv1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(lv1Exam));

    // Mock course for student level L1
    when(courseService.getByStudentLevel(eq(L1)))
        .thenReturn(
            List.of(
                mgt1CourseAssignment.getCourse(),
                prog1CourseAssignment.getCourse(),
                donnee1CourseAssignment.getCourse(),
                web1CourseAssignment.getCourse(),
                sys1CourseAssignment.getCourse(),
                lv1CourseAssignment.getCourse()));

    when(userService.findById(anyString()))
        .thenAnswer(
            (Answer<User>) invocation -> User.builder().id(invocation.getArgument(0)).build());
  }

  @Test
  void correct_result_yearly_result_student1_L1_validate() throws CoursesCreditSumZero {
    var targetLevel = L1;

    YearlyResult result = subject.getLeveledYearlyResultByStudentId(targetLevel, student1.getId());

    assertEquals(targetLevel, result.getLevel());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(15.347666666666667, result.getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_yearly_result_student2_L1_invalidate() throws CoursesCreditSumZero {
    var targetLevel = L1;

    YearlyResult result = subject.getLeveledYearlyResultByStudentId(targetLevel, student2.getId());

    assertEquals(targetLevel, result.getLevel());
    assertEquals(10., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    assertEquals(7.68, result.getWeightedAverage().doubleValue());
    assertEquals(INVALIDATED, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_yearly_result_student3_L1_inProgress() throws CoursesCreditSumZero {
    var targetLevel = L1;

    YearlyResult result = subject.getLeveledYearlyResultByStudentId(targetLevel, student3.getId());

    assertEquals(targetLevel, result.getLevel());
    assertEquals(26., result.getObtainedCredits().doubleValue());
    assertEquals(6, result.getCourseResults().size());
    CourseResult lv1Result = result.getCourseResults().get(5);
    assertEquals(lv1Course.getId(), lv1Result.getCourse().getId());
    assertEquals(CourseResultStatus.IN_PROGRESS, lv1Result.getStatus());
    assertEquals(13.493, result.getWeightedAverage().doubleValue());
    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void generate_result_pdf_okay() throws CoursesCreditSumZero {
    var targetLevel = L1;
    YearlyResult result = subject.getLeveledYearlyResultByStudentId(targetLevel, student1.getId());
    File resultFile =
        yearlyResultGenerationService.generateYearlyResultTranscript(student1, result);
    assertTrue(resultFile.isFile());
  }

  @Test
  void correct_result_yearly_result_M2_empty_notStarted() {
    String studentId = student1.getId();

    StudentLevel expectedLevel = M2;
    YearlyResult yearlyResult = subject.getLeveledYearlyResultByStudentId(expectedLevel, studentId);

    assertEquals(expectedLevel, yearlyResult.getLevel());
    assertEquals(NOT_STARTED, yearlyResult.getStatus());
  }

  @Test
  void correct_result_result_summary_student1_validated() {
    ResultSummary result = subject.getStudentResultSummary(student1.getId());

    assertEquals(5, result.getYearlyResults().size());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(15.3476666666666667, result.getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_result_summary_student2_invalidated() {
    ResultSummary result = subject.getStudentResultSummary(student2.getId());

    assertEquals(5, result.getYearlyResults().size());
    assertEquals(10., result.getObtainedCredits().doubleValue());
    assertEquals(7.68, result.getWeightedAverage().doubleValue());
    assertEquals(INVALIDATED, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_result_summary_student3_in_progress() {
    ResultSummary result = subject.getStudentResultSummary(student3.getId());

    assertEquals(5, result.getYearlyResults().size());
    assertEquals(26., result.getObtainedCredits().doubleValue());
    assertEquals(13.493, result.getWeightedAverage().doubleValue());
    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void yearly_result_with_course_credits_sum_zero_ko() {
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Mgt1Grade));
    when(examService.getExamsByCourseId(badCourseAssignment.getCourse().getId()))
        .thenReturn(List.of(mgt1Exam));
    when(courseService.getByStudentLevel(eq(L1)))
        .thenReturn(List.of(badCourseAssignment.getCourse()));

    assertThrows(
        CoursesCreditSumZero.class,
        () -> subject.getLeveledYearlyResultByStudentId(L1, student1.getId()));
  }

  @Test
  void course_result_with_exams_coefficient_sum_zero_is_inProgress() {
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student1.getId()))
        .thenReturn(List.of(student3GradeForBadExam));
    when(examService.getExamsByCourseId(mgt1CourseAssignment.getCourse().getId()))
        .thenReturn(List.of(badExam));
    when(courseService.getByStudentLevel(eq(L1)))
        .thenReturn(List.of(mgt1CourseAssignment.getCourse()));

    var result = subject.getLeveledYearlyResultByStudentId(L1, student1.getId());

    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(ZERO, result.getWeightedAverage());
    assertEquals(1, result.getCourseResults().size());
    assertEquals(0, result.getObtainedCredits().doubleValue());
  }

  @Test
  void yearly_result_generation_should_return_available_transcript_when_file_exists()
      throws MalformedURLException {

    when(userService.findById(anyString())).thenReturn(student1);
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

    YearlyResultGenerationTranscript result =
        subject.getYearlyResultTranscript(student1.getId(), L1);
    assertEquals(AVAILABLE, result.getStatus());
    assertFalse(result.getLink().isEmpty());
  }

  @Test
  void yearly_result_generation_should_return_generating_status_when_file_is_missing() {
    when(userService.findById(anyString())).thenReturn(student1);
    when(yearlyResultGenerationService.findGenerationRequestByFileName(anyString()))
        .thenReturn(empty());

    YearlyResultGenerationTranscript result =
        subject.getYearlyResultTranscript(student1.getId(), L1);

    assertEquals(GENERATING, result.getStatus());
    assertNull(result.getLink());
  }

  @Test
  void yearly_result_generation_should_regenerate_when_generation_times_out() {

    when(userService.findById(anyString())).thenReturn(student1);
    when(yearlyResultGenerationService.findGenerationRequestByFileName(anyString()))
        .thenReturn(
            Optional.of(
                YearlyResultGenerationRequest.builder()
                    .status(GENERATING)
                    .datetime(now().minus(Duration.ofHours(1L)))
                    .build()));

    YearlyResultGenerationTranscript result =
        subject.getYearlyResultTranscript(student1.getId(), L1);
    assertEquals(GENERATING, result.getStatus());
    verify(eventProducer, only()).accept(anyList());
  }

  @Test
  void yearly_result_generation_should_return_bad_request_when_level_in_progress() {
    String studentId = student3.getId();

    String exceptionMessage =
        assertThrows(
                BadRequestException.class, () -> subject.getYearlyResultTranscript(studentId, L1))
            .getMessage();
    assertEquals(
        "Cannot generate transcript for this level. This level is not yet completed",
        exceptionMessage);
  }

  @Test
  void yearly_result_event_handler_ok() {
    String studentId = student1.getId();
    YearlyResult student1YearlyResult = subject.getLeveledYearlyResultByStudentId(L1, studentId);

    when(userService.findById(anyString())).thenReturn(student1);
    assertDoesNotThrow(
        () -> {
          yearlyResultTranscriptGenerationService.accept(
              YearlyResultTranscriptGeneration.builder()
                  .yearlyResult(student1YearlyResult)
                  .userId(studentId)
                  .build());
        });
  }

  private static Exam mockExam(String id, int coefficientNumerator, int coefficientDenominator) {
    return Exam.builder()
        .id(id)
        .coefficientNumerator(coefficientNumerator)
        .coefficientDenominator(coefficientDenominator)
        .build();
  }

  private static User mockUser(String id) {
    return User.builder().id(id).build();
  }

  private static Promotion mockPromotion() {
    return Promotion.builder().ref("prom1").name("Promotion de test").startDatetime(now()).build();
  }

  private static Group mockGroup(Promotion promotion) {
    return Group.builder().name("Groupe test").ref("GRP_TST").promotion(promotion).build();
  }

  private static Grade mockGrade(Exam exam, double score) {
    return Grade.builder().score(score).exam(exam).build();
  }

  private static Course mockCourse(String id, String code, String name, int credits) {
    return Course.builder().id(id).code(code).name(name).credits(credits).build();
  }

  private static CourseAssignment mockCourseAssignment(String id, Course course) {
    return CourseAssignment.builder().id(id).course(course).build();
  }
}
