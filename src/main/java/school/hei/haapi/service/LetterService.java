package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.PENDING;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SendLetterEmail;
import school.hei.haapi.endpoint.event.model.UpdateLetterEmail;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.endpoint.rest.model.UpdateLettersStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.LetterRepository;
import school.hei.haapi.repository.dao.LetterDao;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
public class LetterService {

  private final LetterRepository letterRepository;
  private final LetterDao letterDao;
  private final UserService userService;
  private final FileService fileService;
  private final MultipartFileConverter multipartFileConverter;
  private final EventProducer eventProducer;

  public List<Letter> getLetters(
      String ref,
      String studentRef,
      LetterStatus status,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return letterDao.findByCriteria(ref, studentRef, status, pageable);
  }

  public Letter getLetterById(String id) {
    return letterRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Letter not found"));
  }

  public Letter createLetter(
      String studentId, String description, String filename, MultipartFile file) {
    User user = userService.findById(studentId);
    String bucketKey = getBucketKey(user.getRef(), filename) + fileService.getFileExtension(file);
    final String uuid = UUID.randomUUID().toString();

    Letter letterToSave =
        Letter.builder()
            .id(uuid)
            .status(PENDING)
            .description(description)
            .student(user)
            .ref(generateRef(uuid))
            .filePath(bucketKey)
            .build();
    File fileToSave = multipartFileConverter.apply(file);
    fileService.uploadObjectToS3Bucket(bucketKey, fileToSave);

    eventProducer.accept(List.of(toSendLetterEmail(letterToSave)));
    return letterRepository.save(letterToSave);
  }

  public List<Letter> getLettersByStudentId(
      String studentId, LetterStatus status, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return Objects.isNull(status)
        ? letterRepository.findAllByStudentId(studentId, pageable)
        : letterRepository.findAllByStudentIdAndStatus(studentId, status, pageable);
  }

  public List<Letter> updateLetter(List<UpdateLettersStatus> letters) {
    return letters.stream()
        .map(
            lt -> {
              Letter letterToUpdate = getLetterById(lt.getId());
              letterToUpdate.setStatus(lt.getStatus());
              letterToUpdate.setApprovalDatetime(Instant.now());
              eventProducer.accept(List.of(toUpdateLetterEmail(letterToUpdate)));
              return letterRepository.save(letterToUpdate);
            })
        .toList();
  }

  public String getBucketKey(String studentRef, String filename) {
    return String.format("LETTERBOX/%s/%s", studentRef, filename);
  }

  public static String generateRef(String id) {
    return id.substring(0, 6)
        + "-"
        + DateTimeFormatter.ofPattern("yyyyMMdd")
            .withZone(ZoneId.of("UTC+3"))
            .format(Instant.now());
  }

  public SendLetterEmail toSendLetterEmail(Letter letter) {
    return SendLetterEmail.builder()
        .description(letter.getDescription())
        .id(letter.getId())
        .studentRef(letter.getStudent().getRef())
        .build();
  }

  public UpdateLetterEmail toUpdateLetterEmail(Letter letter) {
    return UpdateLetterEmail.builder()
        .id(letter.getId())
        .description(letter.getDescription())
        .ref(letter.getStudent().getRef())
        .email(letter.getStudent().getEmail())
        .build();
  }
}
