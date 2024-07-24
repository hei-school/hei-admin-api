package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.GroupAttender;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.GroupAttenderRepository;
import school.hei.haapi.repository.dao.GroupAttenderDao;

@Service
@AllArgsConstructor
public class GroupAttenderService {
  private final GroupAttenderRepository groupAttenderRepository;
  private final GroupAttenderDao groupAttenderDao;

  public List<GroupAttender> getGroupAttenderByGroupIdAndStudentCriteria(
      String groupId,
      String studentRef,
      String studentLastname,
      String studentFirstname,
      PageFromOne page,
      BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());

    return groupAttenderDao.findAllByGroupIdAndStudentCriteria(
        groupId, studentRef, studentLastname, studentFirstname, pageable);
  }
}
