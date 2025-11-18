package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.GradeImportEvent;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;

import java.util.function.Consumer;

@Service
@AllArgsConstructor
@Slf4j
public class GradeImportEventService implements Consumer<GradeImportEvent> {
    private final GradeService gradeService;
    private final UserService userService;
    private final GradeMapper gradeMapper;

    @Override
    public void accept(GradeImportEvent event) {
        var coordinatorUser = userService.getByEmail(event.getCoordinatorEmail());
        try{
            gradeService.createParticipantGrade(gradeMapper.toDomain());
        }

    }
}
