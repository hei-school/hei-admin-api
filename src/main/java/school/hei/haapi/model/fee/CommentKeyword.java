package school.hei.haapi.model.fee;

import lombok.Getter;

@Getter
public enum CommentKeyword {
  MONTHLY_FEE_KEYWORD("mensuel"),
  YEARLY_FEE_KEYWORD("annuel"),
  WORK_STUDY_FEE_COMMENT_KEYWORD("alternance");

  private final String keyword;

  CommentKeyword(String keyword) {
    this.keyword = keyword;
  }
}
