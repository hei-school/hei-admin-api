package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.service.utils.DataFormatterUtils.formatLocalDate;
import static school.hei.haapi.service.utils.DataFormatterUtils.instantToOcsDateFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.UpdatePromotionSGroup;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.dao.PromotionDao;

@Service
@AllArgsConstructor
public class PromotionService {
  private final PromotionRepository promotionRepository;
  private final PromotionDao promotionDao;
  private final GroupService groupService;
  private final UserService userService;

  public List<Promotion> getPromotions(
      String name, String ref, String groupRef, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    return promotionDao.findByCriteria(name, ref, groupRef, pageable);
  }

  public Promotion getPromotionById(String id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Promotion with #id " + id + " not found"));
  }

  public Promotion crupdatePromotion(Promotion domain) {
    Optional<Promotion> optionalPromotion = promotionRepository.findById(domain.getId());
    if (optionalPromotion.isPresent()) {
      optionalPromotion.get().setRef(domain.getRef());
      optionalPromotion.get().setName(domain.getName());
      return promotionRepository.save(optionalPromotion.get());
    }
    return promotionRepository.save(domain);
  }

  public Promotion updateGroups(String promotionId, UpdatePromotionSGroup updatePromotionSGroup) {
    Promotion promotion = getPromotionById(promotionId);
    for (String groupId : updatePromotionSGroup.getGroupIds()) {
      switch (updatePromotionSGroup.getType()) {
        case ADD -> groupService.updateGroups(promotion, groupId);
        case REMOVE -> groupService.updateGroups(null, groupId);
      }
    }

    return promotionRepository.save(promotion);
  }

  public LinkedHashSet<Promotion> getAllStudentPromotions(String userId) {
    return promotionRepository.findAllPromotionsByStudentId(userId);
  }

  public byte[] getStudentsPromotionInXlsx(String promotionId) throws IOException {
    List<User> students = userService.getStudentsByPromotionId(promotionId);
    return generateSheetsOfStudentsPromotionById(students);
  }

  private byte[] generateSheetsOfStudentsPromotionById(List<User> students) throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("excel-sheet");
    Row row0 = sheet.createRow(0);

    row0.createCell(0).setCellValue("Réf");
    row0.createCell(1).setCellValue("Nom");
    row0.createCell(2).setCellValue("Prénom");
    row0.createCell(3).setCellValue("email");
    row0.createCell(4).setCellValue("CIN");
    row0.createCell(5).setCellValue("Date de naissance");
    row0.createCell(6).setCellValue("Lieu de naissance");
    row0.createCell(7).setCellValue("Date d'entrée");
    row0.createCell(8).setCellValue("Sex");

    for (int i = 0; i < students.size(); i++) {
      User useri = students.get(i);
      Row rowi = sheet.createRow(i + 1);
      generateStudentSheetCell(rowi, useri);
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      workbook.write(bos);
    } finally {
      bos.close();
    }
    return bos.toByteArray();
  }

  private void generateStudentSheetCell(Row correspondingRow, User userToPrint) {
    correspondingRow.createCell(0).setCellValue(userToPrint.getRef());
    correspondingRow.createCell(1).setCellValue(userToPrint.getLastName());
    correspondingRow.createCell(2).setCellValue(userToPrint.getFirstName());
    correspondingRow.createCell(3).setCellValue(userToPrint.getEmail());
    correspondingRow.createCell(4).setCellValue(userToPrint.getNic());
    correspondingRow.createCell(5).setCellValue(formatLocalDate(userToPrint.getBirthDate()));
    correspondingRow.createCell(6).setCellValue(userToPrint.getBirthPlace());
    correspondingRow
        .createCell(7)
        .setCellValue(instantToOcsDateFormat(userToPrint.getEntranceDatetime()));
    correspondingRow.createCell(8).setCellValue(String.valueOf(userToPrint.getSex()));
  }
}
