package school.hei.haapi.service.event;

import lombok.AllArgsConstructor;
import school.hei.haapi.endpoint.event.model.UpdateUserStatusAfterPaidFees;
import school.hei.haapi.service.FeeService;

import java.util.function.Consumer;

@AllArgsConstructor
public class UpdateUserStatusAfterPaidFeesTriggeredService implements Consumer<UpdateUserStatusAfterPaidFees> {

    private final FeeService feeService;

    @Override
    public void accept(UpdateUserStatusAfterPaidFees updateUserStatusAfterPaidFees) {
        feeService.updateUserStatus();
    }
}
