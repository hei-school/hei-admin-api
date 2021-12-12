package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;

@Component
public class UserMapper {

  public Student toRestStudent(User user) {
    Student student = new Student();
    student.setId(user.getId());
    student.setFirstName(user.getFirstName());
    student.setLastName(user.getLastName());
    student.setEmail(user.getEmail());
    return student;
  }

  public Teacher toRestTeacher(User user) {
    Teacher teacher = new Teacher();
    teacher.setId(user.getId());
    teacher.setFirstName(user.getFirstName());
    teacher.setLastName(user.getLastName());
    teacher.setEmail(user.getEmail());
    return teacher;
  }
}
