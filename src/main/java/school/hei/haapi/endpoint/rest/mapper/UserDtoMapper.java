package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.UserDto;

@Component
@AllArgsConstructor
public class UserDtoMapper {
  private final UserMapper userMapper;
  private final StatusEnumMapper statusEnumMapper;

  public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .ref(user.getRef())
        .profilePicture(userMapper.getPresignedProfilePictureUrl(user))
        .status(statusEnumMapper.toRestStatus(user.getStatus()))
        .build();
  }
}
