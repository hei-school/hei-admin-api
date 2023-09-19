package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CourseSession;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.endpoint.rest.model.StudentAttendanceMovement;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.UserRepository;

@AllArgsConstructor
@Component
public class AttendanceMapper {
  private final CourseMapper courseMapper;
  private final UserMapper userMapper;
  private final UserRepository userRepository;

  public school.hei.haapi.endpoint.rest.model.StudentAttendance toRestAttendance(StudentAttendance domain) {
    CourseSession courseSession = new CourseSession()
        .id(domain.getCourseSession().getId())
        .course(courseMapper.toRest(domain.getCourseSession().getCourse()))
        .begin(domain.getCourseSession().getBegin())
        .end(domain.getCourseSession().getEnd());
    return new school.hei.haapi.endpoint.rest.model.StudentAttendance()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .student(userMapper.toRestStudent(domain.getStudent()))
        .place(domain.getPlace())
        .courseSession(courseSession)
        .isLate(domain.isLate())
        .lateOf(domain.lateOf(courseSession.getBegin()));
  }

  public StudentAttendanceMovement toRestMovement (school.hei.haapi.model.StudentAttendance domain) {
    return new StudentAttendanceMovement()
        .id(domain.getId())
        .attendanceMovementType(domain.getAttendanceMovementType())
        .createdAt(domain.getCreatedAt())
        .place(domain.getPlace())
        .student(userMapper.toRestStudent(domain.getStudent()));
  }

  public StudentAttendance toDomain (CreateAttendanceMovement toCreate) {
    if(userRepository.findById(toCreate.getStudentId()).isEmpty()) {
      throw new BadRequestException("the student with #"+toCreate.getStudentId()+" doesn't exist");
    }
    return StudentAttendance.builder()
        .attendanceMovementType(toCreate.getAttendanceMovementType())
        .place(toCreate.getPlace())
        .createdAt(toCreate.getCreatedAt())
        .student(userRepository.findById(toCreate.getStudentId()).get())
        .build();
  }
}
