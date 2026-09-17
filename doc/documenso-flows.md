# Documenso — les flux

Fiches d'engagement signées par les moniteurs, via [Documenso](https://documenso.com).
Quatre flux indépendants, dans l'ordre où on les rencontre.

## 1. Synchronisation des modèles

Les modèles sont créés à la main dans l'interface Documenso. HEI Admin n'en tient qu'un
miroir local, rafraîchi à l'ouverture de l'écran.

```mermaid
flowchart LR
  UI["Écran Fiches\nuseSyncDocumensoTemplates"] -->|POST /documenso-templates/sync| SVC[TemplateDocumensoService]
  SVC -->|"GET /template (pages de 100)"| DOC[(Documenso)]
  SVC --> UP["Créer ou mettre à jour\nles modèles vus"]
  SVC --> PR{"Modèle local\nabsent de Documenso ?"}
  PR -->|"aucune fiche ne s'en sert"| DEL["Supprimé"]
  PR -->|"une fiche s'en sert"| KEEP["Conservé"]
```

L'élagage n'a lieu que si la liste distante a été parcourue **en entier** : une page manquée
ferait passer des modèles vivants pour disparus.

## 2. Génération des fiches

Déclenchée par un admin depuis une promotion. Une fiche par étudiant **mensualisé** —
les forfaits annuels sont exclus.

```mermaid
sequenceDiagram
  participant A as Admin
  participant API as DocumensoDocumentController
  participant B as DocumensoBulkGenerationService
  participant EB as EventBridge → SQS
  participant C as DocumensoDocumentGenerationTriggeredService
  participant S as DocumensoDocumentService
  participant D as Documenso

  A->>API: POST /promotions/{id}/documenso-documents
  API->>B: templateName
  B->>B: findAllMonthlyPayingStudentsByPromotionId
  loop un événement par étudiant
    B->>EB: DocumensoDocumentGenerationTriggered
  end
  API-->>A: 202 Accepted (nombre d'étudiants)
  EB->>C: un message par étudiant
  C->>S: generateDocument(studentId, templateName, generatedById)
  S->>S: moniteur de l'étudiant, niveau, modèle
  S->>S: fiche déjà PENDING ou COMPLETED ? → on rend l'existante
  S->>D: GET /template/{id} puis POST /template/use
  D-->>S: documentId + destinataire + jeton
  S->>S: persist (PENDING) + destinataire
```

La réponse est un **202** : la génération se poursuit en arrière-plan, fiche par fiche.
Un échec sur un étudiant n'affecte pas les autres.

Deux garde-fous dans `generateDocument` : une fiche encore valable n'est jamais dupliquée,
et le modèle distant doit déclarer **exactement un** signataire — le moniteur. La signature
de l'administration est incrustée dans le PDF du modèle, elle ne passe pas par Documenso.

Seules les données de l'étudiant sont préremplies. Le moniteur remplit les siennes en signant.

## 3. Signature et archivage

```mermaid
sequenceDiagram
  participant M as Moniteur
  participant UI as HEI Admin
  participant D as Documenso
  participant W as DocumensoWebhookController
  participant H as DocumensoWebhookHandler
  participant S3 as S3

  M->>UI: Signer
  UI->>UI: GET /documenso-documents/{id}/signing-token
  UI->>D: nouvel onglet vers /sign/{token}
  M->>D: signe
  D->>W: POST /documenso/webhook (X-Documenso-Secret)
  W->>W: secret invalide → 401
  W->>H: DOCUMENT_COMPLETED
  H->>H: document inconnu → 200 + log.warn
  H->>D: GET /document/{id}/download?version=signed
  H->>S3: DOCUMENSO/<aaaa-MM>/<id>.pdf
  H->>H: COMPLETED, completedAt de Documenso, date d'archivage
  W-->>D: 200
```

Le traitement est **synchrone** : le 200 n'est rendu qu'une fois le PDF déposé et le statut écrit.

Un document inconnu est acquitté avec un `200`, jamais un `404` : Documenso rejouerait
indéfiniment un payload qu'aucune tentative ne pourra satisfaire — ses propres envois de test,
et les orphelins créés quand la persistance échouait.

Deux dates sont conservées, et leur écart est un diagnostic :

| Date | Origine | Lecture |
|---|---|---|
| `completedDatetime` | `completedAt` de Documenso | quand le moniteur a signé |
| `archivedDatetime` | la nôtre | quand on a récupéré le PDF |

Quelques secondes d'écart : le webhook a fonctionné. Plusieurs heures : il s'est perdu et
c'est le balayage qui a rattrapé.

## 4. Le filet de rattrapage

Un webhook perdu laisserait une fiche `PENDING` pour toujours — aucun autre événement ne
viendra la réparer.

```mermaid
flowchart TD
  CR["Planification EventBridge"] -->|PendingDocumensoDocumentsCheckTriggered| SW[PendingDocumensoDocumentsCheckTriggeredService]
  SW --> Q["Toutes les fiches PENDING"]
  Q --> G["GET /document/{id} pour chacune"]
  G --> ST{Statut distant}
  ST -->|COMPLETED| AR["archiveSignedDocument\nmême chemin que le webhook"]
  ST -->|REJECTED| RJ["REJECTED"]
  ST -->|DRAFT ou PENDING| NO["On ne touche à rien"]
```

Le `DetailType` de la planification doit être le nom **pleinement qualifié** de la classe
d'événement : `EventServiceInvoker` le compare à `clazz.getTypeName()` pour résoudre le service.
Un nom court ne résout rien.

Le coût croît avec le nombre de fiches ouvertes : un appel Documenso par fiche `PENDING`, à
chaque passage. `PENDING` étant l'état normal d'une fiche pendant des semaines, une fréquence
élevée consomme le quota pour rien.

## Cycle de vie d'une fiche

```mermaid
stateDiagram-v2
  [*] --> PENDING: generateDocument
  PENDING --> COMPLETED: webhook, ou balayage
  PENDING --> REJECTED: balayage
  COMPLETED --> [*]: PDF sur S3, lien présigné 5 min
  note right of COMPLETED
    Jamais revisitée par le balayage,
    qui ne parcourt que les PENDING
  end note
```

## Points de rupture connus

**Le modèle Documenso commande tout.** Si le placeholder du destinataire est en rôle `VIEWER`,
ou s'il est `SIGNER` sans champ Signature qui lui soit assigné, Documenso clôt le document
immédiatement. La chaîne archivera alors des fiches que personne n'a signées, sans que rien
côté HEI Admin puisse le détecter.

**Le lien de téléchargement** est présigné pour 5 minutes, généré à la demande depuis
`file_info.file_path`. Supprimer une ligne en base rend son PDF inatteignable : l'objet reste
sur S3 mais plus rien ne le référence.

**Le secret du webhook** est comparé en clair. L'endpoint est en `permitAll` dans `SecurityConf` ;
c'est ce secret qui tient lieu d'authentification.
