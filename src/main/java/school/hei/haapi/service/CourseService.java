package school.hei.haapi.service;

import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException;

public class CourseService {
    @Service

        @Autowired
        private CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // autres méthodes du service

        public Course updateCourse(String courseId, Course courseDetails) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

            // Mise à jour des propriétés de l'objet Course
            course.setCode(courseDetails.getCode());
            course.setName(courseDetails.getName());
            course.setCredits(courseDetails.getCredits());
            course.setTotal_hours(courseDetails.getTotal_hours());
            course.setMain_teacher(courseDetails.getMain_teacher());

            return courseRepository.save(course);
        }
}

