package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.SmsContactGroup;
import school.hei.haapi.endpoint.rest.model.SmsContactGroupDetail;

@Component
@AllArgsConstructor
public class SmsContactGroupMapper {
  private final SmsContactMapper smsContactMapper;

  public SmsContactGroup toRest(school.hei.haapi.model.SmsContactGroup domain) {
    return new SmsContactGroup()
        .id(domain.getId())
        .name(domain.getName())
        .ownerId(domain.getOwner().getId())
        .memberCount(domain.getMembers().size());
  }

  public SmsContactGroupDetail toRestDetail(school.hei.haapi.model.SmsContactGroup domain) {
    return new SmsContactGroupDetail()
        .id(domain.getId())
        .name(domain.getName())
        .ownerId(domain.getOwner().getId())
        .memberCount(domain.getMembers().size())
        .members(domain.getMembers().stream().map(smsContactMapper::toRest).toList());
  }
}
