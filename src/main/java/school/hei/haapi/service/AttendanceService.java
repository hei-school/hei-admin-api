package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.repository.AttendanceRepository;

@Service
@AllArgsConstructor
public class AttendanceService {
  private final AttendanceRepository attendanceRepository;

  public List<StudentAttendance> createStudentAttendanceMovement(List<StudentAttendance> toCreate) {
    return attendanceRepository.saveAll(toCreate);
  }
}
