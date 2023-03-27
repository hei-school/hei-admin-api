package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Group;
import school.hei.haapi.repository.GroupRepository;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository repository;

  public Group getById(String groupId) {
    return repository.getById(groupId);
  }

  public List<Group> getAll() {
    return repository.findAll();
  }

  public List<Group> saveAll(List<Group> groups) {
    return repository.saveAll(groups);
  }
}
