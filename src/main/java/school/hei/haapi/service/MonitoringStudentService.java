package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.SexEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateMonitor;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;

@Slf4j
@Service
@AllArgsConstructor
public class MonitoringStudentService {
  private UserRepository userRepository;
  private SexEnumMapper sexEnumMapper;
  private StatusEnumMapper statusEnumMapper;
  private MonitoringStudentRepository monitoringStudentRepository;

  @Transactional
  public List<User> linkMonitorFollowingStudents(String monitorId, List<String> studentsIds) {
    try {
      monitoringStudentRepository.saveMonitorFollowingStudents(monitorId, studentsIds);
    } catch (DataIntegrityViolationException e) {
      log.error(e.getMessage());
      throw new BadRequestException(
          "One of the students with id %s can't be link with the monitor with id %s"
              .formatted(studentsIds, monitorId));
    }
    return monitoringStudentRepository.findAllById(studentsIds);
  }

  @Transactional
  public List<User> crupdateAndLinkMonitorFollowingStudents(List<CrupdateMonitor> monitors) {
    List<User> savedMonitors =
        userRepository.saveAll(
            monitors.stream()
                .map(
                    monitor -> {
                      User mappedMonitor = this.mapMonitorToDomain(monitor);
                      if (monitor.getId() == null) {
                        mappedMonitor.setId(UUID.randomUUID().toString());
                      }
                      return mappedMonitor;
                    })
                .toList());

    monitors.forEach(
        monitor -> {
          List<String> studentsIds =
              userRepository.findAllByRefIn(monitor.getStudentRefs()).stream()
                  .map(User::getId)
                  .collect(toUnmodifiableList());

          linkMonitorFollowingStudents(
              userRepository
                  .findByRef(monitor.getRef())
                  .orElseThrow(
                      () ->
                          new NotFoundException(
                              "User with ref: " + monitor.getRef() + " not found"))
                  .getId(),
              studentsIds);
        });

    return savedMonitors;
  }

  public List<User> getMonitorsByStudentId(String studentId) {
    return monitoringStudentRepository.findAllMonitorsByStudentId(studentId);
  }

  public List<String> getMonitorIdsByStudentId(String studentId) {
    return monitoringStudentRepository.getAllMonitorsIdsByStudentId(studentId);
  }

  public List<User> getStudentsByMonitorId(
      String monitorId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));

    return monitoringStudentRepository.findAllStudentsByMonitorId(monitorId, pageable);
  }

  private User mapMonitorToDomain(CrupdateMonitor monitor) {
    return User.builder()
        .role(User.Role.MONITOR)
        .id(monitor.getId())
        .firstName(monitor.getFirstName())
        .lastName(monitor.getLastName())
        .email(monitor.getEmail())
        .ref(monitor.getRef())
        .status(statusEnumMapper.toDomainStatus(monitor.getStatus()))
        .phone(monitor.getPhone())
        .entranceDatetime(monitor.getEntranceDatetime())
        .birthDate(monitor.getBirthDate())
        .sex(sexEnumMapper.toDomainSexEnum(monitor.getSex()))
        .address(monitor.getAddress())
        .nic(monitor.getNic())
        .birthPlace(monitor.getBirthPlace())
        .longitude(monitor.getCoordinates().getLongitude())
        .latitude(monitor.getCoordinates().getLatitude())
        .highSchoolOrigin(monitor.getHighSchoolOrigin())
        .build();
  }

  public User getStudentByIdAndMonitorId(String studentId, String monitorId)
      throws NotFoundException {
    var optionalStudent = userRepository.findById(studentId);
    if (optionalStudent.isEmpty() || optionalStudent.get().getRole() != STUDENT) {
      throw new NotFoundException("Student with id: " + studentId + " does not exist");
    }
    if (!monitoringStudentRepository.existsByIdAndMonitors_Id(monitorId, studentId)) {
      throw new NotFoundException(
          "Monitor with id: " + monitorId + " does not have a student with id: " + studentId);
    }
    return optionalStudent.get();
  }
}
