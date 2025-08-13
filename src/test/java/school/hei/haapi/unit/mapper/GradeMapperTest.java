package school.hei.haapi.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CreateGrade;
import school.hei.haapi.endpoint.rest.model.UpdateGrade;
import school.hei.haapi.endpoint.rest.validator.GradeValidator;
import school.hei.haapi.endpoint.rest.validator.UpdateGradeValidator;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.UserService;

class GradeMapperTest {
  private final GradeValidator gradeValidator = new GradeValidator();
  private final UserService userService = mock();
  private final GradeMapper subject =
      new GradeMapper(
          mock(),
          mock(),
          userService,
          gradeValidator,
          new UpdateGradeValidator(gradeValidator),
          mock(),
          mock());

  @Test
  void valid_grade_update_ok() {
    var grade = new UpdateGrade().comment("New comment").grade(new CreateGrade().score(20.));
    var studentRef = "ref";
    when(userService.findByRef(anyString())).thenReturn(User.builder().ref(studentRef).build());

    var domain = subject.toDomain(grade, "", studentRef);

    assertEquals(grade.getGrade().getScore(), domain.grade().getScore());
    assertEquals(grade.getComment(), domain.comment());
    assertEquals(studentRef, domain.student().getRef());
  }

  @Test
  void invalid_grade_update_ko() {
    var grade = new CreateGrade().score(20.);
    var updateGradeEmptyComment = new UpdateGrade().comment("").grade(grade);
    var updateGradeNullComment = new UpdateGrade().grade(grade);

    BadRequestException badRequestExceptionEmptyComment =
        assertThrows(
            BadRequestException.class, () -> subject.toDomain(updateGradeEmptyComment, "", ""));
    assertEquals(
        "Grade modification must be followed by comment about the change",
        badRequestExceptionEmptyComment.getMessage());

    BadRequestException badRequestExceptionNullComment =
        assertThrows(
            BadRequestException.class, () -> subject.toDomain(updateGradeNullComment, "", ""));
    assertEquals(
        "Grade modification must be followed by comment about the change",
        badRequestExceptionNullComment.getMessage());
  }
}
