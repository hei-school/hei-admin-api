package school.hei.haapi.service;

import static school.hei.haapi.service.utils.AttendanceServiceUtils.filterAttendanceFromTwoSet;
import static school.hei.haapi.service.utils.AttendanceServiceUtils.getFilterCase;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import school.hei.haapi.service.utils.AttendanceServiceUtils;

@Service
@AllArgsConstructor
public class AttendanceService {
  private final AttendanceRepository attendanceRepository;
  private final StudentAttendanceDao studentAttendanceDao;
  private final AttendanceServiceUtils utils;

  public List<StudentAttendance> createStudentAttendanceMovement(List<StudentAttendance> toCreate) {
    List<StudentAttendance> toCreateMapped = new ArrayList<>();
    toCreate.forEach(studentAttendance -> {
      utils.saveStudentAttendance(studentAttendance, toCreateMapped);
    });
    return toCreateMapped;
  }

  public List<StudentAttendance> getStudentAttendances(
      String studentKeyword, List<String> coursesIds, List<String> teachersIds,
      List<AttendanceStatus> attendanceStatuses, Instant from, Instant to,
      PageFromOne page, BoundedPageSize pageSize
  ) {
    Pageable pageable = PageRequest.of((page.getValue() - 1), pageSize.getValue());
    List<StudentAttendance> result = new ArrayList<>();
    List<StudentAttendance> studentAttendanceList =
        studentAttendanceDao.findByStudentKeyWordAndCourseSessionCriteria(
            studentKeyword, pageable, coursesIds, teachersIds, from, to
        );

    switch (getFilterCase(attendanceStatuses)) {
      case 1:
        result = filterAttendanceFromTwoSet(
            studentAttendanceList,
            new HashSet<>(getAttendanceByAttendanceStatuses(attendanceStatuses, pageable)));
        break;
      case 2:
        result = studentAttendanceList;
    }
    return result;
  }

  public Set<StudentAttendance> getAttendanceByAttendanceStatuses(
      List<AttendanceStatus> attendanceStatuses, Pageable pageable) {
    Set<StudentAttendance> result = new LinkedHashSet<>();
    Map<AttendanceStatus, List<StudentAttendance>> eachStatusValues = new HashMap<>();
    eachStatusValues.put(AttendanceStatus.MISSING,
        attendanceRepository.findStudentsAbsent(pageable));
    eachStatusValues.put(AttendanceStatus.LATE, attendanceRepository.findStudentLate(pageable));
    eachStatusValues.put(AttendanceStatus.PRESENT,
        attendanceRepository.findStudentPresent(pageable));

    attendanceStatuses.forEach(status -> {
      result.addAll(eachStatusValues.get(status));
    });

    return result;
  }
}