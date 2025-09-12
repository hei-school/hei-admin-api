package school.hei.haapi.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.rest.api.ExamsApi;
import school.hei.haapi.endpoint.rest.api.RemedialsApi;
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.controller.RemedialController;
import school.hei.haapi.endpoint.rest.mapper.RemedialMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateRemedial;
import school.hei.haapi.endpoint.rest.model.Fraction;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.integration.conf.TestUtils;
import school.hei.haapi.model.*;
import school.hei.haapi.service.RemedialService;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.conf.TestUtils.TEACHER1_TOKEN;
import static school.hei.haapi.integration.conf.TestUtils.createExam1;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.RemedialTestData.createRemedial;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;

public class RemedialIT extends FacadeITMockedThirdParties {
    @MockBean RemedialService  remedialService;
    @Autowired
    RemedialController remedialController;
    @Autowired
    RemedialMapper remedialMapper;

    private ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, localPort);
    }

    private static Remedial remedialProg1(){
        return new Remedial(
                "1",
                "Prog 1 remedial",
                createCourseAssignment(prog1(), toky(), List.of(g1())),
                Instant.parse("2025-07-22T10:15:30Z"),
                List.of(axel(), tolojanahary())
        );
    }

    private static Remedial remedialProg2(){
        return new Remedial(
                "2",
                "Prog 2 remedial",
                createCourseAssignment(prog2(), toky(), List.of(g2())),
                Instant.parse("2025-11-22T10:15:30Z"),
                List.of(axel(), tolojanahary())
        );
    }

    @Test
    public void get_all_remedials_ok() {
        var remedials = List.of(remedialProg1(), remedialProg2());
        when(remedialService.getAllRemedials(any(), any(), anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(remedials);
       assertEquals(remedialMapper.toRestList(remedials),
               remedialController.getAllRemedials(new PageFromOne(1), new BoundedPageSize(2), null, null, null, null, null, null));
    }

    @Test
     public void teacher_create_or_update_remedial_ok() throws ApiException {
        ApiClient teacher1Client = anApiClient(TEACHER1_TOKEN);
        RemedialsApi api = new RemedialsApi(teacher1Client);
        var remedial = createRemedial(createCourseAssignment(prog1(), toky(), List.of(g1(), g2())));
        CrupdateRemedial crupdateRemedial = new CrupdateRemedial();
        crupdateRemedial.setId(remedial.getId());
        crupdateRemedial.setTitle(remedial.getTitle());
        crupdateRemedial.setCourseId(remedial.getCourseAssignment().getId());
        crupdateRemedial.setRemedialDate(remedial.getRemedialDate());
        var actualCreate = api.createOrUpdateRemedialInfos(crupdateRemedial);
        System.out.println(actualCreate);
        assertEquals("Remedial title", actualCreate.getTitle());
    }
}
