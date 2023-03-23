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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
public class CourseIT{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    public static User teacher1() {
        User teacher = new User();
        teacher.setId("1");
        teacher.setFirstName("Tokimahery");
        teacher.setLastName("Ramarozaka");
        teacher.setRole(User.Role.TEACHER);
        return teacher;
    }

    public static User teacher2() {
        User teacher = new User();
        teacher.setId("2");
        teacher.setFirstName("Ryan");
        teacher.setLastName("Andriamahery");
        teacher.setRole(User.Role.TEACHER);
        return teacher;
    }
    @Test
    public void testGetCourses() throws Exception {
        // créer la liste de cours à renvoyer par le mock service
        List<Course> courses = new ArrayList<>(Arrays.asList(
                new Course("1","PROG1","Algorithmique",6,20,teacher1()),
                new Course("2","PROG3","P.O.O avancée",7,20,teacher2()),
                new Course("3","WEB1","Interface web",4,20,teacher1())
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
