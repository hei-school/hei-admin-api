package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.response.UpdateCoursesToStudent;
import school.hei.haapi.model.StudentCourses;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.StudentCoursesRepository;
import school.hei.haapi.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class StudentCoursesService {
    private final StudentCoursesRepository studentCoursesRepository;
    private final UserRepository userRepository;
    public List<StudentCourses> updateCourseStatusToStudent(String id, List<UpdateCoursesToStudent> toUpdate){
        User student = userRepository.findById(id).get();
        List<StudentCourses> courses = studentCoursesRepository.findByStudent(student);

        for (StudentCourses course:courses){
            for(UpdateCoursesToStudent updateInfo: toUpdate){
                if(Objects.equals(course.getCourse().getId(), updateInfo.getCourse_id())){
                    course.getCourse().setStatus(updateInfo.getStatus());
                }
            }
        }

        return studentCoursesRepository.saveAll(courses);
    }

    public List<StudentCourses> getSpecificStudentCourses(String id){
        User student = userRepository.findById(id).get();

        return studentCoursesRepository.findByStudent(student);
    }
}
