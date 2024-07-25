package school.hei.haapi.service;

import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.HAVE_BEEN_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.NOT_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WILL_BE_WORKING;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.WORKING;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.model.ProfessionalExperienceFileTypeEnum;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.WorkDocumentRepository;
import school.hei.haapi.repository.dao.WorkDocumentDao;

@Service
@AllArgsConstructor
public class WorkDocumentService {
  private final FileInfoService fileInfoService;
  private final UserService userService;
  private final WorkDocumentRepository workDocumentRepository;
  private final WorkDocumentDao workDocumentDao;

  public WorkDocument getStudentWorkFileById(String workFileId) {
    return workDocumentRepository
        .findById(workFileId)
        .orElseThrow(
            () -> {
              throw new NotFoundException(
                  "Student work document with id #" + workFileId + " not found");
            });
  }

  public List<WorkDocument> getStudentWorkFiles(
      String studentId,
      ProfessionalExperienceFileTypeEnum professionalExperience,
      Pageable pageable) {
    return workDocumentDao.findAllByStudentIdAndProfessionalExperienceType(
        studentId, professionalExperience, pageable);
  }

  public WorkDocument uploadStudentWorkFile(
      String studentId,
      String filename,
      Instant creationDatetime,
      Instant commitmentBegin,
      Instant commitmentEnd,
      MultipartFile workFile,
      ProfessionalExperienceFileTypeEnum professionalExperience) {
    User student = userService.findById(studentId);

    return fileInfoService.uploadFile(
        student,
        filename,
        creationDatetime,
        commitmentBegin,
        commitmentEnd,
        workFile,
        professionalExperience);
  }

  public Optional<WorkDocument> findLastWorkDocumentByStudentId(String studentId) {
    return workDocumentRepository.findTopByStudentIdOrderByCreationDatetimeDesc(studentId);
  }

  public Instant defineStudentCommitmentBegin(Optional<WorkDocument> workDocument) {
    if (!workDocument.isPresent()) {
      return null;
    }
    return workDocument.map(WorkDocument::getCommitmentBegin).orElse(null);
  }

  public WorkStudyStatus defineStudentWorkStatusFromWorkDocumentDetails(
      Optional<WorkDocument> workDocument) {
    return workDocument.map(this::getStudentWorkStudy).orElse(NOT_WORKING);
  }

  public ProfessionalExperienceFileTypeEnum defineStudentProfessionalExperienceStatus(
      Optional<WorkDocument> workDocument) {
    return workDocument.map(WorkDocument::getProfessionalExperienceType).orElse(null);
  }

  private WorkStudyStatus getStudentWorkStudy(WorkDocument workDocument) {
    Instant now = Instant.now();

    if (workDocument.getCommitmentBegin() != null
        && now.isBefore(workDocument.getCommitmentBegin())) {
      return WILL_BE_WORKING;
    }
    if (workDocument.getCommitmentEnd() != null && now.isAfter(workDocument.getCommitmentEnd())) {
      return HAVE_BEEN_WORKING;
    }
    return WORKING;
  }
}
