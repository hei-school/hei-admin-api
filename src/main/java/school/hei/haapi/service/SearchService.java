package school.hei.haapi.service;

import java.util.List;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.SearchResults;
import school.hei.haapi.model.dto.UserDto;
import school.hei.haapi.repository.UserRepository;

@Service
public class SearchService {

  private final UserRepository userRepository;

  public SearchService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public SearchResults searchAll(String search) {
    List<User> allUsers = userRepository.searchUsers(search);

    SearchResults results = new SearchResults();
    results.setStudents(filterAndConvertByRole(allUsers, User.Role.STUDENT));
    results.setTeachers(filterAndConvertByRole(allUsers, User.Role.TEACHER));
    results.setManagers(filterAndConvertByRole(allUsers, User.Role.MANAGER));
    results.setOrganizer(filterAndConvertByRole(allUsers, User.Role.ORGANIZER));
    results.setMonitor(filterAndConvertByRole(allUsers, User.Role.MONITOR));
    results.setStaff(filterAndConvertByRole(allUsers, User.Role.STAFF_MEMBER));

    return results;
  }

  private List<UserDto> filterAndConvertByRole(List<User> users, User.Role role) {
    return users.stream().filter(user -> role.equals(user.getRole())).map(UserDto::from).toList();
  }
}
