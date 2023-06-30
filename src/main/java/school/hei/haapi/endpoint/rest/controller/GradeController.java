package school.hei.haapi.endpoint.rest.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;

  @GetMapping("/students/{student_id}/grades")//"/students/{student_id}/grades"
  public List<StudentGrade> getAllGradesOfStudent(@PathVariable("student_id") String studentId) {
    User student = userService.getById(studentId);
    List<Grade> grades = gradeService.getGradeByUser(student);
    List<StudentGrade> studentGrades = new ArrayList<>();
    for(Grade grade : grades){
      StudentGrade studentGrade = gradeMapper.toRestStudentGrade(grade);
      studentGrades.add(studentGrade);
    }
    return studentGrades;
  }
}
