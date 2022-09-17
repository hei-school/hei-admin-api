package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Event;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Place;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.PlaceService;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class EventMapper {
    private final PlaceService placeService;
    private final UserService userService;
    private final CourseService courseService;

    public Event toRest(school.hei.haapi.model.Event eventDomain){
        return new Event()
                .id(eventDomain.getId())
                .supervisorId(eventDomain.getSupervisor().getId())
                .placeId(eventDomain.getPlace().getId())
                .startingTime(eventDomain.getStartingDateTime())
                .endingTime(eventDomain.getEndingDateTime())
                .type(eventDomain.getEventType())
                .title(eventDomain.getTitle())
                .courseId(eventDomain.getCourse().getId());
    }

    public school.hei.haapi.model.Event toDomain(Event eventRest){
        //throw error if entity with a specified id does not exist
        User teacherOrManager = getTeacherOrManager(eventRest.getSupervisorId());
        Course course = getCourse(eventRest.getCourseId());
        Place place = getPlace(eventRest.getPlaceId());

        return school.hei.haapi.model.Event.builder()
                .id(eventRest.getId())
                .supervisor(teacherOrManager)
                .course(course)
                .eventType(eventRest.getType())
                .startingDateTime(eventRest.getStartingTime())
                .endingDateTime(eventRest.getEndingTime())
                .title(eventRest.getTitle())
                .place(place)
                .build();
    }

    private Place getPlace(String placeId) {
        Place place = placeService.getPlaceById(placeId);
        if (place == null) {
            throw new NotFoundException("place.id=" + placeId + " is not found");
        }
        return place;
    }

    private Course getCourse(String courseId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new NotFoundException("course.id=" + courseId + " is not found");
        }
        return course;
    }

    private User getTeacherOrManager(String supervisorId) {
        User teacherOrManager = userService.getById(supervisorId);
        if (teacherOrManager == null) {
            throw new NotFoundException("supervisor with id=" + supervisorId + " is not found");
        }
        return teacherOrManager;
    }
}
