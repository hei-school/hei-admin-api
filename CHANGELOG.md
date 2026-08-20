## [1.167.1](https://github.com/hei-school/hei-admin-api/compare/v1.167.0...v1.167.1) (2026-08-20)


### Bug Fixes

* **db:** make documenso_user_id migration idempotent ([8749351](https://github.com/hei-school/hei-admin-api/commit/8749351dd7fa304dc575e4068a5c989f83aa0ab0))



# [1.167.0](https://github.com/hei-school/hei-admin-api/compare/v1.166.1...v1.167.0) (2026-08-19)


### Features

* documenso integration ([afb305e](https://github.com/hei-school/hei-admin-api/commit/afb305e383bd1bc4e5167292e09d3223b6866f44))



## [1.166.1](https://github.com/hei-school/hei-admin-api/compare/v1.166.0...v1.166.1) (2026-08-12)


### Bug Fixes

* pin axios version to 1.18.1 ([54199a6](https://github.com/hei-school/hei-admin-api/commit/54199a63f8b236a5b5d2bb41ba644f2deb9bf99d))



# [1.166.0](https://github.com/hei-school/hei-admin-api/compare/v1.165.1...v1.166.0) (2026-08-11)


### Bug Fixes

* get student credit transactions ([a5f583e](https://github.com/hei-school/hei-admin-api/commit/a5f583ed88a402b1367a560ca843421dc19cd941))


### Features

* implement fees archiving and credit movement tracking ([f6f2ffd](https://github.com/hei-school/hei-admin-api/commit/f6f2ffd8c83833fdfdf0c704ee0d8c6635ddc291))
* reject credit payment ([8497a7f](https://github.com/hei-school/hei-admin-api/commit/8497a7f713b82d0d514711c6b68ce8d35c0fb4e1))



## [1.165.1](https://github.com/hei-school/hei-admin-api/compare/v1.165.0...v1.165.1) (2026-08-06)


### Bug Fixes

* pin axios version to 1.18.1 ([5e271d3](https://github.com/hei-school/hei-admin-api/commit/5e271d3aaf043f4838c6896e2bd557d8431f5717))



# [1.165.0](https://github.com/hei-school/hei-admin-api/compare/v1.164.0...v1.165.0) (2026-08-06)


### Features

* add fees only mode filter ([bdf0651](https://github.com/hei-school/hei-admin-api/commit/bdf06512111cd5f403030e00e2f806d77f970cb6))
* create fee template with specific content ([f04be45](https://github.com/hei-school/hei-admin-api/commit/f04be45fcde3c564f3e6496f25aed43d7347baf0))
* create fees through async jobs ([56a5ae7](https://github.com/hei-school/hei-admin-api/commit/56a5ae7dc5a5aa95e71cc0ec9ac5c12497ed0f53))



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



