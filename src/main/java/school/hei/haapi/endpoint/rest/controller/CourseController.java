package school.hei.haapi.endpoint.rest.controller;

import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.service.CourseService;

public class CourseController {
    private CourseService courseService;
    private CourseMapper courseMapper;


    public CourseMapper getCourseMapper() {
        return courseMapper;
    }
}
