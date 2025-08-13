package school.hei.haapi.endpoint.rest.controller;

import java.math.MathContext;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateGrade;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.GradeResultService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final GradeValidator validator;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;
  private final GradeResultService gradeResultService;

  // todo: to review all class
  @GetMapping("/students/{student_id}/grades")
  public List<Grade> getGradesByStudentId(@PathVariable("student_id") String studentId) {
    return gradeService.getGradesByStudentId(studentId).stream().map(gradeMapper::toRest).toList();
  }

  @PutMapping(value = "/exams/{exam_id}/students/{student_id}/grade")
  public Grade crupdateParticipantGrade(
      @PathVariable("exam_id") String examId,
      @PathVariable("student_id") String studentId,
      @RequestBody CrupdateGrade grade) {
    validator.accept(grade);
    var toSave = gradeMapper.toDomain(grade, examId, userService.findById(studentId).getRef());
    return gradeMapper.toRest(gradeService.crupdateParticipantGrade(List.of(toSave)).getFirst());
  }

  @GetMapping(value = "/exams/{exam_id}/grades")
  public List<StudentGrade> getStudentGradesForExam(
      @PathVariable(value = "exam_id") String examId,
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize) {
    return gradeService.getParticipantsGradeForExam(examId, page, pageSize).stream()
        .map(gradeMapper::toRestStudentGrade)
        .toList();
  }

  @PutMapping(value = "/exams/{exam_id}/grades")
  public List<StudentGrade> updateParticipantsGradeForExam(
      @PathVariable("exam_id") String examId, @RequestBody List<UpdateGrade> grades) {
    return gradeService
        .crupdateParticipantGrade(
            grades.stream()
                .map(grade -> gradeMapper.toDomain(grade.getGrade(), examId, grade.getStudentRef()))
                .toList())
        .stream()
        .map(gradeMapper::toRestStudentGrade)
        .toList();
  }

  @GetMapping(value = "/exams/{exam_id}/grade/stats")
  public ExamGradeStats getExamGradeStats(@PathVariable(value = "exam_id") String examsId) {
    return gradeService.getExamGradeStats(examsId);
  }

  @GetMapping("/students/{student_id}/yearly_results/{student_level}")
  public YearlyResult getYearlyResult(
      @PathVariable("student_id") String studentId,
      @PathVariable("student_level") StudentLevel studentLevel) {
    return gradeResultService.getLeveledYearlyResultByStudentId(studentLevel, studentId);
  }

  @GetMapping("/students/{student_id}/results_summary")
  public ResultSummary getResultSummary(@PathVariable("student_id") String studentId) {
    var studentResultSummary = gradeResultService.getStudentResultSummary(studentId);
    return studentResultSummary.weightedAverage(
        studentResultSummary.getWeightedAverage().round(new MathContext(4)));
  }

  @GetMapping("/students/{student_id}/courses/{course_id}/grades")
  public List<Grade> getCourseGrades(
      @PathVariable("student_id") String studentId, @PathVariable("course_id") String courseId) {
    return gradeService.getGradesByStudentAndCourseId(studentId, courseId).stream()
        .map(gradeMapper::toRest)
        .toList();
  }
}
