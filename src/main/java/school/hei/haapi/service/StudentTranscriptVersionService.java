package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentTranscriptClaim;
import school.hei.haapi.model.StudentTranscriptVersion;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.StudentTranscriptVersionRepository;

import java.util.List;
import java.util.Objects;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class StudentTranscriptVersionService {

  private final StudentTranscriptVersionRepository studentTranscriptVersionRepository;

  public StudentTranscriptVersion getByIdAndStudentIdAndTranscriptId(
          String versionId, String transcriptId, String studentId) {
    return studentTranscriptVersionRepository.getByIdAndStudentIdAndTranscriptId(versionId, transcriptId, studentId);
  }

  public List<StudentTranscriptVersion> getAllByStudentIdAndTranscriptId(
          String studentId, String transcriptId, PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of(
            page.getValue() - 1,
            pageSize.getValue(),
            Sort.by(DESC, StudentTranscriptVersion.CREATION_DATETIME));
    return studentTranscriptVersionRepository.getAllByStudentIdAndTranscriptId(studentId, transcriptId, pageable);
  }
}
