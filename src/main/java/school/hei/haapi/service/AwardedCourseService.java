package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseDirection;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CreateAwardedCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.AwardedCourseValidator;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.AwardedCourseRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.CourseDao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.endpoint.rest.model.CourseStatus.LINKED;

@Service
@AllArgsConstructor
public class AwardedCourseService {
  private final CourseRepository courseRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final AwardedCourseRepository awardedCourseRepository;
  private final AwardedCourseMapper awardedCourseMapper;
  private final AwardedCourseValidator awardedCourseValidator;

  public List<AwardedCourse> getByCourseId(String courseId) {
    return courseRepository.getCourseById(courseId).getAwardedCourse();
  }

  public List<AwardedCourse> getByStudentId(String userId) {
    User student = userRepository.getById(userId);
    List<Group> groups = student.getGroupFlows().stream().map(groupFlow -> groupFlow.getGroup()).distinct().collect(Collectors.toList());
    return awardedCourseMapper.getAwardedCoursesDomainByGroups(groups);
  }

  public List<AwardedCourse> getByGroupId(String groupId) {
    return groupRepository.getById(groupId).getAwardedCourse();
  }

  public AwardedCourse getById(String id, String groupId) {
    return awardedCourseRepository.getByIdAndGroupId(id, groupId);
  }

  public AwardedCourse createOrUpdateAwardedCourse(CreateAwardedCourse createAwardedCourse) {
    Group group = groupRepository.getById(createAwardedCourse.getGroupId());
    Course course = courseRepository.getById(createAwardedCourse.getCourseId());
    User teacher = userRepository.getById(createAwardedCourse.getMainTeacherId());
    AwardedCourse awardedCourse = awardedCourseRepository.save(AwardedCourse.builder().course(course).mainTeacher(teacher).group(group).build());
    awardedCourseValidator.accept(awardedCourse);
    return awardedCourse;
  }
  @Transactional
  public List<AwardedCourse> createOrUpdateAwardedCourses(List<CreateAwardedCourse> createAwardedCourses) {
    return createAwardedCourses.stream().map(this::createOrUpdateAwardedCourse).collect(Collectors.toList());
  }
}
