package school.hei.haapi.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.mpbs.MpbsStatusHistory;

public interface MpbsStatusHistoryRepository extends JpaRepository<MpbsStatusHistory, String> {
  List<MpbsStatusHistory> findAllByMpbs_PspId(String mpbsPspId);
}
