package school.hei.haapi.endpoint.rest.mapper;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.service.StudentBadgeService;

@Component
@AllArgsConstructor
public class StudentBadgeMapper {
  private final UserMapper userMapper;
  private final StatusEnumMapper statusEnumMapper;

  public PublicStudent toRest(StudentBadge badge) {
    User student = badge.getStudent();
    return new PublicStudent()
        .id(badge.getPublicId())
        .isValid(!badge.isRevoked())
        .ref(student.getRef())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .status(statusEnumMapper.toRestStatus(student.getStatus()))
        .level(StudentBadgeService.findLevel(student, Instant.now()).orElse(null))
        .specializationField(student.getSpecializationField())
        .profilePicture(userMapper.getPresignedProfilePictureUrl(student));
  }
}
