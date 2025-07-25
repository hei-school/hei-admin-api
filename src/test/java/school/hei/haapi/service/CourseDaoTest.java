package school.hei.haapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.dao.CourseDao;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;

@Testcontainers
class CourseDaoTest extends FacadeITMockedThirdParties {
    @Autowired private CourseDao subject;

    @Test
    void filter_per_level_work() {
        List<Course> courses = subject.findByCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                L2,
                Pageable.unpaged());

        assertEquals(3, courses.size());
        assertEquals(L2, courses.getFirst().getStudentLevel());
        assertEquals(L2, courses.get(1).getStudentLevel());
        assertEquals(L2, courses.get(2).getStudentLevel());
    }
}
