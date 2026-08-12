package school.hei.haapi.model;

/**
 * Keywords matched against Documenso template field labels. Labels are normalized (accents
 * stripped, lowercased) before comparison, so keywords must be written accent-free and lowercase.
 */
public final class TemplateFieldLabels {
  public static final String FULL_NAME = "nom et prenom";
  public static final String LEVEL = "niveau";
  public static final String PARENT_INDICATOR = "tuteur";
  public static final String ADDRESS = "adresse personnelle";
  public static final String PHONE = "telephone";
  public static final String NIC = "titulaire de la cin";

  private TemplateFieldLabels() {}
}
