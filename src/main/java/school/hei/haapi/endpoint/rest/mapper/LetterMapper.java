package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Letter;
import school.hei.haapi.endpoint.rest.model.PagedLettersResponse;
import school.hei.haapi.model.User;
import school.hei.haapi.service.aws.FileService;

import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;

@Component
@AllArgsConstructor
public class LetterMapper {

  private final UserMapper userMapper;
  private final FileService fileService;

  public Letter toRest(school.hei.haapi.model.Letter domain) {
    String url =
            domain.getFilePath() != null
                    ? fileService.getPresignedUrl(domain.getFilePath(), ONE_DAY_DURATION_AS_LONG)
                    : null;

    return new Letter()
        .id(domain.getId())
        .description(domain.getDescription())
        .creationDatetime(domain.getCreationDatetime())
        .approvalDatetime(domain.getApprovalDatetime())
        .status(domain.getStatus())
        .ref(domain.getRef())
        .student(userMapper.toIdentifier(domain.getStudent()))
        .fileUrl(url);
  }

  public PagedLettersResponse toPagedRest(Page<school.hei.haapi.model.Letter> domain) {
    return new PagedLettersResponse()
        .pageNumber(domain.getNumber())
        .pageSize(domain.getSize())
        .data(domain.get().map(this::toRest).toList())
        .count((int) domain.getTotalElements())
        .hasPrevious(domain.hasPrevious());
  }
}
