package school.hei.haapi.endpoint.rest.controller;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.StudentCourseMapper;
import school.hei.haapi.endpoint.rest.model.StudentCourseExam;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.StudentCourseRepository;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final StudentCourseRepository studentCourseRepository;
  private final StudentCourseMapper studentCourseMapper;

  @GetMapping("/students/{student_id}/grades")
  public List<StudentCourseExam> getAllGradesOfStudent(@PathVariable("student_id") String studentId) {
    User student = userService.getById(studentId);
    return studentCourseMapper.toRestStudentCourseExams(studentCourseRepository.findByUserId(student));
  }
}
