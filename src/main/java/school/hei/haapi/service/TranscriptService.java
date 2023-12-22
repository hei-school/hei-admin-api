package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.TranscriptValidator;
import school.hei.haapi.repository.TranscriptRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class TranscriptService {
  private TranscriptRepository transcriptRepository;
  private UserRepository userRepository;
  private TranscriptValidator validator;

  public Transcript getByIdAndStudent(String id, String studentId) {
    return transcriptRepository
        .findByIdAndStudentId(id, studentId)
        .orElseThrow(() -> new NotFoundException("Transcript with id " + id + " not found"));
  }

  public List<Transcript> getAllByStudentId(
      String studentId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return transcriptRepository.findAllByStudentId(studentId, pageable);
  }

  @Transactional
  public List<Transcript> updateStudentTranscript(String studentId, List<Transcript> transcripts) {
    User user = userRepository.getById(studentId);
    return transcripts.stream()
        .map(transcript -> this.updateTranscript(user, transcript))
        .collect(toUnmodifiableList());
  }

  private Transcript updateTranscript(User user, Transcript update) {
    Optional<Transcript> optionalTranscript =
        transcriptRepository.findByIdAndStudentId(update.getId(), user.getId());
    Transcript studentTranscript;
    if (optionalTranscript.isEmpty()) {
      studentTranscript =
          Transcript.builder()
              .id(update.getId())
              .student(user)
              .creationDatetime(update.getCreationDatetime())
              .build();
    } else {
      studentTranscript = optionalTranscript.get();
    }
    studentTranscript.setSemester(update.getSemester());
    studentTranscript.setAcademicYear(update.getAcademicYear());
    studentTranscript.setIsDefinitive(update.getIsDefinitive());
    validator.accept(studentTranscript);
    return transcriptRepository.save(studentTranscript);
  }
}
