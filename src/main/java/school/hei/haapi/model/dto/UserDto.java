package school.hei.haapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.model.User;

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
  private EnableStatus status;

  public static UserDto from(User user) {
    return UserDto.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .ref(user.getRef())
        .profilePicture(user.getProfilePictureKey())
        .build();
  }
}
