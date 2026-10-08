package school.hei.haapi.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.notEntity.CreateGroup;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.dao.GroupDao;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.sms.StudentGroupContactSyncService;

class GroupServiceTest {
  private final GroupRepository groupRepository = mock();
  private final StudentGroupContactSyncService studentGroupContactSyncService = mock();
  private final GroupService subject =
      new GroupService(
          groupRepository,
          mock(UserManagerDao.class),
          mock(GroupFlowService.class),
          groupRepository,
          mock(GroupDao.class),
          studentGroupContactSyncService);

  @Test
  void saveAll_creates_a_matching_contact_group_for_every_new_group() {
    var group = Group.builder().id("g1").name("K2").ref("K2").build();
    var creator = User.builder().id("creator1").build();
    when(groupRepository.save(any())).thenReturn(group);

    subject.saveAll(List.of(CreateGroup.builder().group(group).build()), creator);

    verify(studentGroupContactSyncService, times(1)).createContactGroupFor(group, creator);
  }
}
