package school.hei.haapi.endpoint.rest.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.AwardedCourseExam;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.endpoint.rest.model.AwardedCourse;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;

@AllArgsConstructor
@Component
public class AwardedCourseMapper {
  private final UserMapper userMapper;
  private final CourseMapper courseMapper;
  private final GradeMapper gradeMapper;
  private final GroupMapper groupMapper;
  //todo: to review all class
  public AwardedCourse toRest(school.hei.haapi.model.AwardedCourse awardedCourse) {
    return new AwardedCourse()
        .course(courseMapper.toRest(awardedCourse.getCourse()))
        .id(awardedCourse.getId())
        .group(groupMapper.toRest(awardedCourse.getGroup()))
        .mainTeacher(userMapper.toRestTeacher(awardedCourse.getMainTeacher()));
  }

  public AwardedCourseExam toRest(
      school.hei.haapi.model.AwardedCourse awardedCourse,
      List<StudentExamGrade> studentExamGrades) {
    return new AwardedCourseExam()
        .id(awardedCourse.getId())
        .exams(studentExamGrades)
        .mainTeacher(userMapper.toRestTeacher(awardedCourse.getMainTeacher()))
        .course(courseMapper.toRest(awardedCourse.getCourse()))
        .group(groupMapper.toRest(awardedCourse.getGroup()));
  }

  public List<AwardedCourseExam> toRest(
      List<school.hei.haapi.model.AwardedCourse> awardedCourses, User student) {
    List<AwardedCourseExam> awardedCourseExams = new ArrayList<>();
    for (school.hei.haapi.model.AwardedCourse awardedCourse : awardedCourses) {
      List<StudentExamGrade> studentExamGrades = awardedCourse.getExams().stream()
          .map(exam -> gradeMapper.toRestStudentExamGrade(student, exam))
          .collect(Collectors.toList());
      awardedCourseExams.add(toRest(awardedCourse, studentExamGrades));
    }
    return awardedCourseExams;
  }

  public List<school.hei.haapi.model.AwardedCourse> getAwardedCoursesDomainByGroups(
      List<Group> groups) {
    List<school.hei.haapi.model.AwardedCourse> awardedCourses = new ArrayList<>();
    for (Group group : groups) {
      awardedCourses.addAll(group.getAwardedCourse());
    }
    return awardedCourses;
  }
}
