package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
public final CourseRepository courseRepository;

 public Course getById(String id){return courseRepository.getById(id);}

 public List<Course> getByStudentId(String id){return courseRepository.getByStudentId(id);}


};
