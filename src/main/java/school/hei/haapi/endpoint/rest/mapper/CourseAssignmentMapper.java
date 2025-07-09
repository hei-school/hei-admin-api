package school.hei.haapi.endpoint.rest.mapper;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CourseAssignmentExam;
import school.hei.haapi.endpoint.rest.model.CreateCourseAssignment;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.endpoint.rest.model.CourseAssignment;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.SchoolFileService;
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

  public school.hei.haapi.model.CourseAssignment toDomain(CreateCourseAssignment courseAssignment) {
    school.hei.haapi.model.Course course = courseService.getById(courseAssignment.getCourseId());
    school.hei.haapi.model.User teacher = userService.findById(courseAssignment.getMainTeacherId());
    List<Group> groups = groupService.getAllById(courseAssignment.getGroups());

    return school.hei.haapi.model.CourseAssignment.builder()
            .id(courseAssignment.getId())
            .mainTeacher(teacher)
            .groups(groups)
            .course(course)
            .build();
  }
  // todo: to review all class
  public CourseAssignment toRest(school.hei.haapi.model.CourseAssignment courseAssignment) {
    return new CourseAssignment()
        .course(courseMapper.toRest(courseAssignment.getCourse()))
        .id(courseAssignment.getId())
        .groups(groupMapper.toRest(courseAssignment.getGroups()))
        .mainTeacher(userMapper.toRestTeacher(courseAssignment.getMainTeacher()));
  }

  public CourseAssignmentExam toRest(
          school.hei.haapi.model.CourseAssignment courseAssignment, List<StudentGrade> studentExamGrades) {
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
              .collect(toList());
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

  public school.hei.haapi.model.CourseAssignment toRest(
      CreateCourseAssignment createCourseAssignment) {
    List<Group> groups = groupService.getAllById(createCourseAssignment.getGroups());
    Course course = courseService.getById(createCourseAssignment.getCourseId());
    User teacher = userService.findById(createCourseAssignment.getMainTeacherId());

    return school.hei.haapi.model.CourseAssignment.builder()
        .id(createCourseAssignment.getId())
        .groups(groups)
        .course(course)
        .mainTeacher(teacher)
        .creationDatetime(Instant.now())
        .build();
  }
}
