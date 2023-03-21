package school.hei.haapi.service;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> dcbf6c0e198524dab51d5515d0b92cabb24a51cc
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

<<<<<<< HEAD
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository repository;

  public Course getById(String groupId) {
    return repository.getById(groupId);
  }

  public List<Course> getAll() {
    return repository.findAll();
  }

  public List<Course> saveAll(List<Course> groups) {
    return repository.saveAll(groups);
  }

  public List<Course> getCoursesByStudentId(String studentId){
    return repository.getByStudentId(studentId);
=======
@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;

  public List<Course> crupdateCourses(List<Course> courses){
    return repository.saveAll(courses);
>>>>>>> dcbf6c0e198524dab51d5515d0b92cabb24a51cc
  }
  
  public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of(
            page.getValue() - 1, pageSize.getValue());
    return repository.findAll(pageable).getContent();
  }

  public List<Course> crupdateCourses(List<Course> courses){
    return repository.saveAll(courses);
  }
}