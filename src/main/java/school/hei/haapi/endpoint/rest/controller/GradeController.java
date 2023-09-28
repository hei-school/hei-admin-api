package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.AwardedCourseExam;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.AwardedCourseRepository;
import school.hei.haapi.service.AwardedCourseService;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final AwardedCourseService awardedCourseService;
  private final AwardedCourseMapper awardedCourseMapper;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;
  private final ExamService examService;

  @GetMapping("/students/{student_id}/grades")
  public List<AwardedCourseExam> getAllGradesOfStudent(
      @PathVariable("student_id") String studentId) {
    List<AwardedCourse> awardedCourses = awardedCourseService.getByStudentId(studentId);
    User student = userService.getById(studentId);
    return awardedCourseMapper.toRestAwardedCourseExams(awardedCourses, student);
  }

  @GetMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams/{exam_id}/grades")
  public ExamDetail getExamGrades(
          @PathVariable("group_id") String groupId,
          @PathVariable("awarded_course_id") String awardedCourseId,
          @PathVariable("exam_id") String examId) {
    List<Grade> grades = gradeService.getAllGradesByExamId(examId);
    Exam exam = examService.getExamsByIdAndGroupIdAndAwardedCourseId(examId, awardedCourseId, groupId);
    return gradeMapper.toRestExamDetail(exam, grades);
  }
//TODO: change that if null
  @GetMapping(value = "/groups/{group_id}/awarded_courses/{awarded_course_id}/exams/{exam_id}/students/{student_id}/grade")
  public StudentGrade getGradeOfStudentInOneExam(
          @PathVariable("group_id") String groupId,
          @PathVariable("awarded_course_id") String awardedCourseId,
          @PathVariable("exam_id") String examId,
          @PathVariable("student_id") String studentId
  ) {
    Grade grade = gradeService.getGradeByExamIdAndStudentId(examId, studentId, awardedCourseId, groupId);
    return gradeMapper.toRestStudentGrade(grade);
  }
}
