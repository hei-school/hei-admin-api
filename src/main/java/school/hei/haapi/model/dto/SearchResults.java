package school.hei.haapi.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResults {
  private List<UserDto> students;
  private List<UserDto> teachers;
  private List<UserDto> managers;
  private List<UserDto> organisers;
  private List<UserDto> monitors;
  private List<UserDto> staffMembers;
}
