package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Payment;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.validator.PaymentValidator;
import school.hei.haapi.repository.FeeRepository;
import school.hei.haapi.repository.PaymentRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.endpoint.rest.model.Fee.StatusEnum.PAID;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {

    private final FeeService feeService;
    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;

    public List<Payment> getByStudentIdAndFeeId(
            String studentId, String feeId, PageFromOne page, BoundedPageSize pageSize) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "creationDatetime"));
        return paymentRepository.getByStudentIdAndFeeId(studentId, feeId, pageable);
    }

    public void computeRemainingAmount(String feeId, int amount) {
        Fee associatedFee = feeService.getById(feeId);
        double totalRemainingAmount = (associatedFee.getInterest() + associatedFee.getRemainingAmount());
        if ((amount > totalRemainingAmount)) {
            throw new BadRequestException("The amount " +
                    amount +
                    " can't be bigger than remain amount " +
                    totalRemainingAmount);
        }

        if(associatedFee.getRemainingAmount() != 0 && amount < associatedFee.getRemainingAmount()){
            associatedFee.setRemainingAmount(associatedFee.getRemainingAmount() - amount);
        } else {
            associatedFee.setRemainingAmount(0);
            amount -= associatedFee.getRemainingAmount();
        }
        if (amount <= associatedFee.getInterest()) {
            associatedFee.setInterest(associatedFee.getInterest() - amount);
        }
        if (associatedFee.getRemainingAmount() == 0 && associatedFee.getInterest() == 0) {
            associatedFee.setStatus(PAID);
        }
        feeRepository.save(associatedFee);
    }

    @Transactional
    public List<Payment> saveAll(List<Payment> toCreate) {
        paymentValidator.accept(toCreate);
        toCreate.forEach(
                payment -> computeRemainingAmount(payment.getFee().getId(), payment.getAmount()));
        return paymentRepository.saveAll(toCreate);
    }
}
