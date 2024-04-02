package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.List;
import java.util.function.Function;

@Component
public class WorkStudyStatusCase implements Function<List<WorkStudyStatus>, Integer> {
    @Override
    public Integer apply(List<WorkStudyStatus> workStudyStatuses) {
        if (workStudyStatuses == null) {
            return 0;
        }

        if (workStudyStatuses.size() == 2) {
            return 3;
        }
        return defineWorkStudyStatuses(workStudyStatuses.get(0));
    }

    private int defineWorkStudyStatuses(WorkStudyStatus status){
        return switch (status) {
            case TAKEN -> 1;
            case WORKING -> 2;
            default -> throw new BadRequestException("Unexpected type " + status);
        };
    }
}
