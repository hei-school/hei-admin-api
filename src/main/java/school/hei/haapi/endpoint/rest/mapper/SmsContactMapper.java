package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SmsContact;
import school.hei.haapi.endpoint.rest.model.SmsContactOwnerRole;
import school.hei.haapi.model.User;

@Component
public class SmsContactMapper {

  public SmsContact toRest(school.hei.haapi.model.SmsContact domain) {
    return new SmsContact()
        .id(domain.getId())
        .phoneNumber(domain.getPhoneNumber())
        .name(domain.getName())
        .ownerId(domain.getOwner().getId())
        .ownerRef(domain.getOwner().getRef())
        .ownerRole(toRest(domain.getOwnerRole()));
  }

  public SmsContactOwnerRole toRest(school.hei.haapi.model.SmsContactOwnerRole domain) {
    return domain == null ? null : SmsContactOwnerRole.valueOf(domain.name());
  }

  public school.hei.haapi.model.SmsContactOwnerRole toDomain(SmsContactOwnerRole rest) {
    return rest == null ? null : school.hei.haapi.model.SmsContactOwnerRole.valueOf(rest.name());
  }

  public static String toContactName(User user) {
    return (user.getFirstName() == null ? "" : user.getFirstName())
        + " "
        + (user.getLastName() == null ? "" : user.getLastName());
  }
}
