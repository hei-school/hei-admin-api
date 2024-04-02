package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;

import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Announcement;
import school.hei.haapi.endpoint.rest.model.AnnouncementAuthor;
import school.hei.haapi.endpoint.rest.model.CreateAnnouncement;
import school.hei.haapi.endpoint.rest.model.GroupIdentifier;
import school.hei.haapi.model.User;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class AnnouncementMapper {

  private final FileService fileService;
  private final UserService userService;
  private final GroupService groupService;

  public Announcement toRest(school.hei.haapi.model.Announcement domain) {
    return new Announcement()
        .id(domain.getId())
        .author(toRest(domain.getAuthor()))
        .content(domain.getContent())
        .title(domain.getTitle())
        .creationDatetime(domain.getCreationDatetime())
        .scope(domain.getScope());
  }

  public school.hei.haapi.model.Announcement toDomain(CreateAnnouncement rest) {

    User author = userService.findById(rest.getAuthorId());

    return school.hei.haapi.model.Announcement.builder()
        .id(rest.getId())
        .groups(
            groupService.getAllById(
                Objects.requireNonNull(rest.getTargetGroupList()).stream()
                    .map(GroupIdentifier::getId)
                    .toList()))
        .author(author)
        .scope(rest.getScope())
        .build();
  }

  public AnnouncementAuthor toRest(User user) {

    String profilePictureKey = user.getProfilePictureKey();

    String pictureUrl =
        profilePictureKey != null
            ? fileService.getPresignedUrl(profilePictureKey, ONE_DAY_DURATION_AS_LONG)
            : null;

    return new AnnouncementAuthor()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .profilePicture(pictureUrl);
  }
}
