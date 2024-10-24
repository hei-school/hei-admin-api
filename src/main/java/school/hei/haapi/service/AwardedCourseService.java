package school.hei.haapi.service;

import static java.util.stream.Collectors.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.model.CreateAwardedCourse;
import school.hei.haapi.model.AwardedCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.AwardedCourseValidator;
import school.hei.haapi.repository.AwardedCourseRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.AwardedCourseDao;

@Service
@AllArgsConstructor
public class AwardedCourseService {
  private final CourseRepository courseRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final AwardedCourseRepository awardedCourseRepository;
  private final AwardedCourseMapper awardedCourseMapper;
  private final AwardedCourseValidator awardedCourseValidator;
  private final AwardedCourseDao awardedCourseDao;

  public List<AwardedCourse> getByStudentId(String userId) {
    User student = userRepository.getById(userId);
    List<Group> groups =
        student.getGroupFlows().stream()
            .map(groupFlow -> groupFlow.getGroup())
            .distinct()
            .collect(toList());
    return awardedCourseMapper.getAwardedCoursesDomainByGroups(groups);
  }

  public List<AwardedCourse> getByGroupId(
      String groupId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return awardedCourseRepository.findAllByGroupId(groupId, pageable);
  }

  public AwardedCourse getById(String id, String groupId) {
    return awardedCourseRepository.getByIdAndGroupId(id, groupId);
  }

  public AwardedCourse createOrUpdateAwardedCourse(CreateAwardedCourse createAwardedCourse) {
    Group group = groupRepository.getById(createAwardedCourse.getGroupId());
    Course course = courseRepository.getById(createAwardedCourse.getCourseId());
    User teacher = userRepository.getById(createAwardedCourse.getMainTeacherId());
    AwardedCourse awardedCourse =
        awardedCourseRepository.save(
            AwardedCourse.builder().course(course).mainTeacher(teacher).group(group).build());
    awardedCourseValidator.accept(awardedCourse);
    return awardedCourse;
  }

  public Boolean checkTeacherOfAwardedCourse(
      String teacherId, String awardedCourseId, String groupId) {
    AwardedCourse awardedCourse = getById(awardedCourseId, groupId);
    return awardedCourse.getMainTeacher().getId().equals(teacherId);
  }

  @Transactional
  public List<AwardedCourse> createOrUpdateAwardedCourses(
      List<CreateAwardedCourse> createAwardedCourses) {
    return createAwardedCourses.stream().map(this::createOrUpdateAwardedCourse).collect(toList());
  }

  public List<AwardedCourse> getByCriteria(
      String teacherId, String courseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "name"));
    return awardedCourseDao.findByCriteria(teacherId, courseId, pageable);
  }

  public List<AwardedCourse> getAwardedCoursesByTeacherId(
      String teacherId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return awardedCourseRepository.findAllByMainTeacherId(teacherId, pageable);
  }

  @Transactional
  public List<AwardedCourse> createOrUpdateAwardedCoursesByTeacherId(
      String teacherId, List<AwardedCourse> awardedCourses) {
    awardedCourseValidator.accept(awardedCourses);
    return awardedCourseRepository.saveAll(awardedCourses);
  }

  public AwardedCourse findById(String awardedCourseId) {
    return awardedCourseRepository
        .findById(awardedCourseId)
        .orElseThrow(
            () ->
                new NotFoundException("Awarded course with id: " + awardedCourseId + " not found"));
  }
}
