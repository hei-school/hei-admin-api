package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.UserDtoMapper;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.SearchResults;
import school.hei.haapi.model.dto.UserDto;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class SearchService {

  private final UserRepository userRepository;
  private final UserDtoMapper userDtoMapper;

  public SearchResults searchAll(String search) {
    List<User> allUsers = userRepository.searchUsers(search == null ? "" : search.trim());

    SearchResults results = new SearchResults();
    results.setStudents(filterAndConvertByRole(allUsers, User.Role.STUDENT));
    results.setTeachers(filterAndConvertByRole(allUsers, User.Role.TEACHER));
    results.setManagers(filterAndConvertByRole(allUsers, User.Role.MANAGER));
    results.setOrganisers(filterAndConvertByRole(allUsers, User.Role.ORGANIZER));
    results.setMonitors(filterAndConvertByRole(allUsers, User.Role.MONITOR));
    results.setStaffMembers(filterAndConvertByRole(allUsers, User.Role.STAFF_MEMBER));

    return results;
  }

  private List<UserDto> filterAndConvertByRole(List<User> users, User.Role role) {
    return users.stream()
        .filter(user -> role.equals(user.getRole()))
        .map(userDtoMapper::toDto)
        .toList();
  }
}
