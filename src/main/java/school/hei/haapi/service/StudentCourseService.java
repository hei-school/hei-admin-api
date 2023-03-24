package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.controller.response.UpdateStudentCourseStatusResponse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.StudentCourseRepository;

@Service
@AllArgsConstructor
public class StudentCourseService {

  private final StudentCourseRepository studentCourseRepository;
  private final CourseService courseService;
  private final UserService userService;

  public List<Course> updateCoursesStatuses(String studentId,
                                            List<UpdateStudentCourseStatusResponse> updateStudentCourses) {
    List<StudentCourse> oneStudentCourse = new ArrayList<>();
    oneStudentCourse.add(studentCourseRepository.getById(studentId));
    if (oneStudentCourse.size() > 0) {
      List<Course> updatedCourses = new ArrayList<>();
      for (UpdateStudentCourseStatusResponse course : updateStudentCourses) {
        StudentCourse studentCourse =
            studentCourseRepository.findByCourse_IdAndStudent_Id(course.getCourse_id(), studentId);
        if (studentCourse == null) {
          studentCourse = StudentCourse
              .builder()
              .student(userService.getById(studentId))
              .course(courseService.getById(course.getCourse_id()))
              .build();
        }
        studentCourse.setStatus(course.getStatus());
        studentCourseRepository.save(studentCourse);
        updatedCourses.add(courseService.getById(course.getCourse_id()));
      }
      return updatedCourses;
    } else {
      throw new NotFoundException("Student# " + studentId + " not found");
    }
  }
}
