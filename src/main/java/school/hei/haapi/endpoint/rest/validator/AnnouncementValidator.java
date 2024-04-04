package school.hei.haapi.endpoint.rest.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateAnnouncement;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.Objects;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class AnnouncementValidator implements Consumer<CreateAnnouncement> {

    @Override
    public void accept(CreateAnnouncement createAnnouncement) {
        if(Objects.isNull(createAnnouncement.getTitle())){
            throw new BadRequestException("Title is required");
        }
        if(Objects.isNull(createAnnouncement.getContent())){
            throw new BadRequestException("Content is required");
        }
        if(Objects.isNull(createAnnouncement.getAuthorId())){
            throw new BadRequestException("Author id is required");
        }
        if(Objects.isNull(createAnnouncement.getScope())){
            throw new BadRequestException("Scope is required");
        }
     //   if(Scope.fromValue(getPrincipal().getRole()) == TEACHER && createAnnouncement.getScope() == MANAGER){
       //     throw new ForbiddenException();
       // }
    }
}
