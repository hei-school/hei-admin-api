package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.MonitoringStudentRepository;

@Service
@AllArgsConstructor
public class MonitoringStudentService {
  private MonitoringStudentRepository monitoringStudentRepository;

  @Transactional
  public List<User> linkMonitorFollowingStudents(String monitorId, List<User> studentsToFollow) {
    List<String> studentIds =
        studentsToFollow.stream().map(User::getId).collect(Collectors.toList());
    for (String studentId : studentIds) {
      monitoringStudentRepository.saveMonitorFollowingStudents(monitorId, studentId);
    }
    return monitoringStudentRepository.findAllById(studentIds);
  }

  public List<User> getMonitorsByStudentId(String studentId) {
    return monitoringStudentRepository.findAllMonitorsByStudentId(studentId);
  }

  public List<User> getStudentsByMonitorId(
      String monitorId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));

    return monitoringStudentRepository.findAllStudentsByMonitorId(monitorId, pageable);
  }
}
