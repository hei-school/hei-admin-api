package school.hei.haapi.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.CourseSession;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.repository.AttendanceRepository;
import school.hei.haapi.repository.dao.StudentAttendanceDao;

@Service
@AllArgsConstructor
public class AttendanceService {
  private final AttendanceRepository attendanceRepository;
  private final StudentAttendanceDao studentAttendanceDao;

  public StudentAttendance createStudentAttendanceMovement(StudentAttendance toCreate) {
    return attendanceRepository.save(toCreate);
  }

  public Set<StudentAttendance> getStudentAttendances(
      String studentKeyword, List<String> coursesIds, List<AttendanceStatus> attendanceStatuses,
      Instant from, Instant to, PageFromOne page, BoundedPageSize pageSize
      ) {
    Pageable pageable = PageRequest.of((page.getValue() - 1), pageSize.getValue());
    Set<StudentAttendance> studentAttendanceList =
        new HashSet<>(studentAttendanceDao.findByStudentKeyWord(studentKeyword, pageable));

    if(!coursesIds.isEmpty()) {
      studentAttendanceList.addAll(
          attendanceRepository.findByCoursesSessionCriteria(coursesIds, from, to, pageable)
      );
    }
    if(!attendanceStatuses.isEmpty()) {
      studentAttendanceList.addAll(
        getAttendanceByAttendanceStatuses(attendanceStatuses, pageable)
      );
    }
    return studentAttendanceList;
  }

  public Set<StudentAttendance> getAttendanceByAttendanceStatuses(List<AttendanceStatus> attendanceStatuses, Pageable pageable) {
    Set<StudentAttendance> result = new HashSet<>();
    Map<AttendanceStatus, List<StudentAttendance>> eachStatusValues = new HashMap<>();
    eachStatusValues.put(AttendanceStatus.MISSING, attendanceRepository.findStudentsAbsent(pageable));
    eachStatusValues.put(AttendanceStatus.LATE, attendanceRepository.findStudentByAttendanceStatus(true, pageable));
    eachStatusValues.put(AttendanceStatus.PRESENT, attendanceRepository.findStudentByAttendanceStatus(false, pageable));

    attendanceStatuses.forEach(status -> {
      result.addAll(eachStatusValues.get(status));
    });

    return result;
  }
}
