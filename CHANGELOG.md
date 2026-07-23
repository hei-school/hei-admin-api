# [1.164.0](https://github.com/hei-school/hei-admin-api/compare/v1.163.0...v1.164.0) (2026-07-23)


### Features

* validate retake exams and update automatically exams grades ([12f5278](https://github.com/hei-school/hei-admin-api/commit/12f52780ccb5f77a52598d37aa792eff6c59b4a2))



# [1.163.0](https://github.com/hei-school/hei-admin-api/compare/v1.162.0...v1.163.0) (2026-07-22)


### Bug Fixes

* **course_result:** student repeating year grades ([1c94790](https://github.com/hei-school/hei-admin-api/commit/1c947907dacb1c22f5088d4c03faee61b1cad6cb))
* **CourseResult:** remove course_result's duplication ([fbdeb04](https://github.com/hei-school/hei-admin-api/commit/fbdeb043e6610fc35e7450bb0274834f5caabbb5))
* get alternating student ([874f2d5](https://github.com/hei-school/hei-admin-api/commit/874f2d5c53022a552d6208fad486e4619cbb20d4))
* get student yearly result ([b3f3913](https://github.com/hei-school/hei-admin-api/commit/b3f3913b7ecc57d109842f644c8b51bdc2501bad))
* resolve SonarQube code reliability issues ([4b06a96](https://github.com/hei-school/hei-admin-api/commit/4b06a966d97d158f74438351e53fedd1a73a9623))
* **security:** add new role to security configuration ([16e24a6](https://github.com/hei-school/hei-admin-api/commit/16e24a697c3ee9bbb16bc243e5f364477f5969a2))
* sonar ([02bf9c1](https://github.com/hei-school/hei-admin-api/commit/02bf9c17b2411c41f6137c5e53c9be3fe59557e5))
* sonar check failed ([de8bfd8](https://github.com/hei-school/hei-admin-api/commit/de8bfd8090c1aea7e99b05483102897109ac7574))
* sort pending fees to be first in the student fee list ([c63c0a2](https://github.com/hei-school/hei-admin-api/commit/c63c0a25d5870670cb6995e2bc0f6d93c2493d9c))
* **user-service:** align transactional configuration for getById ([b827389](https://github.com/hei-school/hei-admin-api/commit/b827389fa8fa64e673a8cf06c556df2ead609e25))
* **user-service:** align transactional configuration for getById ([fbd5eec](https://github.com/hei-school/hei-admin-api/commit/fbd5eec6d24bd4e76c12b2dcb1d2a8b003fa9bd8))


### Features

* configure UserActivityInterceptor with UserActivityInterceptorConfigurer ([7f01718](https://github.com/hei-school/hei-admin-api/commit/7f017181d2b2db56728b5b76c0635c8b0405a238))
* create annotator for user interceptor ([a215c04](https://github.com/hei-school/hei-admin-api/commit/a215c04cda4205f5763cbb0c8aa6ff5a83c268f4))
* **globalSearch:** add result limit to global search ([04c4734](https://github.com/hei-school/hei-admin-api/commit/04c47345262a0a6f864bc7624f2db589af29a9b1))
* **security:** add new role to security configuration ([2a16231](https://github.com/hei-school/hei-admin-api/commit/2a162310c1b8bb5d3143d63984bdb1f3ef2d4694))
* update retake exams status ([5ba1019](https://github.com/hei-school/hei-admin-api/commit/5ba10198c1d3b8514c9d1f397345c9d0c3362a77))



# [1.162.0](https://github.com/hei-school/hei-admin-api/compare/v1.159.0...v1.162.0) (2026-06-17)


### Bug Fixes

* apply all request change ([695add5](https://github.com/hei-school/hei-admin-api/commit/695add5e5d75fa5239cdc23e4f6c81412f253c1b))
* **courseResultService:** get course results only for courses assigned to the student's group ([4d446f8](https://github.com/hei-school/hei-admin-api/commit/4d446f8b9db913b45d1e5a13361f477ebce57dca))
* deployement ([763d20a](https://github.com/hei-school/hei-admin-api/commit/763d20aaefac95a96824a79b0f361cd2827c943f))
* empty status history should return the initial status ([aefdae7](https://github.com/hei-school/hei-admin-api/commit/aefdae735c469f7de30f206165a831597b1450f8))
* fetch course with course_assignments ([5dab9b0](https://github.com/hei-school/hei-admin-api/commit/5dab9b02000e66f3791d2c8f1de60ae57d423b68))
* find all fees by due datetime jpql request ([c5e6ed4](https://github.com/hei-school/hei-admin-api/commit/c5e6ed46b6797f41f73894433b02e26a2f9b1382))
* find_all_fees_by_due_datetime jpql ([dada6c1](https://github.com/hei-school/hei-admin-api/commit/dada6c186dd170fbde2562736e0cfbf5b5e2040f))
* get events ([31d368c](https://github.com/hei-school/hei-admin-api/commit/31d368c2529b2e2c042310b2dbd3093ae98c7fdf))
* get student result overviews ([bcf64dc](https://github.com/hei-school/hei-admin-api/commit/bcf64dc20c094c044116bc364fc8fdd1c18aa759))
* intercept request body and filter out OPTIONS and HEAD requests ([0244609](https://github.com/hei-school/hei-admin-api/commit/0244609fba7727aa2047dcb2869185042cc61be8))
* **user:** remove disabled filter and fix status type mapping in UserDto ([31ae0f5](https://github.com/hei-school/hei-admin-api/commit/31ae0f5e47f20c85dc8eb2fde356ae117ec92247))


### Features

* add handler interceptor to all controller requests ([cff8714](https://github.com/hei-school/hei-admin-api/commit/cff8714094598ee297ebde27bf8734b14d324eae))
* get student overview ([6a7d5e4](https://github.com/hei-school/hei-admin-api/commit/6a7d5e4aed51e8e241cdca1b65b5be79858c1732))
* implemente student retake exams list ([29e86ba](https://github.com/hei-school/hei-admin-api/commit/29e86ba5a9f15e0017f838f5451a0d9815404564))
* move endpoint getStudentRetakeExams in RetakeExamController ([80ccb0b](https://github.com/hei-school/hei-admin-api/commit/80ccb0b0c9e70d273fd46e7f78dbc8a28f5a6660))
* update tags grades in doc ([8d9cac5](https://github.com/hei-school/hei-admin-api/commit/8d9cac53d72d4cf7f31c1588050d3541cacf65b4))



# [1.159.0](https://github.com/hei-school/hei-admin-api/compare/v1.158.0...v1.159.0) (2026-05-20)


### Features

* add students/result_overviews endpoint spec ([d7c6efe](https://github.com/hei-school/hei-admin-api/commit/d7c6efe9b90e4bcbaff6dc3c83609e7a46dfbba9))
* student retake exams list ([#1232](https://github.com/hei-school/hei-admin-api/issues/1232)) ([e2c6dce](https://github.com/hei-school/hei-admin-api/commit/e2c6dce5f154439742136b541a9dd928afe72895))



# [1.158.0](https://github.com/hei-school/hei-admin-api/compare/v1.155.0...v1.158.0) (2026-05-15)


### Bug Fixes

* accounting fees duplicate due to status histories ([5822865](https://github.com/hei-school/hei-admin-api/commit/5822865bc55fa996f606599b51b493d39c3d25cf))
* add @Builder.Default to RoomName and FeeCategory field to preserve default value ([91f00e6](https://github.com/hei-school/hei-admin-api/commit/91f00e65ebd930658ad803d45d9c3d4ffc63d220))
* advanced fees stat, unknown feeCategory mapper ([71dfeb1](https://github.com/hei-school/hei-admin-api/commit/71dfeb1d350866099cb81f3ad111ee73289d3f35))
* change vola verification endpoint target  ([86f5121](https://github.com/hei-school/hei-admin-api/commit/86f51217ad5dc6b7092e595e511724478e271c8a))
* extract method and remove duplication ([6d178e3](https://github.com/hei-school/hei-admin-api/commit/6d178e3ecf1b313d067f0988f099bdc37c125d99))
* Fee status not updated when Vola verifies an Mpbs ([6694307](https://github.com/hei-school/hei-admin-api/commit/66943070adea95e8a5dbcfa16e85ba4028191e6f))
* get fees of non-disabled students only in fee export ([12169fa](https://github.com/hei-school/hei-admin-api/commit/12169fa29f4989f3280ed7659cecaa4caedc52f3))
* migrate from REMEDIAL_COSTS to RETAKE_EXAM_COSTS ([c76d530](https://github.com/hei-school/hei-admin-api/commit/c76d5300921a9f53719bc7ae73ed212d0901400a))
* unpaid fees null in advanced fee stats ([283cf98](https://github.com/hei-school/hei-admin-api/commit/283cf989d2ed578ccfdd6eff619549b4711b4532))


### Features

*  (doc) add new query params when getting letter ([e33d660](https://github.com/hei-school/hei-admin-api/commit/e33d66049727322da4d4b68b2f4d44bf323d89f7))
* (doc) add new attribut for globaleAttendance ([31ff579](https://github.com/hei-school/hei-admin-api/commit/31ff57908f77b6b5cd8fd6e22530ce74f716b2b3))
* (doc) add new endpoint to get all student licence ([b30c3f7](https://github.com/hei-school/hei-admin-api/commit/b30c3f7b809f52173d7202184bbe4a517ff17be5))
* vola migration ([1e61f6a](https://github.com/hei-school/hei-admin-api/commit/1e61f6a331468dd45797fdc1359a06f68d10b2e2))



# [1.155.0](https://github.com/hei-school/hei-admin-api/compare/v1.154.1...v1.155.0) (2026-03-26)


### Features

* add status-checks to flag potential withdrawing students ([821726d](https://github.com/hei-school/hei-admin-api/commit/821726d68c1a36120ffc5ed10d7bd9a57b4b03a4))



## [1.154.1](https://github.com/hei-school/hei-admin-api/compare/v1.154.0...v1.154.1) (2026-03-12)


### Bug Fixes

* fees mapper ([489b7ae](https://github.com/hei-school/hei-admin-api/commit/489b7aef69f737474521d1318aed2568a7757f6a))



# [1.154.0](https://github.com/hei-school/hei-admin-api/compare/v1.152.0...v1.154.0) (2026-03-12)


### Bug Fixes

* remove unecessary student alumni filter ([831c6eb](https://github.com/hei-school/hei-admin-api/commit/831c6ebaf07c0da945d46b235b06eda19250fddd))


### Features

* (doc)  client version ([4879ff0](https://github.com/hei-school/hei-admin-api/commit/4879ff0cc003dcc79fbc0cc89c2e54fbd79b7cc2))
* add filter fees by category ([96fcf38](https://github.com/hei-school/hei-admin-api/commit/96fcf383eafbdcea6931eb968f3d3b46ff258e39))
* add non alumni self matcher for scholarship certificate  ([47b3528](https://github.com/hei-school/hei-admin-api/commit/47b35281e4c3c8c55df0daceda6b2330fa1d602c))



# [1.152.0](https://github.com/hei-school/hei-admin-api/compare/v1.151.0...v1.152.0) (2026-03-11)


### Bug Fixes

* add interviewers in toDomainUpdate ([b2b3ec9](https://github.com/hei-school/hei-admin-api/commit/b2b3ec9ca8d68ce3076d4ad3d2c562ab092c54e2))
* advanced fees stats ([0f6d08b](https://github.com/hei-school/hei-admin-api/commit/0f6d08b11d1be6c5124cc9c1626906d31293caa8))
* alumni had no access and should not get scolarship certificate ([fb69004](https://github.com/hei-school/hei-admin-api/commit/fb69004b13b561c215b39feded8495d404b7bf07))
* alumni has 403 error on whomai ([3769817](https://github.com/hei-school/hei-admin-api/commit/3769817fc79acb3bd58529718dcd273f429011e5))
* **auth:** export grade template authorization ([8623183](https://github.com/hei-school/hei-admin-api/commit/8623183676d7fb304540e5d8c3d966b1413f9c6c))
* cor creation dateTime on update ([d9f6805](https://github.com/hei-school/hei-admin-api/commit/d9f68058bdcce82d5c6f8ba6f3247f95b74fd611))
* fees stats file extension ([f5ffa26](https://github.com/hei-school/hei-admin-api/commit/f5ffa26d6d791f5ab07cf762d12472653a6d335d))
* fix corService' ([ac62663](https://github.com/hei-school/hei-admin-api/commit/ac6266359c452ffb216d47d9e7a123cbfc847c54))
* generate grade template ([3af7a65](https://github.com/hei-school/hei-admin-api/commit/3af7a658386744ecd4e2338510593013b27ec620))
* grade template generator ([fadd925](https://github.com/hei-school/hei-admin-api/commit/fadd9253a5b4c0c44245aa94fb6ce00ee6237353))
* is_deleted handling in fee joined queries and update [@where](https://github.com/where) annotation ([58ae4f8](https://github.com/hei-school/hei-admin-api/commit/58ae4f87f4a9042f24b51bb4decef9e517b8770a))
* prevent ScholarCertificate for past students by adding a cycleLevel to Promotion ([a03ec13](https://github.com/hei-school/hei-admin-api/commit/a03ec139de02c916a443d699ecf329f3b4a37809))
* remove interviewer in CorMapper ([1f0fce3](https://github.com/hei-school/hei-admin-api/commit/1f0fce3d3b8fa5224fc338c12cffbd676f9756cc))
* remove return from filter that stops all filters ([0b13029](https://github.com/hei-school/hei-admin-api/commit/0b13029e2cc419db6200221932938d68252726c6))


### Features

* (doc) add cycle level in crupdatePromotion ([f546711](https://github.com/hei-school/hei-admin-api/commit/f546711355507ec88a291ce7ad8f49f5c4d87ca4))
* **doc:** add new endpoint to export all fees ([39dc0d9](https://github.com/hei-school/hei-admin-api/commit/39dc0d980cb1b7b0ee0c498e3aefc2237254526d))
* export advanced fees stats ([e30aaa9](https://github.com/hei-school/hei-admin-api/commit/e30aaa984da1dc1c20f4cee18ef560f7de2762ae))
* export raw fees ([9f803c8](https://github.com/hei-school/hei-admin-api/commit/9f803c860223eef1b0b0a3ae79285d4cc2424a97))



# [1.151.0](https://github.com/hei-school/hei-admin-api/compare/v1.150.0...v1.151.0) (2026-01-30)


### Bug Fixes

* add UNPAID_COUNT stat type to advancedFeeStats ([a649491](https://github.com/hei-school/hei-admin-api/commit/a6494912c132bba341feee48c33e038ca1b841e7))
* generateTranscript serialisation error ([470d06f](https://github.com/hei-school/hei-admin-api/commit/470d06f39a1690152bd9516d7f2f6230c8e11559))
* **global_search:** return presigned S3 URL in UserDto ([b092e84](https://github.com/hei-school/hei-admin-api/commit/b092e847702be940ffa235525cc0801e8e9f03f4))


### Features

* (doc) add new enpoint to export advanced stats ([0030b5f](https://github.com/hei-school/hei-admin-api/commit/0030b5f48181add68cd886c41a6cbb95c860a4e6))



