package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

import java.util.List;

@AllArgsConstructor
@Service
public class CourseService {

    private final CourseRepository repository;

    private final CourseManagerDao courseManagerDao;

    public List<Course> getByRole(User.Role role, PageFromOne page, BoundedPageSize pageSize){
        return getByCriteria(role , " " , " ", page, pageSize, "" , "");
    }

    public List<Course> getByCriteria(
            User.Role role , String firstName , String lastName , PageFromOne page , BoundedPageSize pageSize ,
            String codeOrder , String creditsOrder
    ){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue());

        return courseManagerDao.findByCriteria(role , firstName , lastName , pageable);
    }
}
