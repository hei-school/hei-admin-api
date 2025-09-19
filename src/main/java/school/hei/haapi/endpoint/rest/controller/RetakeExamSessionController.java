package school.hei.haapi.endpoint.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.RetakeExamSessionMapper;
import school.hei.haapi.endpoint.rest.model.RetakeExamSession;
import school.hei.haapi.service.RetakeExamSessionService;

import java.util.List;

@RestController
@RequestMapping
public class RetakeExamSessionController {
    @Autowired
    RetakeExamSessionService retakeExamSessionService;
    @Autowired
    RetakeExamSessionMapper retakeExamSessionMapper;
    @GetMapping("/retake_exam_sessions")
    public List<RetakeExamSession> getRetakeExamSessions(){
        return retakeExamSessionMapper.toRestList(retakeExamSessionService.getRetakeExamSessions());
    }
}
