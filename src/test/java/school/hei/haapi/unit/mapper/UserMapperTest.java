package school.hei.haapi.unit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

public class UserMapperTest extends FacadeITMockedThirdParties {
  @Autowired private UserMapper userMapper;
  @Autowired private UserRepository userRepository;

  private User student;

  @BeforeEach
  void setUp() {
    student = userRepository.save(axel());
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteById(student.getId());
  }

  @Test
  void user_identifier_map_ok() {
    var testStudent = userRepository.findById(student.getId()).orElseThrow();

    var identifierStudent = userMapper.toIdentifier(testStudent);
    var mappedStudent = userMapper.toDomain(identifierStudent);

    assertEquals(testStudent, mappedStudent);
  }
}
