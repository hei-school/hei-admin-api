package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.BoundedPageSize;
import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<Course> getAllCourse (){
        return courseRepository.findAll();
    }
    public List<Course> getAllCoursesByTeacherFirstName (PageFromOne page,BoundedPageSize pageSize,String teacherFirstName){
        Pageable pageable = PageRequest.of(page.getValue() -1 ,pageSize.getValue());

        return courseRepository.getCoursesByMainTeacherFirstName(teacherFirstName, pageable);
    }
    public List<Course> getAllCourseByTeacherLastName (PageFromOne page,BoundedPageSize pageSize,String teacherLastName){
        Pageable pageable = PageRequest.of(page.getValue() -1 ,pageSize.getValue());
        return courseRepository.getCoursesByMainTeacherLastName(teacherLastName, pageable);
    }

    public List<Course> getAllCourseByCode (PageFromOne page,BoundedPageSize pageSize,String code){
        Pageable pageable = PageRequest.of(page.getValue() -1 ,pageSize.getValue());
        return courseRepository.getCoursesByCodeContainingIgnoreCase(code, pageable);
    }

    public List<Course> getAllCourseByName(PageFromOne page,BoundedPageSize pageSize,String name){
        Pageable pageable = PageRequest.of(page.getValue() -1 ,pageSize.getValue());
        return courseRepository.getCoursesByNameContainingIgnoreCase(name, pageable);
    }

    public List<Course> getAllCourseByCredits(PageFromOne page,BoundedPageSize pageSize,Integer credits){
        Pageable pageable = PageRequest.of(page.getValue() -1 ,pageSize.getValue());
        return courseRepository.getCoursesByCredits(credits, pageable);
    }

}
