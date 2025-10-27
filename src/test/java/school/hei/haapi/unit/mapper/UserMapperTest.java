package school.hei.haapi.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.integration.conf.TestUtils.STUDENT1_ID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.repository.UserRepository;

public class UserMapperTest extends FacadeITMockedThirdParties {
  @Autowired private UserMapper userMapper;
  @Autowired private UserRepository userRepository;

  @Test
  void user_identifier_map_ok() {
    var testStudent = userRepository.findById(STUDENT1_ID);
    var identifierStudent = userMapper.toIdentifier(testStudent.get());
    var mappedStudent = userMapper.toDomain(identifierStudent);
    assertEquals(testStudent.get(), mappedStudent);
  }
}
