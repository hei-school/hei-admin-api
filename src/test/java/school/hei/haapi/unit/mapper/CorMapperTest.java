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
import school.hei.haapi.endpoint.rest.model.Cor;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
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

    assertEqualsCor(crupdateCor, rest);
  }

  void assertEqualsCor(CrupdateCor excepted, Cor actual) {
    assertEquals(excepted.getId(), actual.getId());
    assertEquals(excepted.getDescription(), actual.getDescription());
    assertEquals(excepted.getStatus(), actual.getStatus());
    assertEquals(excepted.getInterviewDate(), actual.getInterviewDate());
    assertNotNull(actual.getConcernedStudent());
    assertEquals(excepted.getConcernedStudentId(), actual.getConcernedStudent().getId());
  }
}
