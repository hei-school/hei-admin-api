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
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.WorkDocumentRepository;

@Service
@AllArgsConstructor
public class WorkDocumentService {
  private final FileInfoService fileInfoService;
  private final UserService userService;
  private final WorkDocumentRepository workDocumentRepository;

  public WorkDocument getStudentWorkFileById(String workFileId) {
    return workDocumentRepository
        .findById(workFileId)
        .orElseThrow(
            () -> {
              throw new NotFoundException(
                  "Student work document with id #" + workFileId + " not found");
            });
  }

  public List<WorkDocument> getStudentWorkFiles(String studentId, Pageable pageable) {
    return workDocumentRepository.findAllByStudentId(studentId, pageable);
  }

  public WorkDocument uploadStudentWorkFile(
      String studentId,
      String filename,
      Instant creationDatetime,
      Instant commitmentBegin,
      Instant commitmentEnd,
      MultipartFile workFile) {
    User student = userService.findById(studentId);

    return fileInfoService.uploadFile(
        student, filename, creationDatetime, commitmentBegin, commitmentEnd, workFile);
  }

  public WorkDocument findLastWorkDocumentByStudentId(String studentId) {
    return workDocumentRepository
        .findFirstByStudentIdOrderByCreationDatetimeDesc(studentId)
        .orElse(new WorkDocument());
  }

  public WorkStudyStatus defineStudentWorkStatusFromWorkDocumentDetails(WorkDocument workDocument) {
    Instant now = Instant.now();

    if (workDocument.getCommitmentBegin() != null) {
      return WORKING;
    }
    if (workDocument.getCommitmentBegin() != null
        && now.isBefore(workDocument.getCommitmentBegin())) {
      return WILL_BE_WORKING;
    }
    if (workDocument.getCommitmentEnd() != null && now.isAfter(workDocument.getCommitmentEnd())) {
      return HAVE_BEEN_WORKING;
    }
    return null;
  }
}
