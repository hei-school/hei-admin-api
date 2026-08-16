package school.hei.haapi.integration.conf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;
import school.hei.haapi.service.aws.FileService;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

/** Stubs the third parties the application talks to, for the rows a test owns. */
public class TestMocks {

  public static void setUpS3Service(FileService fileService, User user) {
    when(fileService.getPresignedUrl(user.getRef(), 180L)).thenReturn(user.getRef());
    // doCallRealMethod, not when(...).thenCallRealMethod: the latter *invokes* getFileExtension, so
    // a second setUpS3Service call would run the real method on a null file
    doCallRealMethod().when(fileService).getFileExtension(any());
  }

  public static void setUpS3Service(FileService fileService, Student user) {
    when(fileService.getPresignedUrl(user.getRef(), 180L)).thenReturn(user.getRef());
    // doCallRealMethod, not when(...).thenCallRealMethod: the latter *invokes* getFileExtension, so
    // a second setUpS3Service call would run the real method on a null file
    doCallRealMethod().when(fileService).getFileExtension(any());
  }

  public static void setUpS3Service(FileService fileService, Teacher user) {
    when(fileService.getPresignedUrl(user.getRef(), 180L)).thenReturn(user.getRef());
  }

  public static void setUpS3Service(FileService fileService, Manager user) {
    when(fileService.getPresignedUrl(user.getRef(), 180L)).thenReturn(user.getRef());
  }

  public static void setUpEventBridge(EventBridgeClient eventBridgeClient) {
    when(eventBridgeClient.putEvents((PutEventsRequest) any()))
        .thenReturn(PutEventsResponse.builder().build());
  }

  /** Answers the ownCloud share-creation call with a canned OCS payload. */
  public static void setUpRestTemplate(RestTemplate restTemplateMock) {
    when(restTemplateMock.exchange(any(), eq(POST), any(), eq(String.class)))
        .thenReturn(ResponseEntity.ok(OCS_SHARE_RESPONSE));
  }

  private static final String OCS_SHARE_RESPONSE =
      """
      {
        "ocs": {
          "meta": {
            "status": "ok",
            "statuscode": 100,
            "message": null,
            "totalitems": "",
            "itemsperpage": ""
          },
          "data": {
            "id": "130",
            "share_type": 3,
            "uid_owner": "ilo",
            "displayname_owner": "john",
            "permissions": 15,
            "stime": 1719915415,
            "parent": null,
            "expiration": "2024-07-03 00:00:00",
            "token": "vDq5Er8qizxQOEB",
            "uid_file_owner": "john",
            "displayname_file_owner": "john",
            "additional_info_owner": null,
            "additional_info_file_owner": null,
            "path": "/Test-api",
            "mimetype": "httpd/unix-directory",
            "storage_id": "object::user:john",
            "storage": 66,
            "item_type": "folder",
            "item_source": 22602,
            "file_source": 22602,
            "file_parent": 22570,
            "file_target": "/Test-api",
            "name": "test",
            "url": "https://owncloud.example.com/s/vDq5Er8qizxQOEB",
            "mail_send": 0,
            "attributes": null
          }
        }
      }""";
}
