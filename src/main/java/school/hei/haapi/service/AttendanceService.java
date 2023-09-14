package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.repository.AttendanceRepository;

@Service
@AllArgsConstructor
public class AttendanceService {
  private final AttendanceRepository attendanceRepository;

  public StudentAttendance createStudentAttendanceMovement(StudentAttendance toCreate) {
    return attendanceRepository.save(toCreate);
  }
}
