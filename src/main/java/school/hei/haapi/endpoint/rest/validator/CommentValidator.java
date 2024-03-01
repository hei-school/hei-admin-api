package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateComment;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.Objects;
import java.util.function.Consumer;

@Component
public class CommentValidator implements Consumer<CreateComment> {
    @Override
    public void accept(CreateComment createComment) {
        if(Objects.isNull(createComment.getContent())){
            throw new BadRequestException("Cannot create a comment without content");
        }
    }
}
