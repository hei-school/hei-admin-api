package school.hei.haapi.endpoint.rest.controller;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.endpoint.rest.model.Grade;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.endpoint.rest.model.StudentExamGradeImportValidationResult;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.endpoint.rest.model.YearlyResultGenerationTranscript;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.endpoint.rest.validator.UpdateGradeValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.ExamParticipantService;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeResultService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.MultipartFileConverter;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final UpdateGradeValidator updateGradeValidator;
  private final GradeValidator gradeValidator;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;
  private final GradeResultService gradeResultService;
  private final ExamService examService;
  private final ExamParticipantService examParticipantService;
  private final MultipartFileConverter fileConverter;

  // todo: to review all class
  @GetMapping("/students/{student_id}/grades")
  public List<Grade> getGradesByStudentId(@PathVariable("student_id") String studentId) {
    return gradeService.getGradesByStudentId(studentId).stream().map(gradeMapper::toRest).toList();
  }

  @PostMapping(value = "/exams/{exam_id}/students/{student_id}/grade")
  public Grade createParticipantGrade(
      @PathVariable("exam_id") String examId,
      @PathVariable("student_id") String studentId,
      @RequestBody CreateGrade grade) {
    gradeValidator.accept(grade);
    var toSave = gradeMapper.toDomain(grade, examId, studentId);
    return gradeMapper.toRest(gradeService.createParticipantGrade(List.of(toSave)).getFirst());
  }

  @PostMapping(value = "/exams/{exam_id}/students/{student_id}/grade/update")
  public Grade updateParticipantGrade(
      @PathVariable("exam_id") String examId,
      @PathVariable("student_id") String studentId,
      @RequestBody UpdateGrade grade) {
    updateGradeValidator.accept(grade);
    var toUpdate = gradeMapper.toDomain(grade, examId, userService.getById(studentId).getRef());
    return gradeMapper.toRest(gradeService.updateParticipantGrade(List.of(toUpdate)).getFirst());
  }

  @GetMapping(value = "/exams/{exam_id}/grades")
  public List<StudentGrade> getStudentGradesForExam(
      @PathVariable(value = "exam_id") String examId,
      @RequestParam(defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize,
      @RequestParam(value = "student_ref", required = false) String studentRef) {
    return examParticipantService.getExamParticipantsGrade(examId, page, pageSize, studentRef);
  }

  @PostMapping(value = "/exams/{exam_id}/grades")
  public List<StudentGrade> createParticipantsGradeForExam(
      @PathVariable("exam_id") String examId, @RequestBody List<CreateGrade> grades) {
    return gradeService
        .createParticipantGrade(
            grades.stream()
                .map(grade -> gradeMapper.toDomain(grade, examId, grade.getStudentId()))
                .toList())
        .stream()
        .map(gradeMapper::toRestStudentGrade)
        .toList();
  }

  @PostMapping(value = "/exams/{exam_id}/grades/import")
  public StudentExamGradeImportValidationResult importStudentsExamGrade(
      @RequestParam("due_datetime") Instant dueDatetime,
      @RequestPart("file_to_upload") MultipartFile fileToUpload) {
    return gradeService.initStudentExamGradeImportFromXlsx(
        fileConverter.apply(fileToUpload), dueDatetime);
  }

  @PostMapping(value = "/exams/{exam_id}/grades/update")
  public List<StudentGrade> correctParticipantsGradeForExam(
      @PathVariable("exam_id") String examId, @RequestBody List<UpdateGrade> grades) {
    return gradeService
        .updateParticipantGrade(
            grades.stream()
                .map(grade -> gradeMapper.toDomain(grade, examId, grade.getStudentRef()))
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
    return gradeResultService.getStudentResultSummary(studentId);
  }

  @GetMapping("/students/{student_id}/courses/{course_id}/grades")
  public List<Grade> getCourseGrades(
      @PathVariable("student_id") String studentId, @PathVariable("course_id") String courseId) {
    return gradeService.getGradesByStudentAndCourseId(studentId, courseId).stream()
        .map(gradeMapper::toRest)
        .toList();
  }

  @GetMapping("/students/{student_id}/yearly_results/{student_level}/transcript")
  public YearlyResultGenerationTranscript getYearlyResultTranscript(
      @PathVariable("student_id") String studentId,
      @PathVariable("student_level") StudentLevel studentLevel) {
    return gradeResultService.getYearlyResultTranscript(studentId, studentLevel);
  }

  @GetMapping("/exams/{exam_id}/students/{student_id}/grade")
  public Grade getParticipantGrade(
      @PathVariable("exam_id") String examId, @PathVariable("student_id") String studentId) {
    return gradeMapper.toRest(gradeService.getGradeByExamIdAndStudentId(examId, studentId));
  }
}
