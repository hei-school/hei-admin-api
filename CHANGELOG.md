# [1.125.0](https://github.com/hei-school/hei-admin-api/compare/v1.124.0...v1.125.0) (2025-10-14)


### Features

* filter retake_exam_courses by course_code and participant by ref ([97af3af](https://github.com/hei-school/hei-admin-api/commit/97af3aff5bef526a74916cf958983e55af6e7e2c))



# [1.124.0](https://github.com/hei-school/hei-admin-api/compare/v1.123.1...v1.124.0) (2025-10-10)


### Features

* add get retake exam session by id ([6d985cc](https://github.com/hei-school/hei-admin-api/commit/6d985ccc11ee88c591387042406f7e869258936d))
* **not-implemented:** cor interviewers ([4ea6fd0](https://github.com/hei-school/hei-admin-api/commit/4ea6fd01d92d38516320e54f4ed39bc063177a2c))



## [1.123.1](https://github.com/hei-school/hei-admin-api/compare/v1.123.0...v1.123.1) (2025-10-08)


### Bug Fixes

* Cor store status ([405f96b](https://github.com/hei-school/hei-admin-api/commit/405f96bfb73500cb6e4b85425ab7f8ae1869d761))



# [1.123.0](https://github.com/hei-school/hei-admin-api/compare/v1.122.1...v1.123.0) (2025-10-08)


### Bug Fixes

* add logs and tests to AdvancedFeeStatsService for receipt type  ([6e4c63c](https://github.com/hei-school/hei-admin-api/commit/6e4c63ca32d4a7848c1f34d65f6b6a01a732f7ce))
* filter retake exam session by title ([7a5cf22](https://github.com/hei-school/hei-admin-api/commit/7a5cf22fbe180a8e4847e17b5c5e15664834ce17))
* payment duplication on Mpbs re-verification ([c1c31d2](https://github.com/hei-school/hei-admin-api/commit/c1c31d25f695e00c5e88e915068ae31cc807f637))


### Features

* **doc:** add retake exam status ([f228859](https://github.com/hei-school/hei-admin-api/commit/f228859127bc278c936d2a63e0082e6a4dd79dce))



## [1.122.1](https://github.com/hei-school/hei-admin-api/compare/v1.122.0...v1.122.1) (2025-10-02)


### Bug Fixes

* increase stack size for build ([19092e9](https://github.com/hei-school/hei-admin-api/commit/19092e9f4570c356a0f45a81d995c8beb6964b80))



# [1.122.0](https://github.com/hei-school/hei-admin-api/compare/v1.121.0...v1.122.0) (2025-10-02)


### Bug Fixes

* increase stack size for build ([be30d92](https://github.com/hei-school/hei-admin-api/commit/be30d925d0c3aa1d8c53263e916d0ae7a4be633b))


### Features

* add get all retake exam courses and all retake exam course participant in specific session ([2afe4f1](https://github.com/hei-school/hei-admin-api/commit/2afe4f1ec4682da23fffb974b6932d56a14ea475))



# [1.121.0](https://github.com/hei-school/hei-admin-api/compare/v1.120.0...v1.121.0) (2025-10-01)


### Bug Fixes

* course result status always in progress on completed exam ([8d2690c](https://github.com/hei-school/hei-admin-api/commit/8d2690cd83905f485c6b9bd4f0f31e97122235a3))
* **doc:** remove registration date in crupdateExam ([61520a5](https://github.com/hei-school/hei-admin-api/commit/61520a5f84ff231cd624884a1285d396ccf97397))
* handle all exceptions in attemptSaveTrasaction ([1b3e81c](https://github.com/hei-school/hei-admin-api/commit/1b3e81ccf2f49663dc9872b91d0e6cd02896ad68))
* handle runtime exceptions in attemptSaveTransaction ([f639e0f](https://github.com/hei-school/hei-admin-api/commit/f639e0fea8fb48ee9594fec90034a1e3238f32c1))
* implement cor ([ae9f885](https://github.com/hei-school/hei-admin-api/commit/ae9f8853746cf16c5a37573af9feee1f6e23b833))
* transcript generation failed to initialize proxy ([f3d9459](https://github.com/hei-school/hei-admin-api/commit/f3d94595beefc4cdbc4d579f6bc1ec653e755944))


### Features

* add retake exam registration date and refactor test ([01566b4](https://github.com/hei-school/hei-admin-api/commit/01566b4ae52e7a85eea8c719733e34e53b5cbaed))


### Reverts

* Revert "docs(api): student attendance with refactor components and filter" ([592b395](https://github.com/hei-school/hei-admin-api/commit/592b3952116b69030b90fe964000c026fe805b73))



# [1.120.0](https://github.com/hei-school/hei-admin-api/compare/v1.119.0...v1.120.0) (2025-09-24)


### Bug Fixes

* wrong result overview status computation ([e5e2db1](https://github.com/hei-school/hei-admin-api/commit/e5e2db1c70ff2745a0c25716b9d7ffe639b46131))


### Features

* **doc:** add retake exam registration date ([8c6c3b5](https://github.com/hei-school/hei-admin-api/commit/8c6c3b5b5fec723423dfbcd4d75760728ba91e84))



# [1.119.0](https://github.com/hei-school/hei-admin-api/compare/v1.118.1...v1.119.0) (2025-09-23)


### Bug Fixes

* find user group at instant ([518500f](https://github.com/hei-school/hei-admin-api/commit/518500f5cdec09b17ad652792a25ffba5248b38a))
* in progress course status ([7aefae4](https://github.com/hei-school/hei-admin-api/commit/7aefae4edcc80d54de6b5dbaf41c3c8e66e51c8e))
* mask retake exams for existing ones ([61f8a40](https://github.com/hei-school/hei-admin-api/commit/61f8a403c6dcacc9af619a86842d0bd082bee367))
* mpbs xlsx verification fail on duplicate reference ([c5f2cf3](https://github.com/hei-school/hei-admin-api/commit/c5f2cf346053d19d6090ca9af431de7c443943c7))
* yearly result transcript generation ([4247e1f](https://github.com/hei-school/hei-admin-api/commit/4247e1ff351e7080dea201e5d77552c52ead613d))
* yearly result transcript unavailable average on incomplete course result ([0dcfc7f](https://github.com/hei-school/hei-admin-api/commit/0dcfc7fbca5d5714b7f1e4ee2207a2bcc4e241d3))


### Features

* **not-implemented:** COR ([e33e6b3](https://github.com/hei-school/hei-admin-api/commit/e33e6b3890d936b8a1dd63f92a457c1de801411c))



## [1.118.1](https://github.com/hei-school/hei-admin-api/compare/v1.118.0...v1.118.1) (2025-09-18)


### Bug Fixes

* **doc:** rename Remedials to RetakeExam and refactor components ([0356a58](https://github.com/hei-school/hei-admin-api/commit/0356a58e8643194e96d7367aab00c189b5fa54ee))



