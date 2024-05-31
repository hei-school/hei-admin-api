package school.hei.haapi.service.mobileMoney;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.exception.ApiException;

@Component
@Primary
public class MobileMoneyApiFacade implements MobileMoneyApi {
    private final MobileMoneyApi orangeApi;
    private final MobileMoneyApi mvolaApi;

    public MobileMoneyApiFacade(@Qualifier("OrangeApi")
    MobileMoneyApi orangeApi,
    @Qualifier("MvolaApi")
    MobileMoneyApi mvolaApi) {
        this.orangeApi = orangeApi;
        this.mvolaApi = mvolaApi;
    }

    @Override
    public TransactionDetails getByTransactionRef(MobileMoneyType type, String ref) throws ApiException {
        return switch (type){
            case MVOLA -> mvolaApi.getByTransactionRef(type, ref);
            case ORANGE_MONEY -> orangeApi.getByTransactionRef(type, ref);
            case AIRTEL_MONEY -> throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, "NOT IMPLEMENTED");
        };
    }
}
