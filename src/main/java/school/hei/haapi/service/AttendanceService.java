package school.hei.haapi.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.AttendanceStatus;
import school.hei.haapi.model.BoundedPageSize;
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

  public List<StudentAttendance> getStudentAttendanceByStudentKeyword(
      String studentKeyword, PageFromOne page, BoundedPageSize pageSize
  ) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue()
    );

    return studentAttendanceDao.findByStudentKeyWord(studentKeyword, pageable);
  }

  public List<StudentAttendance> getStudentAttendanceByCoursesIds(List<String> coursesIds) {
    return attendanceRepository.findByCoursesId(coursesIds);
  }
}
