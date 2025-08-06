package school.hei.haapi.service;

import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.M2;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseResult;
import school.hei.haapi.endpoint.rest.model.CourseResultStatus;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.CoursesCreditSumZero;
import school.hei.haapi.repository.dao.CourseAssignmentDao;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;

class GradeResultServiceTest {
  private final GradeDao gradeDao = mock();
  private final CourseAssignmentDao courseAssignmentDao = mock();
  private final ExamService examService = mock();
  private final GradeResultService subject =
      new GradeResultService(
          new CourseResultService(courseAssignmentDao, gradeDao, new CourseMapper(), examService));
  private final YearlyResultGenerationService yearlyResultGenerationService = new YearlyResultGenerationService(
      new HtmlParser(),
      new PdfRenderer(),
      new Base64Converter(),
      new ClassPathResourceResolver());

  private final User student2 = User.builder().id("bad student").build();
  private final User student3 = User.builder().id("student with missing grade").build();

  private final User student1 = mock();
  private final Promotion promotion = Promotion.builder()
      .ref("prom1")
      .name("Promotion de test")
      .build();
  private final Group group = Group.builder()
      .name("Groupe test")
      .ref("GRP_TST")
      .promotion(promotion)
      .build();
  private final Exam mgt1Exam = Exam.builder().id("mgt1 exam").coefficient(1).build();
  private final Exam prog1Exam = Exam.builder().id("prog1 exam").coefficient(1).build();
  private final Exam donnees1Exam = Exam.builder().id("donnees1 exam").coefficient(1).build();
  private final Exam web1Exam = Exam.builder().id("web1 exam").coefficient(1).build();
  private final Exam sys1Exam = Exam.builder().id("sys1 exam").coefficient(1).build();
  private final Exam lv1Exam = Exam.builder().id("lv1 exam").coefficient(1).build();
  private final Exam badExam = Exam.builder().id("bad exam").coefficient(0).build();

  private final Grade student1Mgt1Grade = Grade.builder().score(17.75).exam(mgt1Exam).build();
  private final Grade student1Prog1Grade = Grade.builder().score(13.59).exam(prog1Exam).build();
  private final Grade student1Donnees1Grade =
      Grade.builder().score(15.4375).exam(donnees1Exam).build();
  private final Grade student1Web1Grade = Grade.builder().score(18.75).exam(web1Exam).build();
  private final Grade student1Sys1Grade = Grade.builder().score(13.).exam(sys1Exam).build();
  private final Grade student1Lv1Grade = Grade.builder().score(13.91).exam(lv1Exam).build();
  private final Grade student2Mgt1Grade = Grade.builder().score(14.75).exam(mgt1Exam).build();
  private final Grade student2Prog1Grade = Grade.builder().score(4.46).exam(prog1Exam).build();
  private final Grade student2Donnees1Grade = Grade.builder().score(6.).exam(donnees1Exam).build();
  private final Grade student2Web1Grade = Grade.builder().score(7.5).exam(web1Exam).build();
  private final Grade student2Sys1Grade = Grade.builder().score(10.00).exam(sys1Exam).build();
  private final Grade student2Lv1Grade = Grade.builder().score(3.91).exam(lv1Exam).build();
  private final Grade student3Mgt1Grade = Grade.builder().score(17.75).exam(mgt1Exam).build();
  private final Grade student3Prog1Grade = Grade.builder().score(13.59).exam(prog1Exam).build();
  private final Grade student3Donnees1Grade =
      Grade.builder().score(15.4375).exam(donnees1Exam).build();
  private final Grade student3Web1Grade = Grade.builder().score(18.75).exam(web1Exam).build();
  private final Grade student3Sys1Grade = Grade.builder().score(13.).exam(sys1Exam).build();
  private final Grade student3GradeForBadExam = Grade.builder().score(13.59).exam(badExam).build();

  private final Course mgt1Course = Course.builder().id("mgt1").credits(4).build();
  private final Course prog1Course = Course.builder().id("prog1").credits(6).build();
  private final Course donne1Course = Course.builder().id("donne1").credits(4).build();
  private final Course web1Course = Course.builder().id("web1").credits(6).build();
  private final Course sys1Course = Course.builder().id("sys1").credits(6).build();
  private final Course lv1Course = Course.builder().id("lv1").credits(4).build();
  private final Course badCourse = Course.builder().id("bad course").credits(0).build();

  private final CourseAssignment mgt1CourseAssignment =
      CourseAssignment.builder().id("mgt1 courseAssignment").course(mgt1Course).build();
  private final CourseAssignment prog1CourseAssignment =
      CourseAssignment.builder().id("prog1 courseAssignment").course(prog1Course).build();
  private final CourseAssignment donnee1CourseAssignment =
      CourseAssignment.builder().id("donnee1 courseAssignment").course(donne1Course).build();
  private final CourseAssignment web1CourseAssignment =
      CourseAssignment.builder().id("web1 courseAssignment").course(web1Course).build();
  private final CourseAssignment sys1CourseAssignment =
      CourseAssignment.builder().id("sys1 courseAssignment").course(sys1Course).build();
  private final CourseAssignment lv1CourseAssignment =
      CourseAssignment.builder().id("lv1 courseAssignment").course(lv1Course).build();
  private final CourseAssignment badCourseAssignment =
      CourseAssignment.builder().id("lv1 courseAssignment").course(badCourse).build();

  @BeforeEach
  void setUp() {
    // Mock student1 grades
    when(student1.getId()).thenReturn("id");
    when(student1.getFirstName()).thenReturn("Student");
    when(student1.getLastName()).thenReturn("One");
    when(student1.getRef()).thenReturn("STD1");
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
    when(examService.getExamsByCourseAssignmentId(mgt1CourseAssignment.getId()))
        .thenReturn(List.of(mgt1Exam));
    when(examService.getExamsByCourseAssignmentId(prog1CourseAssignment.getId()))
        .thenReturn(List.of(prog1Exam));
    when(examService.getExamsByCourseAssignmentId(donnee1CourseAssignment.getId()))
        .thenReturn(List.of(donnees1Exam));
    when(examService.getExamsByCourseAssignmentId(web1CourseAssignment.getId()))
        .thenReturn(List.of(web1Exam));
    when(examService.getExamsByCourseAssignmentId(sys1CourseAssignment.getId()))
        .thenReturn(List.of(sys1Exam));
    when(examService.getExamsByCourseAssignmentId(lv1CourseAssignment.getId()))
        .thenReturn(List.of(lv1Exam));

    // Mock course assignment from course level
    when(courseAssignmentDao.findByCriteria(any(), any(), eq(L1), any()))
        .thenReturn(
            List.of(
                mgt1CourseAssignment,
                prog1CourseAssignment,
                donnee1CourseAssignment,
                web1CourseAssignment,
                sys1CourseAssignment,
                lv1CourseAssignment));
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
  void generate_result_pdf_okay() throws CourseCreditsSumZero {
    var targetLevel = L1;

    YearlyResult result =
        gradeResultService.getLeveledYearlyResultByStudentId(targetLevel, student1.getId());

    yearlyResultGenerationService.generateYealyResultFile(student1, result).getAbsolutePath();
  }

  @Test
  void correct_result_yearly_result_M2_empty_ko() {
    String studentId = student1.getId();

    assertThrows(
        CoursesCreditSumZero.class, () -> subject.getLeveledYearlyResultByStudentId(M2, studentId));
  }

  @Test
  void correct_result_result_summary_student1_validated() {
    ResultSummary result = subject.getStudentResultSummary(student1.getId());

    assertEquals(1, result.getYearlyResults().size());
    assertEquals(30., result.getObtainedCredits().doubleValue());
    assertEquals(15.3476666666666667, result.getWeightedAverage().doubleValue());
    assertEquals(VALIDATED, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_result_summary_student2_invalidated() {
    ResultSummary result = subject.getStudentResultSummary(student2.getId());

    assertEquals(1, result.getYearlyResults().size());
    assertEquals(10., result.getObtainedCredits().doubleValue());
    assertEquals(7.68, result.getWeightedAverage().doubleValue());
    assertEquals(INVALIDATED, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void correct_result_result_summary_student3_in_progress() {
    ResultSummary result = subject.getStudentResultSummary(student3.getId());

    assertEquals(1, result.getYearlyResults().size());
    assertEquals(26., result.getObtainedCredits().doubleValue());
    assertEquals(13.493, result.getWeightedAverage().doubleValue());
    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(30., result.getTotalCredits().doubleValue());
  }

  @Test
  void yearly_result_with_course_credits_sum_zero_ko() {
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student1.getId()))
        .thenReturn(List.of(student1Mgt1Grade));
    when(examService.getExamsByCourseAssignmentId(badCourseAssignment.getId()))
        .thenReturn(List.of(mgt1Exam));
    when(courseAssignmentDao.findByCriteria(any(), any(), eq(L1), any()))
        .thenReturn(List.of(badCourseAssignment));

    assertThrows(
        CoursesCreditSumZero.class,
        () -> subject.getLeveledYearlyResultByStudentId(L1, student1.getId()));
  }

  @Test
  void course_result_with_exams_coefficient_sum_zero_is_inProgress() {
    when(gradeDao.getStudentGradesByCourseId(mgt1Course.getId(), student1.getId()))
        .thenReturn(List.of(student3GradeForBadExam));
    when(examService.getExamsByCourseAssignmentId(mgt1CourseAssignment.getId()))
        .thenReturn(List.of(badExam));
    when(courseAssignmentDao.findByCriteria(any(), any(), eq(L1), any()))
        .thenReturn(List.of(mgt1CourseAssignment));

    var result = subject.getLeveledYearlyResultByStudentId(L1, student1.getId());

    assertEquals(IN_PROGRESS, result.getStatus());
    assertEquals(ZERO, result.getWeightedAverage());
    assertEquals(1, result.getCourseResults().size());
    assertEquals(0, result.getObtainedCredits().doubleValue());
  }
}
