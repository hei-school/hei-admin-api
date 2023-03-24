package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;

    private final CourseManagerDao courseManagerDao;
    private final StudentCourseRepository studentCourseRepository;
    public List<Course> saveAll(List<Course> courses) {
        return repository.saveAll(courses);
    }

    public Course getById(String courseId){return repository.getById(courseId);}

    public List<StudentCourse> getByStudentIdAndStatus(String studentId, StudentCourse.CourseStatus status) {
        StudentCourse.CourseStatus newStatus = status==null?StudentCourse.CourseStatus.LINKED:status;
        return studentCourseRepository.getStudentCourseByStudentIdAndStatus(studentId,newStatus);
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

    public List<StudentCourse> saveAllStudentCourses(String studentId, List<StudentCourse> toDomainStudentCourse) {
        return studentCourseRepository.saveAll(toDomainStudentCourse);
    }

    public List<StudentCourse> getCoursesByStudentIdAndCourseId(String studentId, String courseId) {
        return studentCourseRepository.getStudentCourseByStudentIdAndCourseId(studentId,courseId);
    }

}
