package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseManagerDao courseManagerDao;

    public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize){
        if (page == null){
            page = new PageFromOne(1);
        }
        if (pageSize == null){
            pageSize = new BoundedPageSize(15);
        }

        Pageable pageable = PageRequest.of(
                page.getValue() -1,
                pageSize.getValue());
        return courseRepository.findAll(pageable).getContent();
    }

    public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize,String code,String name,Integer credits,String teacher_first_name,String teacher_last_name,String creditsOrder,String codeOrder){
        if (creditsOrder.length()>0 && !creditsOrder.equals("ASC") && !creditsOrder.equals("DESC"))
            throw new BadRequestException("credits parameter is different of ASC and DESC");
        if (codeOrder.length()>0 && !codeOrder.equals("ASC") && !codeOrder.equals("DESC"))
            throw new BadRequestException("code parameter is different of ASC and DESC");
        if(page==null){
            PageFromOne defaultPage = new PageFromOne(1);
            page = defaultPage;
        }
        if(pageSize==null){
            BoundedPageSize defaultPageSize = new BoundedPageSize(15);
            pageSize = defaultPageSize;
        }
        Pageable pageable = PageRequest.of(page.getValue()-1, pageSize.getValue());
        return courseManagerDao.findByCriteria(
                code, name, credits, teacher_first_name, teacher_last_name, creditsOrder, codeOrder, pageable);
    }
}
