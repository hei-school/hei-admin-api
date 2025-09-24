package school.hei.haapi.integration.conf;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.DAYS;
import static school.hei.haapi.endpoint.rest.model.EnableStatus.ENABLED;
import static school.hei.haapi.endpoint.rest.model.FeeCategory.L1;
import static school.hei.haapi.endpoint.rest.model.FeeFrequency.MONTHLY;
import static school.hei.haapi.endpoint.rest.model.FeeStatusEnum.PENDING;
import static school.hei.haapi.endpoint.rest.model.FeeTypeEnum.TUITION;
import static school.hei.haapi.integration.conf.TestUtils.COURSE1_ID;
import static school.hei.haapi.integration.conf.TestUtils.MANAGER_ID;
import static school.hei.haapi.integration.conf.TestUtils.group1;
import static school.hei.haapi.model.CorComment.CorStatus.IN_PROGRESS;
import static school.hei.haapi.model.Event.PlaceName.IVANDRY;
import static school.hei.haapi.model.Event.RoomName.UNKNOWN;
import static school.hei.haapi.model.User.Role.STUDENT;

import com.github.javafaker.Faker;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.CreateEvent;
import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.endpoint.rest.model.CrupdateTeacher;
import school.hei.haapi.endpoint.rest.model.EventLocation;
import school.hei.haapi.endpoint.rest.model.EventType;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.MpbsStatus;
import school.hei.haapi.endpoint.rest.model.PlaceEnum;
import school.hei.haapi.endpoint.rest.model.RoomEnum;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Event;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.User;
import school.hei.haapi.model.mpbs.Mpbs;

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
      createAwardedCourseList.add(createCourseAssignment());
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

  public static CrupdateCourseAssignment createCourseAssignment() {
    return new CrupdateCourseAssignment()
        .courseId("course2_id")
        .groupIds(List.of("group2_id"))
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
        UNKNOWN,
        IVANDRY,
        faker.lorem().sentence(10),
        null,
        false,
        beginDatetime.toInstant(),
        faker.date().between(beginDatetime, faker.date().past(10, DAYS)).toInstant(),
        null,
        Course.builder()
            .id("")
            .code("")
            .name("")
            .credits(0)
            .totalHours(0)
            .courseAssignments(List.of())
            .build(),
        List.of());
  }

  public Mpbs someMpbs(User student) {
    return Mpbs.builder()
        .status(faker.options().option(MpbsStatus.class))
        .fee(someFee(student))
        .amount(faker.number().numberBetween(100, 10_000))
        .build();
  }

  public static CreateEvent someCreatableEvent(
      EventType eventType,
      String planerId,
      Instant beginDatetime,
      Instant endDatetime,
      List<Group> groups) {
    return new CreateEvent()
        .id("event" + randomUUID() + "_id")
        .courseId(COURSE1_ID)
        .beginDatetime(beginDatetime)
        .endDatetime(endDatetime)
        .description("Another event")
        .eventType(eventType)
        .plannerId(planerId)
        .location(someLocation())
        .groups(groups.stream().map(TestUtils::createGroupIdentifier).toList());
  }

  private static EventLocation someLocation() {
    return new EventLocation()
        .room(faker.options().option(RoomEnum.class))
        .place(faker.options().option(PlaceEnum.class));
  }

  public static CreateEvent someCreatableEvent(
      EventType eventType, String planerId, Instant beginDatetime, Instant endDatetime) {
    return someCreatableEvent(eventType, planerId, beginDatetime, endDatetime, List.of(group1()));
  }

  public static CreateEvent someCreatableEventByManager1(EventType eventType) {
    return someCreatableEvent(
        eventType,
        MANAGER_ID,
        Instant.parse("2023-12-08T08:00:00.00Z"),
        Instant.parse("2023-12-08T10:00:00.00Z"));
  }

  public static User someStudent(String firstName) {
    return User.builder(someCoordinates())
        .id(UUID.randomUUID().toString())
        .role(STUDENT)
        .firstName(firstName)
        .email(faker.internet().emailAddress())
        .ref(someRef("STD"))
        .lastName(faker.name().lastName())
        .address(faker.address().fullAddress())
        .status(User.Status.ENABLED)
        .entranceDatetime(Instant.now())
        .build();
  }

  public static Coordinates someCoordinates() {
    return new Coordinates()
        .latitude(faker.number().randomDouble(2, -90, 90))
        .longitude(faker.number().randomDouble(2, -180, 180));
  }

  public static Cor someCor(User user, Instant interviewDatetime) {
    return Cor.builder()
        .id(UUID.randomUUID().toString())
        .interviewDatetime(interviewDatetime)
        .concernedStudent(user)
        .description(faker.lorem().paragraph())
        .status(IN_PROGRESS)
        .build();
  }
}
