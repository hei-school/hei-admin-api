package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;
    public List<Course> getCourses(String code, String name, Integer credits, String teacherFirstName, String teacherLastName) {
        List<Course> courses = courseRepository.findAll();

        if (code != null) {
            courses = courses.stream()
                    .filter(course -> course.getCode().contains(code))
                    .collect(Collectors.toList());
        }

        if (name != null) {
            courses = courses.stream()
                    .filter(course -> course.getName().contains(name))
                    .collect(Collectors.toList());
        }

        if (credits != null) {
            courses = courses.stream()
                    .filter(course -> course.getCredits() == credits)
                    .collect(Collectors.toList());
        }

        if (teacherFirstName != null) {
            courses = courses.stream()
                    .filter(course -> course.getMainTeacher().getFirstName().contains(teacherFirstName))
                    .collect(Collectors.toList());
        }

        if (teacherLastName != null) {
            courses = courses.stream()
                    .filter(course -> course.getMainTeacher().getLastName().contains(teacherLastName))
                    .collect(Collectors.toList());
        }

        return courses;
    }
}
