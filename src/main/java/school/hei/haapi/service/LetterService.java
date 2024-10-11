package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.*;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.BANK_TRANSFER;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SendLetterEmail;
import school.hei.haapi.endpoint.event.model.UpdateLetterEmail;
import school.hei.haapi.endpoint.rest.model.LetterStats;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.endpoint.rest.model.UpdateLettersStatus;
import school.hei.haapi.model.*;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.LetterValidator;
import school.hei.haapi.repository.LetterRepository;
import school.hei.haapi.repository.dao.LetterDao;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
@Slf4j
public class LetterService {

  private final LetterRepository letterRepository;
  private final LetterDao letterDao;
  private final UserService userService;
  private final FileService fileService;
  private final MultipartFileConverter multipartFileConverter;
  private final EventProducer eventProducer;
  private final FeeService feeService;
  private final PaymentService paymentService;
  private final LetterValidator validator;

  public List<Letter> getLetters(
      String ref,
      String studentRef,
      LetterStatus status,
      String name,
      String feeId,
      Boolean isLinkedWithFee,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return letterDao.findByCriteria(
        ref, studentRef, status, name, feeId, isLinkedWithFee, pageable);
  }

  public Letter getLetterById(String id) {
    return letterRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Letter not found"));
  }

  public Letter getByFeeId(String feeId) {
    return letterRepository.findByFeeId(feeId).orElse(null);
  }

  public Letter createLetter(
      String studentId,
      String description,
      String filename,
      MultipartFile file,
      String feeId,
      Integer amount) {
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
            .amount(amount)
            .build();

    if (Objects.nonNull(feeId)) {
      letterToSave.setFee(feeService.getById(feeId));
    }

    File fileToSave = multipartFileConverter.apply(file);
    fileService.uploadObjectToS3Bucket(bucketKey, fileToSave);

    validator.accept(letterToSave);

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

              if (lt.getStatus() == REJECTED && Objects.isNull(lt.getReasonForRefusal())) {
                throw new BadRequestException("Must provide a reason for refusal");
              }
              if (lt.getStatus() == PENDING) {
                throw new BadRequestException("Cannot update a status to pending");
              }
              letterToUpdate.setReasonForRefusal(lt.getReasonForRefusal());

              if (lt.getStatus() == RECEIVED && Objects.nonNull(letterToUpdate.getFee())) {
                Payment payment =
                    Payment.builder()
                        .type(BANK_TRANSFER)
                        .comment(letterToUpdate.getFee().getComment())
                        .isDeleted(false)
                        .amount(letterToUpdate.getAmount())
                        .fee(letterToUpdate.getFee())
                        .creationDatetime(Instant.now())
                        .build();
                paymentService.saveAll(List.of(payment));
              }

              eventProducer.accept(List.of(toUpdateLetterEmail(letterToUpdate)));
              return letterRepository.save(letterToUpdate);
            })
        .toList();
  }

  public LetterStats getStats() {
    return new LetterStats()
        .pending(letterRepository.countByStatus(PENDING))
        .rejected(letterRepository.countByStatus(REJECTED))
        .received(letterRepository.countByStatus(RECEIVED));
  }

  public String getBucketKey(String studentRef, String filename) {
    return String.format("LETTERBOX/%s/%s", studentRef, filename);
  }

  public static String generateRef(String id) {
    return "HEI-"
        + id.substring(0, 6)
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
        .studentEmail(letter.getStudent().getEmail())
        .build();
  }

  public UpdateLetterEmail toUpdateLetterEmail(Letter letter) {
    return UpdateLetterEmail.builder()
        .id(letter.getId())
        .description(letter.getDescription())
        .ref(letter.getStudent().getRef())
        .email(letter.getStudent().getEmail())
        .reason(letter.getReasonForRefusal())
        .status(letter.getStatus())
        .build();
  }
}
