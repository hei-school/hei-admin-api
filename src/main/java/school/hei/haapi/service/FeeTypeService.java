package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.FeeType;
import school.hei.haapi.repository.FeeTypeRepository;


import java.util.List;

@Service
@AllArgsConstructor
public class FeeTypeService {

    private final FeeTypeRepository feeTypeRepository;

    public List<FeeType> getFeeTypes(){
        return feeTypeRepository.findAll();
    }

    public FeeType getFeeTypeById(String id){
        return feeTypeRepository.getById(id);
    }

    public FeeType createOrUpdateFeeTypes(FeeType domain){
        return feeTypeRepository.save(domain);
    }

}
