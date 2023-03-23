package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.controller.response.UpdateStudentCourseStatusResponse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentCourseService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseService courseService;

        public List<Course> updateCoursesStatuses(String student_id, List<UpdateStudentCourseStatusResponse> updateStudentCourses){
            if(studentCourseRepository.findById(student_id).isEmpty()){
                throw new NotFoundException("Student" + student_id + " not found");
            }
            else {
                List<Course> updatedCourses = new ArrayList<>();
                for(UpdateStudentCourseStatusResponse course : updateStudentCourses) {
                    StudentCourse studentCourse = studentCourseRepository.findByCourse_IdAndStudent_Id(course.getCourse_id(),student_id);
                    studentCourse.setStatus(course.getStatus());
                    studentCourseRepository.save(studentCourse);
                    updatedCourses.add(courseService.getById(course.getCourse_id()));
                }
                return updatedCourses;
            }
        }

}
