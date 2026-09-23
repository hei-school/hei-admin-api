package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.SmsContactGroupMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.model.SmsContact;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;

class SmsContactGroupMapperTest {
  private final SmsContactGroupMapper subject = new SmsContactGroupMapper(new SmsContactMapper());

  private final User owner = User.builder().id("admin1").ref("ADM000001").build();

  @Test
  void toRest_reports_only_the_member_count_not_the_members_themselves() {
    var member = SmsContact.builder().id("c1").owner(owner).build();
    var group =
        SmsContactGroup.builder()
            .id("g1")
            .name("Promo 2026")
            .owner(owner)
            .members(List.of(member))
            .build();

    var rest = subject.toRest(group);

    assertEquals("g1", rest.getId());
    assertEquals("Promo 2026", rest.getName());
    assertEquals("admin1", rest.getOwnerId());
    assertEquals(1, rest.getMemberCount());
  }

  @Test
  void toRestDetail_embeds_the_full_member_list() {
    var member =
        SmsContact.builder()
            .id("c1")
            .phoneNumber("321111111")
            .name("Antenaina")
            .owner(owner)
            .ownerRole(school.hei.haapi.model.SmsContactOwnerRole.ADMIN)
            .build();
    var group =
        SmsContactGroup.builder()
            .id("g1")
            .name("Promo 2026")
            .owner(owner)
            .members(List.of(member))
            .build();

    var rest = subject.toRestDetail(group);

    assertEquals(1, rest.getMemberCount());
    assertEquals(1, rest.getMembers().size());
    assertEquals("321111111", rest.getMembers().get(0).getPhoneNumber());
    assertEquals("Antenaina", rest.getMembers().get(0).getName());
  }
}
