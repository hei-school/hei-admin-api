package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.BadRequestException;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final StudentCourseRepository studentCourseRepository;
    public List<Course> saveAll(List<Course> courses) {
        return repository.saveAll(courses);
    }

    public Course getById(String courseId){return repository.getById(courseId);}

    public List<StudentCourse> getByStudentIdAndStatus(String studentId, StudentCourse.CourseStatus status) {
        StudentCourse.CourseStatus newStatus = status==null?StudentCourse.CourseStatus.LINKED:status;
        return studentCourseRepository.getStudentCourseByStudentIdAndStatus(studentId,newStatus);
    }
}