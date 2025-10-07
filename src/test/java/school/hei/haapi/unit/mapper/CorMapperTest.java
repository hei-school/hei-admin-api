package school.hei.haapi.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.FakeDataProvider.someCreatableCor;
import static school.hei.haapi.integration.conf.FakeDataProvider.someStudent;

import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.CorCommentMapper;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.UserIdentifier;
import school.hei.haapi.service.UserService;

class CorMapperTest {
  private final UserMapper userMapperMock = mock();
  private final UserService userServiceMock = mock();
  private final CorMapper subject =
      new CorMapper(userMapperMock, userServiceMock, new CorCommentMapper());

  @Test
  void cor_status_correct_conversion() {
    var student = someStudent("axel");
    when(userServiceMock.getById(student.getId())).thenReturn(student);
    when(userMapperMock.toIdentifier(student)).thenReturn(new UserIdentifier().id(student.getId()));

    var crupdateCor = someCreatableCor(student.getId());
    var domain = subject.toDomain(crupdateCor);
    var rest = subject.toRest(domain);

    assertEquals(crupdateCor.getId(), rest.getId());
    assertEquals(crupdateCor.getDescription(), rest.getDescription());
    assertEquals(crupdateCor.getStatus(), rest.getStatus());
    assertEquals(crupdateCor.getInterviewDate(), rest.getInterviewDate());
    assertNotNull(rest.getConcernedStudent());
    assertEquals(crupdateCor.getConcernedStudentId(), rest.getConcernedStudent().getId());
  }
}
