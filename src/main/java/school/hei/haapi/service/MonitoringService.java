package school.hei.haapi.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class MonitoringService {
  private final UserRepository userRepository;

  @Transactional
  public List<User> linkMonitorFollowingStudents(String monitorId, List<User> studentsToFollow) {
    List<String> studentIds =
        studentsToFollow.stream().map(User::getId).collect(Collectors.toList());
    for (String studentId : studentIds) {
      userRepository.saveMonitorFollowingStudents(monitorId, studentId);
    }
    return userRepository.findAllById(studentIds);
  }
}
