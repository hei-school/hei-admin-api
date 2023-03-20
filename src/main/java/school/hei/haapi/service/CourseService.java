package school.hei.haapi.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository courseRepository;


}
