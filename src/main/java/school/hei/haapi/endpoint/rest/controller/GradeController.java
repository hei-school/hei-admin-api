package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.service.GradeService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final UserService userService;
  private final GradeService gradeService;

  @GetMapping("/students/{student_id}/grades")
  public List<Grade> getAllGradesOfStudent(@PathVariable("student_id") String studentId) {
    User student = userService.getById(studentId);
    return gradeService.getGradeByUser(student);
  }
}
