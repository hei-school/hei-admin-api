package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;

   public Course findById(String courseId){
        return courseRepository.getById(courseId);
    }

    public List<Course> findAll(){
       return courseRepository.findAll();
    }
    public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize){
        if(page==null){
            List<Course> allCourses = new ArrayList<>();
            if(page==null && pageSize==null){
                Pageable pageable = PageRequest.of(0, 15);
                allCourses = repository.findAll(pageable).toList();
            }
            else if(page==null){
                Pageable pageable = PageRequest.of(1, pageSize.getValue());
                return repository.findAll(pageable).toList();
                allCourses = repository.findAll(pageable).toList();
            }
            if(pageSize==null){
        else if(pageSize==null){
                    Pageable pageable = PageRequest.of(page.getValue()-1, 15);
                    return repository.findAll(pageable).toList();
                    allCourses = repository.findAll(pageable).toList();
                }
                else{
                    Pageable pageable = PageRequest.of(page.getValue()-1, pageSize.getValue());
                    return repository.findAll(pageable).toList();
                    allCourses = repository.findAll(pageable).toList();
                }
                return allCourses;
            }
        }
}
}
