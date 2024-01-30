package school.hei.haapi.integration.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import school.hei.haapi.SentryConf;
import school.hei.haapi.endpoint.rest.security.cognito.CognitoComponent;
import school.hei.haapi.file.BucketConf;
import school.hei.haapi.service.aws.FileService;

@AutoConfigureMockMvc
public class MockedThirdParties {
  @LocalServerPort protected int localPort;
  @MockBean protected BucketConf bucketConf;
  @MockBean protected SentryConf sentryConf;
  @MockBean protected CognitoComponent cognitoComponentMock;
  @MockBean protected FileService fileService;
}
