package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Complaint;
import school.hei.haapi.model.ComplaintAssignee;
import school.hei.haapi.model.User;

class ComplaintTest {
  @Test
  void getRoutedManager_returns_empty_when_no_assignees() {
    var complaint = Complaint.builder().build();

    assertTrue(complaint.getRoutedManager().isEmpty());
  }

  @Test
  void getRoutedManager_returns_empty_when_no_assignee_is_auto_routed() {
    var manuallyAssignedUser = User.builder().id("staff-1").build();
    var assigner = User.builder().id("manager-1").build();
    var complaint =
        Complaint.builder()
            .assignees(
                List.of(
                    ComplaintAssignee.builder()
                        .user(manuallyAssignedUser)
                        .assignedBy(assigner)
                        .build()))
            .build();

    assertTrue(complaint.getRoutedManager().isEmpty());
  }

  @Test
  void getRoutedManager_returns_the_auto_routed_manager() {
    var routedManager = User.builder().id("manager-1").build();
    var complaint =
        Complaint.builder()
            .assignees(
                List.of(ComplaintAssignee.builder().user(routedManager).assignedBy(null).build()))
            .build();

    var found = complaint.getRoutedManager();

    assertTrue(found.isPresent());
    assertEquals(routedManager.getId(), found.get().getId());
  }
}
