package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.repository.CourseFollowedRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseFollowedRepository courseFollowedRepository;

  public List<CourseFollowed> getCourseFollowedByOneStudent(String studentId,
                                                            CourseStatus status) {
    return courseFollowedRepository.findAllByStudentIdAndStatus(studentId, status);
  }
}
