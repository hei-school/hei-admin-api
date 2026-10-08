package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.BadgeInvalidity.EXPIRED;
import static school.hei.haapi.endpoint.rest.model.BadgeInvalidity.REVOKED;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.BadgeInvalidity;
import school.hei.haapi.endpoint.rest.model.BadgeOwner;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.service.StudentLevelResolver;
import school.hei.haapi.service.StudentSituationService;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class StudentBadgeMapper {
  private static final long PROFILE_PICTURE_URL_SECONDS = 5 * 60L;

  private final StatusEnumMapper statusEnumMapper;
  private final StudentLevelResolver studentLevelResolver;
  private final StudentSituationService studentSituationService;
  private final FileService fileService;

  /** What a scanned badge shows: with its late fees, nothing once the badge is invalid. */
  public PublicStudent toPublicRest(StudentBadge badge) {
    var invalidity = invalidityOf(badge);
    if (invalidity != null) {
      return new PublicStudent().isValid(false).invalidity(invalidity);
    }
    var student = badge.getStudent();
    var lateFees = studentSituationService.lateFeesOf(student);
    return toRest(badge)
        .lateFees(lateFees)
        .suspensionReason(studentSituationService.suspensionReasonOf(student, lateFees));
  }

  public PublicStudent toRest(StudentBadge badge) {
    var student = badge.getStudent();
    var invalidity = invalidityOf(badge);
    return new PublicStudent()
        .id(badge.getPublicId())
        .isValid(invalidity == null)
        .invalidity(invalidity)
        .academicYear(badge.getAcademicYear())
        .expirationDatetime(badge.getExpirationDatetime())
        .ref(student.getRef())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .status(statusEnumMapper.toRestStatus(student.getStatus()))
        .level(studentLevelResolver.findCurrentLevel(student).orElse(null))
        .specializationField(student.getSpecializationField())
        .profilePicture(profilePictureUrlOf(student));
  }

  public BadgeOwner toOwner(StudentBadge badge) {
    return new BadgeOwner().id(badge.getStudent().getId());
  }

  private static BadgeInvalidity invalidityOf(StudentBadge badge) {
    if (badge.isRevoked()) {
      return REVOKED;
    }
    return badge.isExpiredAt(Instant.now()) ? EXPIRED : null;
  }

  private String profilePictureUrlOf(User student) {
    var key = student.getProfilePictureKey();
    return key == null ? null : fileService.getPresignedUrl(key, PROFILE_PICTURE_URL_SECONDS);
  }
}
