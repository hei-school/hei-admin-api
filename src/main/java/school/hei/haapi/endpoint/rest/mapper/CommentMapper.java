package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateComment;
import school.hei.haapi.endpoint.rest.model.Observer;
import school.hei.haapi.model.Comment;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.UserService;

@Component
@AllArgsConstructor
public class CommentMapper {

    private final UserService userService;
    private final UserMapper userMapper;

    public Comment toDomain(CreateComment rest){
        User subject = userService.findById(rest.getStudentId());
        User observer = userService.findById(rest.getObserverId());

        return Comment.builder()
               .id(rest.getId())
                .subject(subject)
                .observer(observer)
                .content(rest.getContent())
                .build();
    }

    public school.hei.haapi.endpoint.rest.model.Comment toRest(Comment domain){
        return new school.hei.haapi.endpoint.rest.model.Comment()
                .content(domain.getContent())
                .creationDatetime(domain.getCreationDatetime())
                .id(domain.getId())
                .subject(userMapper.toRestStudent(domain.getSubject()))
                .observer(toRest(domain.getObserver()));

    }

    public Observer toRest(User domain){
        return new Observer()
                .id(domain.getId())
                .ref(domain.getRef())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .role(toRestRoleEnum(domain.getRole()))
                .profilePicture(domain.getProfilePictureKey());

    }

    public Observer.RoleEnum toRestRoleEnum(User.Role role){
        return switch (role){
            case MANAGER -> Observer.RoleEnum.MANAGER;
            case TEACHER -> Observer.RoleEnum.TEACHER;
            default -> throw new BadRequestException("Unexpected type " + role);
        };
    }

}
