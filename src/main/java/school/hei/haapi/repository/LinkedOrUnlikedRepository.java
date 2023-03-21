package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public Interface LinkedOrUnlikedRepository extends JpaRepository <LinkedOrUnliked,String>{
    @Query(value = "insert into ")
  List<Payment> getByCourseId(@Param("course_id") String CourseId);    
}
