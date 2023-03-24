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

    public List<Course> getAllCoursesBy (PageFromOne page,BoundedPageSize pageSize,String teacherFirstName,String teacherLastName,String code,String name,Integer credits){
        Pageable pageable = PageRequest.of(page.getValue() -1 ,pageSize.getValue());
        if(teacherFirstName != null){
            return courseRepository.getCoursesByMainTeacherFirstName(teacherFirstName, pageable);
        }
        else if(teacherLastName != null){
            return courseRepository.getCoursesByMainTeacherLastName(teacherLastName, pageable);
        }
        else if(code != null){
            return courseRepository.getCoursesByCodeContainingIgnoreCase(code, pageable);
        }
        else if(name != null){
            return courseRepository.getCoursesByNameContainingIgnoreCase(name, pageable);
        }
        else if(credits != null){
            return courseRepository.getCoursesByCredits(credits, pageable);
        }
        return courseRepository.findAll();
    }

}
