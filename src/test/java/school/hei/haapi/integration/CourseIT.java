package school.hei.haapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import school.hei.haapi.endpoint.rest.controller.CourseController;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CourseService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.ENABLED;

@WebMvcTest(CourseController.class)
public class CourseIT{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    public static User teacher1() {
        User teacher = new User();
        teacher.setId("teacher1_id");
        teacher.setFirstName("One");
        teacher.setLastName("Teacher");
        teacher.setEmail("test+teacher1@hei.school");
        teacher.setRef("TCR21001");
        teacher.setPhone("0322411125");
        teacher.setStatus(ENABLED);
        teacher.setSex(M);
        teacher.setRole(User.Role.TEACHER);
        teacher.setBirthDate(LocalDate.parse("1990-01-01"));
        teacher.setEntranceDatetime(Instant.parse("2021-10-08T08:27:24.00Z"));
        teacher.setAddress("Adr 3");
        return teacher;
    }

    public static User teacher2() {
        User teacher = new User();
        teacher.setId("teacher2_id");
        teacher.setFirstName("Two");
        teacher.setLastName("Teacher");
        teacher.setEmail("test+teacher2@hei.school");
        teacher.setRef("TCR21002");
        teacher.setPhone("0322411126");
        teacher.setStatus(ENABLED);
        teacher.setSex(F);
        teacher.setRole(User.Role.TEACHER);
        teacher.setBirthDate(LocalDate.parse("1990-01-02"));
        teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
        teacher.setAddress("Adr 4");
        return teacher;
    }
    public static User teacher3() {
        User teacher = new User();
        teacher.setId("teacher3_id");
        teacher.setFirstName("Three");
        teacher.setLastName("Teach");
        teacher.setEmail("test+teacher3@hei.school");
        teacher.setRef("TCR21003");
        teacher.setPhone("0322411126");
        teacher.setStatus(ENABLED);
        teacher.setSex(M);
        teacher.setRole(User.Role.TEACHER);
        teacher.setBirthDate(LocalDate.parse("1990-01-02"));
        teacher.setEntranceDatetime(Instant.parse("2021-10-09T08:28:24Z"));
        teacher.setAddress("Adr 4");
        return teacher;
    }

    @Test
    public void testGetCourses() throws Exception {
        // créer la liste de cours à renvoyer par le mock service
        List<Course> courses = new ArrayList<>(Arrays.asList(
                new Course("1","PROG1","Algorithmique",6,20,teacher1()),
                new Course("2","PROG3","P.O.O avancée",7,20,teacher2()),
                new Course("3","WEB1","Interface web",4,20,teacher3())
        ));

        // simuler l'appel au service pour récupérer la liste de tous les cours
        when(courseService.getAllCourses()).thenReturn(courses);

        // effectuer une requête GET avec les paramètres de tri
        mockMvc.perform(get("/courses")
                        .param("creditsOrder", "DESC")
                        .param("codeOrder", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("P.O.O avancée"))
                .andExpect(jsonPath("$[1].code").value("Interface web"))
                .andExpect(jsonPath("$[2].code").value("Algorithmique"))
                .andExpect(jsonPath("$[0].credits").value(4))
                .andExpect(jsonPath("$[1].credits").value(6))
                .andExpect(jsonPath("$[2].credits").value(7));
    }
}
