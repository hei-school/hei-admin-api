package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.model.User;

class SmsContactMapperTest {
  private final SmsContactMapper subject = new SmsContactMapper();

  @Test
  void every_domain_owner_role_maps_to_its_rest_counterpart_by_name() {
    for (var domainRole : school.hei.haapi.model.SmsContactOwnerRole.values()) {
      var rest = subject.toRest(domainRole);
      assertEquals(domainRole.name(), rest.name());
      assertEquals(domainRole, subject.toDomain(rest));
    }
  }

  @Test
  void null_owner_role_is_null_safe_both_ways() {
    assertNull(subject.toRest((school.hei.haapi.model.SmsContactOwnerRole) null));
    assertNull(subject.toDomain(null));
  }

  @Test
  void contact_name_joins_first_and_last_name() {
    var user = User.builder().firstName("Antenaina").lastName("Jaonina").build();
    assertEquals("Antenaina Jaonina", SmsContactMapper.toContactName(user));
  }

  @Test
  void contact_name_tolerates_missing_first_or_last_name() {
    var user = User.builder().firstName(null).lastName("Jaonina").build();
    assertEquals(" Jaonina", SmsContactMapper.toContactName(user));
  }

  @Test
  void a_contact_maps_to_its_rest_representation() {
    var owner = User.builder().id("owner1").ref("REF-1").build();
    var domain =
        school.hei.haapi.model.SmsContact.builder()
            .id("c1")
            .phoneNumber("321111111")
            .name("Antenaina Jaonina")
            .owner(owner)
            .ownerRole(school.hei.haapi.model.SmsContactOwnerRole.STUDENT)
            .build();

    var rest = subject.toRest(domain);

    assertEquals("c1", rest.getId());
    assertEquals("321111111", rest.getPhoneNumber());
    assertEquals("Antenaina Jaonina", rest.getName());
    assertEquals("owner1", rest.getOwnerId());
    assertEquals("REF-1", rest.getOwnerRef());
    assertEquals(
        school.hei.haapi.endpoint.rest.model.SmsContactOwnerRole.STUDENT, rest.getOwnerRole());
  }
}
