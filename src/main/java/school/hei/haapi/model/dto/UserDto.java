package school.hei.haapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import school.hei.haapi.model.User;
import school.hei.haapi.service.aws.FileService;

@Builder
@Getter
@AllArgsConstructor
public class UserDto {
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String profilePicture;
  private String ref;
  private static final long ONE_DAY_DURATION_AS_LONG = 24 * 60 * 60 * 1000L;

  public static UserDto from(User user, FileService fileService) {
    String profilePictureUrl = null;

    if (user.getProfilePictureKey() != null) {
      profilePictureUrl =
          fileService.getPresignedUrl(user.getProfilePictureKey(), ONE_DAY_DURATION_AS_LONG);
    }
    return UserDto.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .ref(user.getRef())
        .profilePicture(profilePictureUrl)
        .build();
  }
}
