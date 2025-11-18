package school.hei.haapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;

public class GradeImportTest extends FacadeITMockedThirdParties {
    @Autowired private GradeService subject;
}
