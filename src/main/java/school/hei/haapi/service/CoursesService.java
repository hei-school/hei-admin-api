package school.hei.haapi.service;


import lombok.AllArgsConstructor;
import org.hibernate.mapping.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Courses;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CoursesRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class CoursesService {
    private final CoursesRepository repository;

    public Courses getById(String coursesId){
        return repository.getById(coursesId);
    }

    public List<Courses> getByCode(
            String code,
            PageFromOne page,
            BoundedPageSize pageSize
    ){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "code"));
        return repository.findByCodeContainingIgnoreCase(
                code,pageable
        );
    }

    public Courses save(Courses courses){
        coursesValidator.accept(courses);
        return repository.save(courses);
    }
}