package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.repository.dao.CourseDao;

@Testcontainers
class CourseDaoTest extends FacadeITMockedThirdParties {
  @Autowired private CourseDao subject;

  @Test
  void filter_per_level_work() {
    var target = L2;
    var courses =
        subject.findByCriteria(
            null, null, null, null, null, null, null, target, Pageable.unpaged());

    courses.forEach(course -> assertEquals(target, course.getStudentLevel()));
  }
}
