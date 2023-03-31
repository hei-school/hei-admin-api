package school.hei.haapi.model.exception;

public class DataAccessException extends ApiException {
    public DataAccessException( String message) {
        super(ExceptionType.CLIENT_EXCEPTION,message);
    }
}
