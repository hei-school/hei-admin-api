package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<Course> getAllCourse (){
        return courseRepository.findAll();
    }
    public List<Course> getAllCoursesByTeacherFirstName (String teacherFirstName){
        return courseRepository.getCoursesByMainTeacher_FirstName(teacherFirstName);
    }
    public List<Course> getAllCourseByTeacherLastName (String teacherLastName){
        return courseRepository.getCoursesByMainTeacher_LastName(teacherLastName);
    }

    public List<Course> getAllCourseByCode (String code){
        return courseRepository.getCoursesByCodeIgnoreCase(code);
    }

    public List<Course> getAllCourseByName(String name){
        return courseRepository.getCourseByNameIgnoreCase(name);
    }

    public List<Course> getAllCourseByCredits(Integer credits){
        return courseRepository.getCoursesByCredits(credits);
    }

}
