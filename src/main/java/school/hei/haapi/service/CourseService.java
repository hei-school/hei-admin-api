package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<StudentCourse> getByStudentIdAndStatus(String studentId, StudentCourse.CourseStatus status) {
        StudentCourse.CourseStatus newStatus = status==null?StudentCourse.CourseStatus.LINKED:status;
        return studentCourseRepository.getStudentCourseByStudentIdAndStatus(studentId,newStatus);
    }

    public List<Course> getCourses(
            PageFromOne page,
            BoundedPageSize pageSize,
            String name,
            String code,
            Integer credits,
            String teacherFirstName,
            Sort.Direction codeOrder
    ){
            if(codeOrder != null) {
                return repository.findCoursesWithParams(
                        name,
                        code,
                        teacherFirstName,
                        credits,
                        pageableCreator(page, pageSize, codeOrder, "code")
                );
            }
            else{
                return repository.findCoursesWithParams(
                        name,
                        code,
                        teacherFirstName,
                        credits,
                        pageableCreator(page, pageSize, null, null)
                );
                }
            }


    public List<StudentCourse> saveAllStudentCourses(String studentId, List<StudentCourse> toDomainStudentCourse) {
        return studentCourseRepository.saveAll(toDomainStudentCourse);
    }

    public StudentCourse getByStudentIdAndCourseId(String studentId, String courseId) {
        return studentCourseRepository.getStudentCourseByStudentIdAndCourseId(studentId,courseId);
    }

    private Pageable pageableCreator(PageFromOne page, BoundedPageSize pageSize, Sort.Direction order, String orderTarget){
        Pageable pageable = null;

        if(orderTarget != null && order != null){
            Sort sort = Sort.by(order, orderTarget);
            pageable = PageRequest.of(page.getValue()-1, pageSize.getValue(), sort);
        }
        else{
            pageable = PageRequest.of(page.getValue()-1, pageSize.getValue());
        }
        return pageable;
    }
}
