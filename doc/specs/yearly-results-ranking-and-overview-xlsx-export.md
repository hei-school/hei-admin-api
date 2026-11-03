# Spec — Classement des résultats annuels par promotion et export xlsx des sortants

## Contexte

Deux besoins distincts, à ne pas confondre :

1. **Classement par promotion + niveau** (nouveau) : pour une promotion et un niveau (L1/L2/L3/M1/M2)
   donnés, exporter en xlsx le classement de tous les étudiants de la promotion à ce niveau
   (moyenne pondérée, crédits obtenus/totaux, groupe), tous groupes confondus.
2. **Liste des sortants** (existant, `StudentResultOverview`) : agrégat par étudiant sur toute sa
   scolarité (déjà en place, exposé en JSON via `GET /students/result_overviews`) — on ajoute
   uniquement un export xlsx de cette liste existante, sans toucher à son calcul.

## 1. Modèle de données — table persistée `yearly_result`

Les résultats par niveau ne sont plus recalculés à la volée à chaque lecture : ils sont persistés
dans une nouvelle table `yearly_result`, à la granularité **étudiant + niveau + groupe**.

| Colonne | Type | Remarque |
|---|---|---|
| `id` | varchar (PK) | `uuid_generate_v4()` |
| `student_id` | varchar (FK `user`) | not null |
| `group_id` | varchar (FK `group`) | not null — groupe de l'étudiant à ce niveau |
| `level` | enum `student_level` | not null — réutilise l'enum Postgres existant |
| `weighted_average` | double precision | nullable |
| `obtained_credits` | double precision | nullable |
| `total_credits` | double precision | nullable |
| `status` | enum `result_overview_status` | not null — réutilise l'enum Postgres existant |

Contrainte d'unicité : `(student_id, level)` — une seule ligne par étudiant et par niveau (upsert).

## 2. Déclenchement du recalcul

**Uniquement sur ajout ou modification d'une note** (pas de backfill, pas de recalcul à la
demande) :

- Les endpoints de saisie de note (`POST /exams/{exam_id}/students/{student_id}/grade`,
  `.../grade/update`, `POST /exams/{exam_id}/grades`, `.../grades/update`) déclenchent déjà un
  event `StudentResultOverviewUpserted(promotionId)`.
- Le consumer de cet event recalcule, pour chaque étudiant de la promotion, son résultat par
  niveau (réutilise `GradeResultService.getStudentResultSummary`, qui retourne déjà un résultat
  par niveau) et upserte une ligne `yearly_result` par niveau où le statut n'est pas
  `NOT_STARTED`. Le groupe de l'étudiant à ce niveau est résolu via l'historique `GroupFlow`
  (même mécanisme que `GroupFlowService.findStudentLatestGroupFlowPeriodsAtLevel`).
- Les promotions déjà en cours ne sont donc peuplées qu'à partir de la prochaine note
  ajoutée/modifiée — assumé et accepté, pas de script de backfill.

## 3. Spec — calcul du rang (`rank`)

- **Périmètre** : tous les étudiants de la promotion ayant une ligne `yearly_result` au niveau
  demandé (donc `status != NOT_STARTED`), **tous groupes confondus**. Le rang est global pour la
  promotion + niveau ; le groupe reste une simple colonne informative, pas un périmètre de
  classement.
- **Tri** : par `weighted_average` décroissant (meilleure moyenne = rang 1). Les lignes sans
  moyenne (`null`) sont placées en fin de classement (`NULLS LAST`).
- **Égalités (ex æquo)** : classement "standard competition ranking" (style 1-2-2-4). Deux
  étudiants avec exactement la même moyenne reçoivent le même rang ; l'étudiant suivant saute au
  rang correspondant à sa position réelle (pas au rang immédiatement suivant).
  Exemple : moyennes `18, 15, 15, 12` → rangs `1, 2, 2, 4`.
- **Étudiants exclus** : ceux sans ligne `yearly_result` à ce niveau (`NOT_STARTED`) n'apparaissent
  pas dans le fichier.
- **Calcul** : fait en mémoire au moment de la génération du xlsx (pas stocké en base) — le rang
  dépend du sous-ensemble promotion + niveau filtré à l'instant T, ce n'est pas une propriété
  intrinsèque de la ligne `yearly_result`.

## 4. Endpoint — classement par niveau (xlsx)

```
GET /promotions/{promotion_id}/yearly_results/raw/xlsx?level=L1
Produces: application/vnd.ms-excel
```

- `promotion_id` : path, requis
- `level` : query, requis (`L1`, `L2`, `L3`, `M1` ou `M2`)

Colonnes du fichier, dans l'ordre : `rank | ref | weightedAverage | obtainedCredits | totalCredits | groupe`

Lecture directe de la table `yearly_result` filtrée sur les groupes de la promotion assignés à ce
niveau, triée en SQL par `weighted_average desc` (`NULLS LAST`), pas de recalcul à la volée.

## 5. Endpoint — liste des sortants (xlsx)

```
GET /students/result_overviews/raw/xlsx?promotion_id={promotion_id}&status={status}
Produces: application/vnd.ms-excel
```

- `promotion_id` : query, requis
- `status` : query, optionnel (`VALIDATED` / `INVALIDATED` / `IN_PROGRESS` / `NOT_STARTED`) — si
  absent, exporte tous les statuts ; si fourni, filtre comme le fait déjà l'endpoint JSON existant
  `GET /students/result_overviews`.

Colonnes du fichier, dans l'ordre :
`ref | firstName | lastName | weightedAverage | obtainedCredits | totalCredits | status | graduationPromotion`

Source : table existante `student_result_overview`, aucun changement de calcul — uniquement un
nouveau format de sortie (xlsx au lieu de JSON uniquement), sans pagination (export complet).
