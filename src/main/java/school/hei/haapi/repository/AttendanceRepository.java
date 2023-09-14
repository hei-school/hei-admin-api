package school.hei.haapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.haapi.model.StudentAttendance;

@Repository
public interface AttendanceRepository extends JpaRepository<StudentAttendance, String> {
}
