package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.AwardedCourseExam;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.ExamValidator;
import school.hei.haapi.service.AwardedCourseService;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final AwardedCourseService awardedCourseService;
  private final AwardedCourseMapper awardedCourseMapper;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;
  private final ExamService examService;
  private final ExamValidator examValidator;

  @PostMapping("/groups/{group_id}/awarded_courses/{awarded_course_id}/exams/{exam_id}/grades")
  public List<ExamDetail> createStudentExamGrade(@PathVariable(name = "group_id") String groupId,
                                                 @PathVariable(name = "awarded_course_id") String awardedCourseId,
                                                 @PathVariable(name = "exam_id") String examId,
                                                 @RequestBody List<CreateGrade> gradesToCreate) {
    List<Grade> grades = gradesToCreate.stream()
            .map(gradeMapper::toDomain)
            .collect(toUnmodifiableList());
    Exam correspondingExam = examService.findById(examId);
    List<Grade> savedGrades = gradeService.saveAll(grades);

    return gradeMapper.toRestExamDetail(correspondingExam, savedGrades);
  }

  @GetMapping("/students/{student_id}/grades")
  public List<AwardedCourseExam> getAllGradesOfStudent(
      @PathVariable("student_id") String studentId) {
    List<AwardedCourse> awardedCourses = awardedCourseService.getByStudentId(studentId);
    User student = userService.findById(studentId);
    return awardedCourseMapper.toRest(awardedCourses, student);
  }

  @GetMapping(
      value = "/groups/{group_id}/awarded_courses/" + "{awarded_course_id}/exams/{exam_id}/grades")
  public ExamDetail getExamGrades(
      @PathVariable("group_id") String groupId,
      @PathVariable("awarded_course_id") String awardedCourseId,
      @PathVariable("exam_id") String examId) {
    List<Grade> grades =
        examService
            .getExamsByIdAndGroupIdAndAwardedCourseId(examId, awardedCourseId, groupId)
            .getGrades();
    Exam exam =
        examService.getExamsByIdAndGroupIdAndAwardedCourseId(examId, awardedCourseId, groupId);
    return gradeMapper.toRestExamDetail(exam, grades);
  }

  @GetMapping(
      value =
          "/groups/{group_id}/awarded_courses/"
              + "{awarded_course_id}/exams/{exam_id}/students/{student_id}/grade")
  public StudentGrade getGradeOfStudentInOneExam(
      @PathVariable("group_id") String groupId,
      @PathVariable("awarded_course_id") String awardedCourseId,
      @PathVariable("exam_id") String examId,
      @PathVariable("student_id") String studentId) {
    Grade grade = gradeService.getGradeByExamIdAndStudentId(examId, studentId);
    return gradeMapper.toRestStudentGrade(grade);
  }
}
