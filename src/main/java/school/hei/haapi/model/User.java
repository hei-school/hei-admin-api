package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Comparator.comparing;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.User.Status.*;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import school.hei.haapi.endpoint.rest.model.Coordinates;
import school.hei.haapi.endpoint.rest.model.SpecializationField;
import school.hei.haapi.model.exception.ApiException;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update \"user\" set is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
// TODO: separate to a child table as MANAGER, TEACHER, STUDENT, MONITOR
public class User implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String firstName;

  @NotBlank(message = "Last name is mandatory")
  private String lastName;

  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Reference is mandatory")
  private String ref;

  private String nic;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  @Setter(AccessLevel.NONE)
  private Status status;

  private String phone;

  private String degree;

  private String function;

  private String ostie;

  private String cnaps;

  private Instant endingService;

  private LocalDate birthDate;

  private String birthPlace;

  private Instant entranceDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private SpecializationField specializationField;

  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private Sex sex;

  private String address;

  @Column(name = "\"role\"")
  @Enumerated(STRING)
  @JdbcTypeCode(NAMED_ENUM)
  private Role role;

  private String profilePictureKey;

  // RELATION (TEACHER): Course Assignment
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "mainTeacher")
  @ToString.Exclude
  private List<CourseAssignment> courseAssignments;

  // RELATION (STUDENT): Group Flows
  @OneToMany(mappedBy = "student", fetch = LAZY)
  @ToString.Exclude
  @JsonIgnore
  private List<GroupFlow> groupFlows;

  // RELATION (STUDENT): Grades
  @OneToMany(mappedBy = "student", fetch = LAZY)
  @ToString.Exclude
  @JsonIgnore
  private List<Grade> grades;

  // RELATION (STUDENT): Work Documents
  @OneToMany(mappedBy = "student", fetch = LAZY)
  @JsonIgnore
  private List<WorkDocument> workDocuments;

  // RELATION (MONITOR - STUDENT): Which Monitor follows which students or which student is
  // following by which monitor
  // TODO: check if joinColumns and inversJoinColumns are in the correct place, refactor if need be.
  @ManyToMany(fetch = LAZY)
  @JoinTable(
      name = "\"monitor_following_student\"",
      joinColumns = @JoinColumn(name = "\"monitor_id\""),
      inverseJoinColumns = @JoinColumn(name = "\"student_id\""))
  private List<User> monitors;

  private Double longitude;
  private Double latitude;

  private String highSchoolOrigin;

  @OneToMany(mappedBy = "user", fetch = LAZY)
  @JsonIgnore
  private List<Letter> letters;

  @OneToMany(mappedBy = "user", fetch = LAZY)
  private List<AnnouncementReaction> reactions;

  public void setStatus(Status newStatus) {
    if (DISABLED.equals(this.getStatus()) && ENABLED.equals(newStatus)) {
      throw new IllegalArgumentException("DISABLED User cannot be set back to ENABLED");
    }

    if (DISABLED.equals(this.getStatus()) && SUSPENDED.equals(newStatus)) {
      throw new IllegalArgumentException("DISABLED User cannot be set to SUSPENDED");
    }

    this.status = newStatus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    User user = (User) o;
    return id != null && Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "User{"
        + "id='"
        + id
        + '\''
        + ", firstName='"
        + firstName
        + '\''
        + ", lastName='"
        + lastName
        + '\''
        + ", email='"
        + email
        + '\''
        + ", ref='"
        + ref
        + '\''
        + ", nic='"
        + nic
        + '\''
        + ", status="
        + status
        + ", phone='"
        + phone
        + '\''
        + ", birthDate="
        + birthDate
        + ", birthPlace='"
        + birthPlace
        + '\''
        + ", entranceDatetime="
        + entranceDatetime
        + ", specializationField="
        + specializationField
        + ", sex="
        + sex
        + ", address='"
        + address
        + '\''
        + ", role="
        + role
        + ", profilePictureKey='"
        + profilePictureKey
        + '\''
        + ", highSchoolOrigin='"
        + highSchoolOrigin
        + '}';
  }

  public Optional<Group> findCurrentGroup() {
    var lastGroupFlow =
        this.getGroupFlows().stream()
            .filter(groupFlow -> JOIN.equals(groupFlow.getGroupFlowType()))
            .max(comparing(GroupFlow::getFlowDatetime));
    return lastGroupFlow.map(GroupFlow::getGroup);
  }

  public Optional<Group> findGroupAt(Instant instant) {
    return this.getGroupFlows().stream()
        .filter(
            groupFlow ->
                JOIN.equals(groupFlow.getGroupFlowType())
                    && instant.isAfter(groupFlow.getFlowDatetime()))
        .max(comparing(GroupFlow::getFlowDatetime))
        .map(GroupFlow::getGroup);
  }

  public enum Sex {
    M,
    F;
  }

  public enum Status {
    ENABLED,
    DISABLED,
    SUSPENDED,
    ALUMNI,
  }

  public enum Role {
    STAFF_MEMBER,
    ADMIN,
    MONITOR,
    STUDENT,
    TEACHER,
    MANAGER,
    ORGANIZER;
  }

  public String getSpecializationFieldString() {
    return switch (this.specializationField) {
      case COMMON_CORE -> "Tronc commun";
      case TN -> "Transformation Numérique";
      case EL -> "Écosystème Logiciel";
      default -> throw new ApiException(SERVER_EXCEPTION, "Invalid specialization field");
    };
  }

  public static User.UserBuilder builder() {
    return new User.UserBuilder();
  }

  public static User.UserBuilder builder(Coordinates coordinates) {
    var builder = User.builder();

    if (coordinates != null)
      builder.longitude(coordinates.getLongitude()).latitude(coordinates.getLatitude());

    return builder;
  }
}
