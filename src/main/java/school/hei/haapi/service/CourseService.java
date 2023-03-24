package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CoursesRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class CourseService {
    private final CoursesRepository coursesRepository;
    private final CourseManagerDao courseManagerDao;
    public List<Courses>  getCourses(int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);
        return coursesRepository.findAll(pageable).toList();
    }
    public List<Courses> getByFiltre(
            String code , String name, Integer credits, String teacher_first_name, String teacher_last_name, PageFromOne page, BoundedPageSize pageSize
    ){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "ref"));
        return courseManagerDao.findByFiltre(
                code, name, credits, teacher_first_name, teacher_last_name, pageable);
    }
}
