package school.hei.haapi.integration.conf;

import static java.time.ZoneOffset.UTC;
import static java.util.concurrent.TimeUnit.DAYS;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.endpoint.rest.model.CrupdateTeacher;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.Mpbs.Mpbs;
import school.hei.haapi.model.User;

/**
 * Utility class for generating realistic fake data for testing purposes
 *
 * <p><b>Important:</b> Add/Modify data generation methods to this class as needs are identified
 */
@Component
public class FakeDataProvider {
  private static final Faker faker = new Faker();

  public static List<CrupdateCourseAssignment> someCreatableCreateAwardedCourseList(
      int nbOfCrupdateCourseAssignment) {
    List<CrupdateCourseAssignment> createAwardedCourseList = new ArrayList<>();
    for (int i = 0; i < nbOfCrupdateCourseAssignment; i++) {
      createAwardedCourseList.add(createAwardedCourse());
    }
    return createAwardedCourseList;
  }

  public static CrupdateTeacher someCreatableTeacher() {
    return new CrupdateTeacher()
        .firstName(faker.name().firstName())
        .lastName(faker.name().lastName())
        .email(faker.internet().emailAddress())
        .ref(someRef("TCR"))
        .phone(faker.phoneNumber().phoneNumber())
        .status(ENABLED)
        .sex(faker.options().option(Sex.class))
        .birthDate(LocalDate.parse("2000-01-01"))
        .entranceDatetime(Instant.parse("2021-11-08T08:25:24.00Z"))
        .coordinates(new Coordinates().latitude(null).longitude(null))
        .address(faker.address().fullAddress());
  }

  private static String someRef(String prefix) {
    return "%s%s".formatted(prefix, faker.number().digits(5));
  }

  public static CrupdateCourseAssignment createAwardedCourse() {
    return new CrupdateCourseAssignment()
        .courseId("course2_id")
        .groups(List.of("group2_id"))
        .mainTeacherId("teacher2_id");
  }

  public Group createGroup() {
    return new Group()
        .name(faker.lorem().sentence(10))
        .ref(someRef("GRP"))
        .creationDatetime(faker.date().past(30, DAYS).toInstant());
  }

  public List<Group> someCreatableGroupList(int nbOfGroup) {
    List<Group> groupList = new ArrayList<>();
    for (int i = 0; i < nbOfGroup; i++) {
      groupList.add(createGroup());
    }
    return groupList;
  }

  public Fee someFee(User student) {
    return Fee.builder()
        .id(UUID.randomUUID().toString())
        .comment(faker.lorem().sentence(10))
        .student(student)
        .totalAmount(0)
        .remainingAmount(0)
        .status(PENDING)
        .updatedAt(Instant.now())
        .creationDatetime(Instant.now())
        .dueDatetime(Instant.now())
        .category(L1)
        .frequency(MONTHLY)
        .type(TUITION)
        .build();
  }

  public Event someEvent() {
    var beginDatetime =
        faker
            .date()
            .past(
                200, DAYS, Date.from(LocalDate.now().minusYears(1).atStartOfDay().toInstant(UTC)));
    return new Event(
        UUID.randomUUID().toString(),
        faker.options().option(EventType.class),
        faker.lorem().sentence(2),
        faker.lorem().sentence(10),
        null,
        false,
        beginDatetime.toInstant(),
        faker.date().between(beginDatetime, faker.date().past(10, DAYS)).toInstant(),
        null,
        new Course("", "", "", 0, 0, List.of()),
        List.of());
  }

  public Mpbs someMpbs(User student) {
    return Mpbs.builder()
        .status(faker.options().option(MpbsStatus.class))
        .fee(someFee(student))
        .amount(faker.number().numberBetween(100, 10_000))
        .build();
  }
}
