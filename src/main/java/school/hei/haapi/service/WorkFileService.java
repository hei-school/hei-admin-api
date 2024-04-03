package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.*;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkFile;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.WorkFileRepository;

@Service
@AllArgsConstructor
public class WorkFileService {
  private final FileInfoService fileInfoService;
  private final UserService userService;
  private final WorkFileRepository workFileRepository;
  private final UserRepository userRepository;

  public WorkFile getStudentWorkFileById(String workFileId) {
    return workFileRepository
        .findById(workFileId)
        .orElseThrow(
            () -> {
              throw new NotFoundException(
                  "Student work document with id #" + workFileId + " not found");
            });
  }

  public List<WorkFile> getStudentWorkFiles(String studentId, Pageable pageable) {
    return workFileRepository.findAllByStudentId(studentId, pageable);
  }

  public WorkFile uploadStudentWorkFile(
      String studentId,
      String filename,
      Instant creationDatetime,
      Instant commitmentBegin,
      Instant commitmentEnd,
      WorkStudyStatus studentWorkStatus,
      MultipartFile workFile) {
    User student = userService.findById(studentId);

    student.setWorkStatus(
        defineStudentWorkStatus(commitmentBegin, commitmentEnd, studentWorkStatus));
    userRepository.save(student);

    return fileInfoService.uploadFile(
        student, filename, creationDatetime, commitmentBegin, commitmentEnd, workFile);
  }

  private WorkStudyStatus defineStudentWorkStatus(
      Instant commitmentBegin, Instant commitmentEnd, WorkStudyStatus studentWorkStatus) {
    Instant now = Instant.now();

    if (studentWorkStatus != null) {
      return studentWorkStatus;
    }
    if (now.isBefore(commitmentBegin)) {
      return WILL_BE_WORKING;
    }
    if (now.isAfter(commitmentEnd)) {
      return HAVE_BEEN_WORKING;
    }
    return WORKING;
  }
}
