package school.hei.haapi.service.mobileMoney;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.MobileMoneyType;
import school.hei.haapi.http.model.TransactionDetails;
import school.hei.haapi.model.exception.ApiException;

import java.util.List;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Primary
public class MobileMoneyApiFacade implements MobileMoneyApi {
    private final MobileMoneyApi orangeApi;
    private final MobileMoneyApi mvolaApi;
    private final MobileMoneyApi orangeScrappingApi;

    public MobileMoneyApiFacade(
            @Qualifier("OrangeApi")
            MobileMoneyApi orangeApi,
            @Qualifier("MvolaApi")
            MobileMoneyApi mvolaApi,
            @Qualifier("OrangeScrappingApi")
            MobileMoneyApi orangeScrappingApi) {
        this.orangeApi = orangeApi;
        this.mvolaApi = mvolaApi;
        this.orangeScrappingApi = orangeScrappingApi;
    }

    @Override
    public TransactionDetails getByTransactionRef(MobileMoneyType type, String ref) throws ApiException {
        return switch (type){
            case MVOLA -> mvolaApi.getByTransactionRef(type, ref);
            case ORANGE_MONEY -> orangeScrappingApi.getByTransactionRef(type, ref);
            case AIRTEL_MONEY -> throw new ApiException(SERVER_EXCEPTION, "NOT IMPLEMENTED");
        };
    }

    @Override
    public List<TransactionDetails> fetchThenSaveTransactionsDetails(MobileMoneyType type) {
        return switch (type){
            case MVOLA -> mvolaApi.fetchThenSaveTransactionsDetails(type);
            case ORANGE_MONEY -> orangeScrappingApi.fetchThenSaveTransactionsDetails(type);
            case AIRTEL_MONEY -> throw new ApiException(SERVER_EXCEPTION, "NOT IMPLEMENTED");
        };
    }
}
