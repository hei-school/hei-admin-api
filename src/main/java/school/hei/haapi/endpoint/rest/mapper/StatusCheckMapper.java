package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.model.StatusCheckResult.PENDING;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Role.TEACHER;

import jakarta.ws.rs.BadRequestException;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateStatusCheck;
import school.hei.haapi.endpoint.rest.model.StatusCheck;
import school.hei.haapi.model.User;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class StatusCheckMapper {
  private final UserMapper userMapper;
  private final UserService userService;

  public school.hei.haapi.model.StatusCheck toDomain(StatusCheck rest) {
    return school.hei.haapi.model.StatusCheck.builder()
        .id(rest.getId())
        .concernedStudent(userMapper.toDomain(rest.getConcernedStudent()))
        .requestingUser(userMapper.toDomain(rest.getRequestingUser()))
        .description(rest.getDescription())
        .creationDatetime(rest.getCreationDatetime())
        .updateDatetime(rest.getUpdateDatetime())
        .build();
  }

  public school.hei.haapi.model.StatusCheck toDomain(CreateStatusCheck createStatusCheck) {
    var concernedStudent =
        getUserByIdAndRoleIfPresent(createStatusCheck.getConcernedStudentId(), STUDENT);
    var requestingUser =
        getUserByIdAndRoleIfPresent(createStatusCheck.getRequestingUserId(), TEACHER);
    return school.hei.haapi.model.StatusCheck.builder()
        .id(createStatusCheck.getId())
        .concernedStudent(concernedStudent)
        .requestingUser(requestingUser)
        .result(PENDING)
        .description(createStatusCheck.getDescription())
        .creationDatetime(Instant.now())
        .updateDatetime(Instant.now())
        .build();
  }

  public StatusCheck toRest(school.hei.haapi.model.StatusCheck domain) {
    return new StatusCheck()
        .id(domain.getId())
        .concernedStudent(userMapper.toIdentifier(domain.getConcernedStudent()))
        .requestingUser(userMapper.toIdentifier(domain.getRequestingUser()))
        .description(domain.getDescription())
        .result(domain.getResult())
        .creationDatetime(domain.getCreationDatetime())
        .updateDatetime(domain.getUpdateDatetime());
  }

  private User getUserByIdAndRoleIfPresent(String id, User.Role expectedRole) {
    var concernedUser = userService.getById(id);
    if (!concernedUser.getRole().equals(expectedRole)) {
      throw new BadRequestException(
          "User with id " + id + " is not a " + expectedRole + " as expected.");
    }
    return concernedUser;
  }
}
