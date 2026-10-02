package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.service.StudentLevelResolver;

@Component
@AllArgsConstructor
public class StudentBadgeMapper {
  private final UserMapper userMapper;
  private final StatusEnumMapper statusEnumMapper;
  private final StudentLevelResolver studentLevelResolver;

  public PublicStudent toRest(StudentBadge badge) {
    var student = badge.getStudent();
    return new PublicStudent()
        .id(badge.getPublicId())
        .isValid(badge.isValidAt(Instant.now()))
        .academicYear(badge.getAcademicYear())
        .expirationDatetime(badge.getExpirationDatetime())
        .ref(student.getRef())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .status(statusEnumMapper.toRestStatus(student.getStatus()))
        .level(studentLevelResolver.findCurrentLevel(student).orElse(null))
        .specializationField(student.getSpecializationField())
        .profilePicture(userMapper.getPresignedProfilePictureUrl(student));
  }
}
