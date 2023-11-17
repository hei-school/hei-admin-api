package school.hei.haapi.endpoint.rest.mapper;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CourseSession;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;

import java.util.List;

@AllArgsConstructor
@Component
public class AttendanceMapper {
  private final AwardedCourseMapper awardedCourseMapper;
  private final UserMapper userMapper;
  private final UserRepository userRepository;

  public school.hei.haapi.endpoint.rest.model.StudentAttendance toRestAttendance(
      StudentAttendance domain) {
    CourseSession courseSession =
        domain.getCourseSession() != null
            ? toCourseSession(domain.getCourseSession())
            : new CourseSession();
    return new school.hei.haapi.endpoint.rest.model.StudentAttendance()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .student(userMapper.toRestStudent(domain.getStudent()))
        .lateOf((int) domain.lateOf(courseSession.getBegin()))
        .place(domain.getPlace())
        .courseSession(courseSession)
        .isLate(domain.isLate());
  }

  public StudentAttendanceMovement toRestMovement(school.hei.haapi.model.StudentAttendance domain) {
    return new StudentAttendanceMovement()
        .id(domain.getId())
        .attendanceMovementType(domain.getAttendanceMovementType())
        .createdAt(domain.getCreatedAt())
        .place(domain.getPlace())
        .student(userMapper.toRestStudent(domain.getStudent()));
  }

  public StudentAttendance toDomain(CreateAttendanceMovement toCreate) {
    if (userRepository.findByRefContainingIgnoreCase(toCreate.getStudentRef()).isEmpty()) {
      throw new NotFoundException(
          "the student with #" + toCreate.getStudentRef() + " doesn't exist");
    }
    return StudentAttendance.builder()
        .attendanceMovementType(toCreate.getAttendanceMovementType())
        .place(toCreate.getPlace())
        .createdAt(toCreate.getCreatedAt())
        .student(userRepository.findByRefContainingIgnoreCase(toCreate.getStudentRef()).get())
        .build();
  }

  public void accept(List<CreateAttendanceMovement> toCreates) {
    List<String> wrongStds = new ArrayList<>();
    toCreates.forEach(movement -> {
      if (!userRepository.findByRefContainingIgnoreCase(movement.getStudentRef()).isPresent()) {
        wrongStds.add(movement.getStudentRef());
      }
    });
    if (!wrongStds.isEmpty()) {
      if(wrongStds.size() > 1) {
        throw new NotFoundException("Students with: #" + wrongStds.toString() + " are not found");
      }
      else {
        throw new NotFoundException("Student with: #" + wrongStds.toString() + " is not found");
      }
    }
  }

  public CourseSession toCourseSession(school.hei.haapi.model.CourseSession toMap) {
    return new CourseSession()
        .awarededCourse(awardedCourseMapper.toRest(toMap.getAwardedCourse()))
        .id(toMap.getId())
        .end(toMap.getEnd())
        .begin(toMap.getBegin());
  }
}
