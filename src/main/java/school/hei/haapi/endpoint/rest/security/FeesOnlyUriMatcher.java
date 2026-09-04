package school.hei.haapi.endpoint.rest.security;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class FeesOnlyUriMatcher {

    private static final Set<String> ALLOWED_STUDENT_ROUTE_SEGMENTS = Set.of("stats", "level");
    private static final Pattern STUDENT_ID = Pattern.compile("^[0-9a-fA-F-]{8,}$");

    public boolean isAllowed(String uri) {
        return uri.equals("/ping")
                || uri.equals("/whoami")
                || uri.equals("/health/db")
                || uri.startsWith("/authentication/")
                || uri.startsWith("/fees")
                || uri.startsWith("/feeTemplates")
                || uri.startsWith("/feeCreationJobs")
                || uri.startsWith("/mpbs")
                || uri.startsWith("/delay_penalty")
                || uri.startsWith("/admins")
                || uri.startsWith("/managers")
                || uri.equals("/students")
                || isStudentByIdOrAllowedSegment(uri)
                || isStudentFees(uri);
    }

    private boolean isStudentByIdOrAllowedSegment(String uri) {
        if (!uri.startsWith("/students/")) {
            return false;
        }
        String segment = uri.substring("/students/".length());
        if (STUDENT_ID.matcher(segment).matches()) {
            return true;
        }
        return Arrays.stream(uri.split("/")).anyMatch(ALLOWED_STUDENT_ROUTE_SEGMENTS::contains);
    }

    private boolean isStudentFees(String uri) {
        return uri.startsWith("/students/") && uri.contains("/fees");
    }
}