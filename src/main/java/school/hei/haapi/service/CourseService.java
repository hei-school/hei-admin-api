package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.model.CourseFollowedRest;
import school.hei.haapi.repository.CourseFollowedRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseFollowedRepository courseFollowedRepository;
  private final CourseMapper courseMapper;
  public List<CourseFollowed> getCourseFollowedByOneStudent(String studentId,
                                                            CourseStatus status) {
    return courseFollowedRepository.findAllByStudentIdAndStatus(studentId, status);
  }

  public List<CourseFollowed> updateStudentCourseLink(List<CourseFollowedRest> studentCourseToUpdate, String studentId){

    List<CourseFollowed> toPersist = new ArrayList<>();

    for ( CourseFollowedRest courseFollowedRest : studentCourseToUpdate ){
      toPersist.add(courseMapper.toRest(courseFollowedRest, studentId));
    }
    return courseFollowedRepository.saveAll(toPersist);
  }

}
