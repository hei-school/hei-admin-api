package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CourseAssignment;
import school.hei.haapi.endpoint.rest.model.CourseAssignmentExam;
import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.CourseAssignmentValidator;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.UserService;

@AllArgsConstructor
@Component
public class CourseAssignmentMapper {
  private final UserMapper userMapper;
  private final CourseMapper courseMapper;
  private final GradeMapper gradeMapper;
  private final GroupMapper groupMapper;
  private final GroupService groupService;
  private final CourseService courseService;
  private final UserService userService;
  private final CourseAssignmentValidator validator;

  public school.hei.haapi.model.CourseAssignment toDomain(
      CrupdateCourseAssignment crupdateCourseAssignment) {
    school.hei.haapi.model.Course course =
        courseService.getById(crupdateCourseAssignment.getCourseId());
    return toDomain(crupdateCourseAssignment, course);
  }

  public school.hei.haapi.model.CourseAssignment toDomain(
      String courseId, CrupdateCourseAssignment crupdateCourseAssignment) {
    school.hei.haapi.model.Course course = courseService.getById(courseId);
    return toDomain(crupdateCourseAssignment, course);
  }

  @NotNull
  private school.hei.haapi.model.CourseAssignment toDomain(
      CrupdateCourseAssignment crupdateCourseAssignment, Course course) {
    User teacher = userService.findById(crupdateCourseAssignment.getMainTeacherId());
    List<Group> groups = groupService.getAllById(crupdateCourseAssignment.getGroupIds());
    var domainCourseAssignment =
        school.hei.haapi.model.CourseAssignment.builder()
            .id(crupdateCourseAssignment.getId())
            .mainTeacher(teacher)
            .groups(groups)
            .course(course)
            .build();
    validator.accept(domainCourseAssignment);
    return domainCourseAssignment;
  }

  public CourseAssignment toRest(school.hei.haapi.model.CourseAssignment courseAssignment) {
    return new CourseAssignment()
        .id(courseAssignment.getId())
        .course(courseMapper.toRest(courseAssignment.getCourse()))
        .groups(groupMapper.toRest(courseAssignment.getGroups()))
        .mainTeacher(userMapper.toRestTeacher(courseAssignment.getMainTeacher()));
  }

  public List<CourseAssignment> toRest(
      List<school.hei.haapi.model.CourseAssignment> courseAssignments) {
    return courseAssignments.stream().map(this::toRest).toList();
  }

  public CourseAssignmentExam toRest(
      school.hei.haapi.model.CourseAssignment courseAssignment,
      List<StudentGrade> studentExamGrades) {
    return new CourseAssignmentExam()
        .id(courseAssignment.getId())
        .exams(studentExamGrades)
        .mainTeacher(userMapper.toRestTeacher(courseAssignment.getMainTeacher()))
        .course(courseMapper.toRest(courseAssignment.getCourse()))
        .groups(groupMapper.toRest(courseAssignment.getGroups()));
  }

  public List<CourseAssignmentExam> toRest(
      List<school.hei.haapi.model.CourseAssignment> courseAssignments, User student) {
    var courseAssignmentExams = new ArrayList<CourseAssignmentExam>();
    for (var courseAssignment : courseAssignments) {
      List<StudentGrade> studentExamGrades =
          courseAssignment.getExams().stream()
              .map(exam -> gradeMapper.toRestStudentExamGrade(student, exam))
              .toList();
      courseAssignmentExams.add(toRest(courseAssignment, studentExamGrades));
    }
    return courseAssignmentExams;
  }

  public List<school.hei.haapi.model.CourseAssignment> toDomainCourseAssignmentsByGroups(
      List<Group> groups) {
    var courseAssignments = new ArrayList<school.hei.haapi.model.CourseAssignment>();
    for (Group group : groups) {
      courseAssignments.addAll(group.getCourseAssignments());
    }
    return courseAssignments;
  }

  public CourseAssignment toRest(CrupdateCourseAssignment crupdateCourseAssignment) {
    List<Group> groups = groupService.getAllById(crupdateCourseAssignment.getGroupIds());
    Course course = courseService.getById(crupdateCourseAssignment.getCourseId());
    User teacher = userService.findById(crupdateCourseAssignment.getMainTeacherId());

    return new CourseAssignment()
        .id(crupdateCourseAssignment.getId())
        .groups(groups.stream().map(groupMapper::toRest).toList())
        .course(courseMapper.toRest(course))
        .mainTeacher(userMapper.toRestTeacher(teacher));
  }
}
