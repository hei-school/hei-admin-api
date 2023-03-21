package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.BadRequestException;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;

import java.util.ArrayList;
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

    public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize){
        List<Course> allCourses = new ArrayList<>();
        if(page==null && pageSize==null){
            Pageable pageable = PageRequest.of(0, 15);
            allCourses = repository.findAll(pageable).toList();
        }
        else if(page==null){
            Pageable pageable = PageRequest.of(1, pageSize.getValue());
            allCourses = repository.findAll(pageable).toList();
        }
        else if(pageSize==null){
            Pageable pageable = PageRequest.of(page.getValue()-1, 15);
            allCourses = repository.findAll(pageable).toList();
        }
        else{
            Pageable pageable = PageRequest.of(page.getValue()-1, pageSize.getValue());
            allCourses = repository.findAll(pageable).toList();
        }
        return allCourses;
    }

        public List<StudentCourse> getByStudentIdAndStatus(String studentId, StudentCourse.CourseStatus status) {
        return studentCourseRepository.getStudentCourseByStudentIdAndStatus(studentId,status);

    }
}
