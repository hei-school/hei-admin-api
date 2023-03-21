package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.controller.response.UpdateStudentCourseStatusResponse;
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

    private final CourseRepository courseRepository;


    public List<Course> updateStatus(String student_id, List<UpdateStudentCourseStatusResponse> updateStudentCourseStatusResponse) {
        List<Course> allCourses = new ArrayList<Course>();
        List<StudentCourse> studentCourse = studentCourseRepository.findAllByStudent_id(student_id);
        if (studentCourse.size() > 0) {

            for(int i=0; i<updateStudentCourseStatusResponse.size(); i++){

                for(int k=0; k < studentCourse.size(); k++){
                    if(studentCourse.get(k).getCourse_id() == courseRepository.getById(updateStudentCourseStatusResponse.get(i).getCourse_id())){
                        studentCourse.get(k).setStatus(updateStudentCourseStatusResponse.get(i).getStatus());
                    }
                }

                    studentCourse.get(i).setCourse_id(courseService.getById(updateStudentCourseStatusResponse.get(i).getCourse_id()));
                    studentCourse.get(i).setStatus(updateStudentCourseStatusResponse.get(i).getStatus());
                    }
            }else {
                throw new NotFoundException("Student" + student_id + " not found");
            }
            studentCourseRepository.saveAll(studentCourse);

        for(int f=0; f < updateStudentCourseStatusResponse.size();f++){
            allCourses.add(courseService.getById(updateStudentCourseStatusResponse.get(f).getCourse_id()));
        }

            return allCourses;
        }

}
