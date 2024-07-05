package school.hei.haapi.service.utils;

import school.hei.haapi.endpoint.rest.model.OwnCloudPermission;

import java.util.*;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.*;

public class OwnCloudUtils {

    public static List<Integer> findPermissionKey(int value, List<Integer> key, List<Integer> res) {
        if (key.contains(value)) {
            res.add(value);
            return res;
        } else {
            List<Integer> currentKeyArray = new ArrayList<>();
            for (int k : key) {
                if (k < value) {
                    currentKeyArray.add(k);
                }
            }
            int currentValue = currentKeyArray.removeLast();
            res.add(currentValue);
            return findPermissionKey(value - currentValue, currentKeyArray, res);
        }
    }

    public static EnumSet<OwnCloudPermission> findPermissionValue(int value) {
        List<Integer> permissionKeys = findPermissionKey(value, Arrays.asList(1, 2, 4, 8, 16), new ArrayList<>());
        Map<Integer, OwnCloudPermission> permissionValue = new HashMap<>();
        permissionValue.put(1, READ);
        permissionValue.put(2, UPDATE);
        permissionValue.put(4, CREATE);
        permissionValue.put(8, DELETE);
        permissionValue.put(16, SHARE);

        List<OwnCloudPermission> res = new ArrayList<>();
        for (int k : permissionKeys) {
            res.add(permissionValue.get(k));
        }

        return EnumSet.copyOf(res);
    }

    public static String getBasicAuth(String username, String password) {
              return "Basic " + new String(Base64.getEncoder().encode(String.format("%s : %s", username, password).getBytes(US_ASCII)));
    };
}
