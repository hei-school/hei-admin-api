package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.integration.conf.TestUtils.course1;
import static school.hei.haapi.integration.conf.TestUtils.teacher1;
import static school.hei.haapi.model.User.Role.TEACHER;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.endpoint.rest.mapper.SexEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.UserManagerDao;

class UserManagerDaoTest extends FacadeITMockedThirdParties {
  @Autowired private UserManagerDao subject;
  @Autowired private SexEnumMapper sexEnumMapper;
  @Autowired private StatusEnumMapper statusEnumMapper;

  @Test
  void filter_user_ok() {
    List<User> teachers =
        subject.findByCriteria(
            TEACHER,
            teacher1().getRef(),
            teacher1().getFirstName(),
            teacher1().getLastName(),
            PageRequest.of(0, 10),
            statusEnumMapper.toDomainStatus(teacher1().getStatus()),
            sexEnumMapper.toDomainSexEnum(teacher1().getSex()),
            null,
            null,
            course1().getId(),
            null,
            null);

    assertEquals(1, teachers.size());
    assertEquals(teacher1().getId(), teachers.getFirst().getId());
  }
}
