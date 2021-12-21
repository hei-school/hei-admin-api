package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Student;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.StudentRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final StudentRepository studentRepository;

  public User getById(String userId) {
    return userRepository.getById(userId);
  }

  public User getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  public Student getStudentByUserId(String userId) {
    return studentRepository.getByUserId(userId);
  }
  public Student getStudentById(String studentId) {
    return studentRepository.getById(studentId);
  }
}
