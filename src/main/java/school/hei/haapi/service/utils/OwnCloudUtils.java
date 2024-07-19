package school.hei.haapi.service.utils;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.CREATE;
import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.DELETE;
import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.READ;
import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.SHARE;
import static school.hei.haapi.endpoint.rest.model.OwnCloudPermission.UPDATE;

import java.util.*;
import school.hei.haapi.endpoint.rest.model.OwnCloudPermission;

public class OwnCloudUtils {

  private static final Map<Integer, OwnCloudPermission> permissionMap =
      Map.of(
          1, READ,
          2, UPDATE,
          4, CREATE,
          8, DELETE,
          16, SHARE);

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

  public static EnumSet<OwnCloudPermission> findOwnCloudPermissionsFromValue(int value) {
    List<Integer> permissionKeys =
        findPermissionKey(
            value,
            Arrays.asList(permissionMap.keySet().toArray(new Integer[0])),
            new ArrayList<>());

    List<OwnCloudPermission> res = new ArrayList<>();
    for (int k : permissionKeys) {
      res.add(permissionMap.get(k));
    }

    return EnumSet.copyOf(res);
  }

  public static String getBasicAuth(String username, String password) {
    return "Basic "
        + new String(
            Base64.getEncoder()
                .encode(String.format("%s : %s", username, password).getBytes(US_ASCII)));
  }
  ;

  public static Integer getPermissionValueFromOwnCloudPermission(
      List<OwnCloudPermission> ownCloudPermissions) {
    return permissionMap.entrySet().stream()
        .filter(entry -> ownCloudPermissions.contains(entry.getValue()))
        .mapToInt(Map.Entry::getKey)
        .sum();
  }

  public static String generateRandomPassword() {
    return UUID.randomUUID().toString();
  }
}
