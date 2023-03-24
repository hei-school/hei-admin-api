package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.Comparator;
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
                    .filter(course -> course.getCode().toLowerCase().contains(code.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (name != null) {
            courses = courses.stream()
                    .filter(course -> course.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (credits != null) {
            courses = courses.stream()
                    .filter(course -> course.getCredits() == credits)
                    .collect(Collectors.toList());
        }

        if (teacherFirstName != null) {
            courses = courses.stream()
                    .filter(course -> course.getMainTeacher().getFirstName().toLowerCase().contains(teacherFirstName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (teacherLastName != null) {
            courses = courses.stream()
                    .filter(course -> course.getMainTeacher().getLastName().toLowerCase().contains(teacherLastName.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (credits != null) {
            Comparator<Course> comparator = Comparator.comparing(Course::getCredits);
            if (credits.equals("DESC")) {
                comparator = comparator.reversed();
            }
            courses = courses.stream().sorted(comparator).collect(Collectors.toList());
        }

        if (code != null) {
            Comparator<Course> comparator = Comparator.comparing(Course::getCode);
            if (code.equals("DESC")) {
                comparator = comparator.reversed();
            }
            courses = courses.stream().sorted(comparator).collect(Collectors.toList());
        }
        return courses;
    }
}
