package school.hei.haapi.integration.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.function.Executable;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.model.CourseAssignment;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;

/**
 * Assertion helpers shared by the integration tests.
 *
 * <p>Holds no test data: fixtures belong to {@code integration.testData} and each test owns the
 * rows it creates.
 */
public class ApiAssertions {

  public static void assertThrowsApiException(String expectedBody, Executable executable) {
    var apiException = assertThrows(ApiException.class, executable);
    assertEquals(expectedBody, apiException.getResponseBody());
  }

  public static void assertThrowsForbiddenException(Executable executable) {
    assertThrowsApiException(
        "{\"type\":\"403 FORBIDDEN\",\"message\":\"Access is denied\"}", executable);
  }

  public static void assertBadRequestException(String exceptedMessage, Executable executable) {
    assertThrowsApiException(
        "{\"type\":\"400 BAD_REQUEST\",\"message\":\"%s\"}".formatted(exceptedMessage), executable);
  }

  public static BadRequestException assertThrowsDomainBadRequestException(Executable executable) {
    return assertThrows(BadRequestException.class, executable);
  }

  public static void assertThrowsDomainBadRequestException(
      String expectedMessage, Executable executable) {
    var e = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedMessage, e.getMessage());
  }

  public static void asserThrowsDomainNotFoundException(
      String expectedMessage, Executable executable) {
    var e = assertThrows(NotFoundException.class, executable);
    assertEquals(expectedMessage, e.getMessage());
  }

  public static boolean isBefore(String a, String b) {
    return a.compareTo(b) < 0;
  }

  public static boolean isBefore(int a, int b) {
    return a < b;
  }

  public static boolean isValidUUID(String candidate) {
    try {
      UUID.fromString(candidate);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  // TODO: remove all these custom asserts once the auto-now on all creationTimestamp is by-passed
  public static void assertCourseAssignmentsIgnoringGroupCreationDateTime(
      List<CourseAssignment> actual, List<CourseAssignment> expected) {
    if (actual == null || expected == null) {
      fail("One of the lists is null");
      return;
    }
    var actualCloned =
        actual.stream().map(ApiAssertions::cloneCourseAssignmentNoTimestamp).toList();
    var expectedCloned =
        expected.stream().map(ApiAssertions::cloneCourseAssignmentNoTimestamp).toList();
    assertTrue(
        "Actual list does not contain all expected elements"
            + actualCloned
            + " did not contain "
            + expectedCloned,
        actualCloned.containsAll(expectedCloned));
  }

  private static CourseAssignment cloneCourseAssignmentNoTimestamp(CourseAssignment original) {
    var clone = new CourseAssignment();
    clone.setId(original.getId());
    clone.setMainTeacher(original.getMainTeacher());
    clone.setCourse(original.getCourse());
    clone.setGroups(
        original.getGroups().stream()
            .map(ApiAssertions::cloneGroupNoTimestamp)
            .collect(Collectors.toList()));
    return clone;
  }

  public static Group cloneGroupNoTimestamp(Group original) {
    return new Group()
        .id(original.getId())
        .name(original.getName())
        .ref(original.getRef())
        .size(original.getSize())
        .attributedColor(original.getAttributedColor());
  }
}
