package school.hei.haapi.endpoint.rest.mapper;

import school.hei.haapi.model.Course;

public class CourseMapper {
    public Course ToDomain(Course course){
        Course newCourse = new Course();
        newCourse.setId(course.getId());
        newCourse.setCode(course.getCode());
        newCourse.setName(course.getName());
        newCourse.setCredits(course.getCredits());
        newCourse.setMain_teacher(course.getMain_teacher());

        return newCourse;
    }

}