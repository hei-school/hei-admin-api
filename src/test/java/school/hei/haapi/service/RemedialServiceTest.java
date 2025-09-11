package school.hei.haapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Remedial;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.RemedialDao;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RemedialServiceTest extends FacadeITMockedThirdParties {
    @MockBean
    private RemedialDao remedialDaoMock;
    @Autowired
    private RemedialService subject;

    @Test
    void get_all_remedials_OK() {
        Remedial remedial = new Remedial();
        User student = new User();
        student.setId("1");
        student.setEmail("");
        remedial.setId("1");
        remedial.setTitle("Weather prediction");
        remedial.setStudents(List.of(student));
        when(remedialDaoMock.findByCriteria(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(remedial));

        List<Remedial> remedials = subject.getAllRemedials(new PageFromOne(1),new BoundedPageSize(2),null,null,null,null,null,null);

        assertEquals("Weather prediction", remedials.getFirst().getTitle());
   }
}