# [1.156.0](https://github.com/hei-school/hei-admin-api/compare/v1.155.0...v1.156.0) (2026-04-29)


### Bug Fixes

* accounting fees duplicate due to status histories ([5822865](https://github.com/hei-school/hei-admin-api/commit/5822865bc55fa996f606599b51b493d39c3d25cf))
* advanced fees stat, unknown feeCategory mapper ([71dfeb1](https://github.com/hei-school/hei-admin-api/commit/71dfeb1d350866099cb81f3ad111ee73289d3f35))
* change vola verification endpoint target  ([86f5121](https://github.com/hei-school/hei-admin-api/commit/86f51217ad5dc6b7092e595e511724478e271c8a))
* extract method and remove duplication ([6d178e3](https://github.com/hei-school/hei-admin-api/commit/6d178e3ecf1b313d067f0988f099bdc37c125d99))
* Fee status not updated when Vola verifies an Mpbs ([6694307](https://github.com/hei-school/hei-admin-api/commit/66943070adea95e8a5dbcfa16e85ba4028191e6f))
* get fees of non-disabled students only in fee export ([12169fa](https://github.com/hei-school/hei-admin-api/commit/12169fa29f4989f3280ed7659cecaa4caedc52f3))
* unpaid fees null in advanced fee stats ([283cf98](https://github.com/hei-school/hei-admin-api/commit/283cf989d2ed578ccfdd6eff619549b4711b4532))


### Features

* (doc) add new attribut for globaleAttendance ([dd6fb19](https://github.com/hei-school/hei-admin-api/commit/dd6fb197196a070f3990f5d944d2765a893c4f69))
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



# [1.150.0](https://github.com/hei-school/hei-admin-api/compare/v1.149.0...v1.150.0) (2026-01-22)


### Bug Fixes

* get all exams ([1f40bcf](https://github.com/hei-school/hei-admin-api/commit/1f40bcf49c85572f7d0c053f74ce255dd83df2f5))


### Features

* add result summary generation ([4ca97a5](https://github.com/hei-school/hei-admin-api/commit/4ca97a562949bcdefdb0a2027d95a7ef29d58eb6))
* allow admin to create a new student ([4b5814e](https://github.com/hei-school/hei-admin-api/commit/4b5814e6ab01e0eef887b2178ed13efe92f6f6f5))



# [1.149.0](https://github.com/hei-school/hei-admin-api/compare/v1.148.0...v1.149.0) (2026-01-14)


### Bug Fixes

* enable repeater grades for their previous groups ([39d5dc9](https://github.com/hei-school/hei-admin-api/commit/39d5dc93c56d5e6bedd226e2e6fd89449aeb306e))
* promotion and scholarship test ([8da7eab](https://github.com/hei-school/hei-admin-api/commit/8da7eab686cae8351970a39da0d4c478f0695650))
* promotion and scholarship test ([1103727](https://github.com/hei-school/hei-admin-api/commit/1103727584144792eaef42f9cb0774bd3df6510f))


### Features

* (doc) add new endpoint to download student results summary transcript ([5405043](https://github.com/hei-school/hei-admin-api/commit/5405043298883dd7c26bd1656c727049d2e5c48d))
* add global search ([b3afb06](https://github.com/hei-school/hei-admin-api/commit/b3afb06dc20edbdc9932485b0f55f80c87f98ead))
* add grade template generator ([16b0edf](https://github.com/hei-school/hei-admin-api/commit/16b0edf802b6aeebf3ee84ebe428c6f14d3dd672))



# [1.148.0](https://github.com/hei-school/hei-admin-api/compare/v1.147.0...v1.148.0) (2025-12-29)


### Features

* add user details in get monitor link student requests ([c0cedb3](https://github.com/hei-school/hei-admin-api/commit/c0cedb311fb8173c46323cbc3ce431ea3dc43c1e))



# [1.147.0](https://github.com/hei-school/hei-admin-api/compare/v1.146.0...v1.147.0) (2025-12-29)


### Features

* add student status to event participant ([8dfb6a4](https://github.com/hei-school/hei-admin-api/commit/8dfb6a4f1184c0c569d4f6daf882447ba60de4dd))



