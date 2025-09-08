package school.hei.haapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.ResultSummary;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Status.ALUMNI;

@Service
@AllArgsConstructor
public class StudentElevationService {
    private final GradeResultService gradeResultService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public List<Map<String, Object>> elevateStudentByUserIdOrGroupId(List<String> ids) {
        List<Map<String, Object>> results = new ArrayList<>();
        Set<String> processedUserIds = new HashSet<>();
        for(String id : ids) {
            try {
                User user = userService.findById(id);
                if (user != null && !processedUserIds.contains(user.getId())) {
                    processedUserIds.add(user.getId());
                    results.add(elevateStudentToAlumni(user));
                }
                continue;
            } catch (NotFoundException ignored) {

            }
            List<User> users = userRepository.findAllRemainingStudentsByGroupIds(List.of(id), Pageable.unpaged());
            if (users == null || users.isEmpty()) {
                Map<String, Object> notFound = new HashMap<>();
                notFound.put("id", id);
                notFound.put("elevated", false);
                notFound.put("error", "GroupId not found or group contains no students");
                results.add(notFound);
                continue;
            }
            for (User user : users) {
                if(user == null) continue;
                if(processedUserIds.contains(user.getId())) continue;
                processedUserIds.add(user.getId());
                results.add(elevateStudentToAlumni(user));
            }
        }
        return results;
    }

    private Map<String, Object> elevateStudentToAlumni(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        try{
            if(user.getRole() != STUDENT) {
                result.put("elevated", false);
                result.put("error", "Not a student");
                return result;
            }
            ResultSummary summary = gradeResultService.getStudentResultSummary(user.getId());
            BigDecimal obtainedCredits = Optional.ofNullable(summary).map(ResultSummary::getObtainedCredits).orElse(BigDecimal.ZERO);
            final short licenseCredit = 180;
            if(obtainedCredits.compareTo(BigDecimal.valueOf(licenseCredit)) < 0) {
                result.put("elevated", false);
                result.put("error", "Insufficient credits");
                return result;
            }
            user.setStatus(ALUMNI);
            userRepository.save(user);
            result.put("elevated", true);
            result.put("error", null);
            return result;
        } catch (Exception e) {
            result.put("elevated", false);
            result.put("error", "Unexpected error: " + e.getMessage());
            return result;
        }
    }
}
