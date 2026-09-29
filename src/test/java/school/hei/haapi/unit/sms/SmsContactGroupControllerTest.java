package school.hei.haapi.unit.sms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.controller.SmsContactGroupController;
import school.hei.haapi.endpoint.rest.mapper.SmsContactGroupMapper;
import school.hei.haapi.endpoint.rest.mapper.SmsContactMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateSmsContactGroup;
import school.hei.haapi.endpoint.rest.security.model.Principal;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.SmsContactGroup;
import school.hei.haapi.model.User;
import school.hei.haapi.service.sms.SmsContactGroupService;

class SmsContactGroupControllerTest {
  private final SmsContactGroupService smsContactGroupServiceMock = mock();
  private final SmsContactGroupMapper smsContactGroupMapper =
      new SmsContactGroupMapper(new SmsContactMapper());

  private final SmsContactGroupController subject =
      new SmsContactGroupController(smsContactGroupServiceMock, smsContactGroupMapper);

  private final User owner = User.builder().id("admin1").build();
  private final Principal principal = new Principal(owner, "token");

  private SmsContactGroup group() {
    return SmsContactGroup.builder().id("g1").name("Promo 2026").owner(owner).build();
  }

  @Test
  void lists_the_authenticated_owners_groups() {
    when(smsContactGroupServiceMock.getByOwner(any(), any())).thenReturn(List.of(group()));

    var page = subject.getSmsContactGroups(principal, new PageFromOne(1), new BoundedPageSize(10));

    assertEquals(1, page.size());
    assertEquals("g1", page.get(0).getId());
  }

  @Test
  void creates_a_group_for_the_authenticated_owner() {
    when(smsContactGroupServiceMock.create(owner, "Promo 2026", List.of("c1"))).thenReturn(group());
    var toCreate = new CrupdateSmsContactGroup().name("Promo 2026").contactIds(List.of("c1"));

    var created = subject.createSmsContactGroup(toCreate, principal);

    assertEquals("g1", created.getId());
  }

  @Test
  void gets_a_group_with_its_members() {
    when(smsContactGroupServiceMock.getById("g1")).thenReturn(group());

    assertEquals("g1", subject.getSmsContactGroupById("g1").getId());
  }

  @Test
  void updates_a_group() {
    var updated = SmsContactGroup.builder().id("g1").name("New name").owner(owner).build();
    when(smsContactGroupServiceMock.update("g1", "New name", List.of("c2"))).thenReturn(updated);
    var toUpdate = new CrupdateSmsContactGroup().name("New name").contactIds(List.of("c2"));

    assertEquals("New name", subject.updateSmsContactGroup("g1", toUpdate).getName());
  }

  @Test
  void deletes_a_group() {
    when(smsContactGroupServiceMock.delete("g1")).thenReturn(group());

    assertEquals("g1", subject.deleteSmsContactGroup("g1").getId());
  }
}
