package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentTranscriptVersion;
import school.hei.haapi.repository.StudentTranscriptVersionRepository;
import school.hei.haapi.repository.UserRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class StudentTranscriptVersionService {

  private final StudentTranscriptVersionRepository studentTranscriptVersionRepository;
  private final UserRepository userRepository;

  public StudentTranscriptVersion getByIdAndStudentIdAndTranscriptId(String versionId, String studentId, String transcriptId) {
    return studentTranscriptVersionRepository.getByIdAndStudentIdAndTranscriptId(versionId, transcriptId, studentId);
  }
}
