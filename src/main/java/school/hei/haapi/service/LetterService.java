package school.hei.haapi.service;

import static java.util.UUID.randomUUID;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.FileType.OTHER;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.PENDING;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.RECEIVED;
import static school.hei.haapi.endpoint.rest.model.LetterStatus.REJECTED;
import static school.hei.haapi.endpoint.rest.model.Payment.TypeEnum.BANK_TRANSFER;
import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.SendLetterEmail;
import school.hei.haapi.endpoint.event.model.UpdateLetterEmail;
import school.hei.haapi.endpoint.rest.model.LetterStats;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.endpoint.rest.model.UpdateLettersStatus;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.FileInfo;
import school.hei.haapi.model.Letter;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.letterStatsDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.FileInfoRepository;
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
  private final EventParticipantService eventParticipantService;
  private final FileInfoRepository fileInfoRepository;

  public List<Letter> getLetters(
      String ref,
      String studentRef,
      LetterStatus status,
      String name,
      String feeId,
      Boolean isLinkedWithFee,
      List<User.Role> roles,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return letterDao.findByCriteria(
        ref, studentRef, status, name, feeId, isLinkedWithFee, roles, pageable);
  }

  public List<Letter> getLettersByEventParticipantId(String eventParticipantId) {
    return letterRepository.findByEventParticipantId(eventParticipantId).stream().toList();
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
      Integer amount,
      String eventParticipantId) {
    User user = userService.getById(studentId);

    final String uuid = randomUUID().toString();

    String bucketKey =
        getBucketKey(user.getRef(), file.getOriginalFilename() + uuid)
            + fileService.getFileExtension(file);

    Letter letterToSave =
        Letter.builder()
            .id(uuid)
            .status(PENDING)
            .description(description)
            .user(user)
            .ref(generateRef(uuid))
            .filePath(bucketKey)
            .amount(amount)
            .eventParticipant(
                Objects.isNull(eventParticipantId)
                    ? null
                    : eventParticipantService.findById(eventParticipantId))
            .build();

    if (Objects.nonNull(feeId)) {
      letterToSave.setFee(feeService.getById(feeId));
    }

    File fileToSave = multipartFileConverter.apply(file);
    fileService.uploadObjectToS3Bucket(bucketKey, fileToSave);

    eventProducer.accept(List.of(toSendLetterEmail(letterToSave)));

    return letterRepository.save(letterToSave);
  }

  private String getLetterReceiver() {
    Principal principal = getPrincipal();
    if (Objects.equals(principal.getRole(), "STUDENT")) {
      return "contact@mail.hei.school";
    }
    return "valisoa@mail.hei.school";
  }

  public List<Letter> getLettersByStudentId(
      String userId, LetterStatus status, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return Objects.isNull(status)
        ? letterRepository.findAllByUserId(userId, pageable)
        : letterRepository.findAllByUserIdAndStatus(userId, status, pageable);
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

              if (lt.getStatus() == RECEIVED) {
                FileInfo fileInfo =
                    FileInfo.builder()
                        .fileType(OTHER)
                        .creationDatetime(Instant.now())
                        .name(letterToUpdate.getDescription())
                        .user(letterToUpdate.getUser())
                        .filePath(letterToUpdate.getFilePath())
                        .build();
                fileInfoRepository.save(fileInfo);
              }

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

  public LetterStats getStats(List<User.Role> role) {
    letterStatsDto letterStatsDto;
    if (Objects.nonNull(role) && !role.isEmpty()) {
      letterStatsDto = letterRepository.getLetterStatisticsForRoles(role);
    } else {
      letterStatsDto = letterRepository.getLetterStatistics();
    }
    return letterStatsDto.toLetterStats();
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
        .studentRef(letter.getUser().getRef())
        .studentEmail(letter.getUser().getEmail())
        .receiver(getLetterReceiver())
        .build();
  }

  public UpdateLetterEmail toUpdateLetterEmail(Letter letter) {
    return UpdateLetterEmail.builder()
        .id(letter.getId())
        .description(letter.getDescription())
        .ref(letter.getUser().getRef())
        .email(letter.getUser().getEmail())
        .reason(letter.getReasonForRefusal())
        .status(letter.getStatus())
        .build();
  }
}
