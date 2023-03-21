package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseTemplate;
import school.hei.haapi.model.Student;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
@Component
@AllArgsConstructor
public class CourseMapper {
  private final CourseRepository repository;
  private final StudentRepository studentRepository;
  public Student toRest( CourseTemplate courseTemplate){
      return Student.builder()
              .courseStatus(courseTemplate.getStatus())
              .build();
  }

  public List<Course> toList(String id ,List<CourseTemplate> courseTemplateList){
      List<Student> students = new ArrayList<>();
      List<Course> courses = new ArrayList<>();
      Student student = studentRepository.getById(id);

    for(CourseTemplate courseTemplate : courseTemplateList){
        Course course = repository.getById(courseTemplate.getCourse_id());

        student.setCourse(Course.builder()
                        .main_teacher(course.getMain_teacher())
                        .credits(course.getCredits())
                        .name(course.getName())
                        .code(course.getCode())
                        .total_hours(course.getTotal_hours())
                        .id(course.getId())
                .build());
        student.setCourseStatus(courseTemplate.getStatus());
    }
    students.add(student);
    for(Student student1 : students){
      Course course = repository.getById(student1.getCourse().getId());
      courses.add(course);
    }
    return courses;
  }
}
