# [1.153.0](https://github.com/hei-school/hei-admin-api/compare/v1.152.0...v1.153.0) (2026-03-04)


### Bug Fixes

* is_deleted handling in fee joined queries and update @Where annotation ([dc92249](https://github.com/hei-school/hei-admin-api/commit/dc922499965e2d6e9d44b01631c593ba7eff743a))


### Features

* add category param in get fee ([72c9db6](https://github.com/hei-school/hei-admin-api/commit/72c9db67a2612cf73ed2b85b88b12acfa8ccdde5))
* vola progressive integration ([888f708](https://github.com/hei-school/hei-admin-api/commit/888f7089de6c21dc8aec4b2929b02d883342b3b8))



# [1.152.0](https://github.com/hei-school/hei-admin-api/compare/v1.151.0...v1.152.0) (2026-02-26)


### Bug Fixes

* add interviewers in toDomainUpdate ([b2b3ec9](https://github.com/hei-school/hei-admin-api/commit/b2b3ec9ca8d68ce3076d4ad3d2c562ab092c54e2))
* advanced fees stats ([0f6d08b](https://github.com/hei-school/hei-admin-api/commit/0f6d08b11d1be6c5124cc9c1626906d31293caa8))
* **auth:** export grade template authorization ([8623183](https://github.com/hei-school/hei-admin-api/commit/8623183676d7fb304540e5d8c3d966b1413f9c6c))
* cor creation dateTime on update ([d9f6805](https://github.com/hei-school/hei-admin-api/commit/d9f68058bdcce82d5c6f8ba6f3247f95b74fd611))
* fees stats file extension ([f5ffa26](https://github.com/hei-school/hei-admin-api/commit/f5ffa26d6d791f5ab07cf762d12472653a6d335d))
* fix corService' ([ac62663](https://github.com/hei-school/hei-admin-api/commit/ac6266359c452ffb216d47d9e7a123cbfc847c54))
* generate grade template ([3af7a65](https://github.com/hei-school/hei-admin-api/commit/3af7a658386744ecd4e2338510593013b27ec620))
* grade template generator ([fadd925](https://github.com/hei-school/hei-admin-api/commit/fadd9253a5b4c0c44245aa94fb6ce00ee6237353))
* prevent ScholarCertificate for past students by adding a cycleLevel to Promotion ([a03ec13](https://github.com/hei-school/hei-admin-api/commit/a03ec139de02c916a443d699ecf329f3b4a37809))
* remove interviewer in CorMapper ([1f0fce3](https://github.com/hei-school/hei-admin-api/commit/1f0fce3d3b8fa5224fc338c12cffbd676f9756cc))


### Features

* (doc)  client version ([4879ff0](https://github.com/hei-school/hei-admin-api/commit/4879ff0cc003dcc79fbc0cc89c2e54fbd79b7cc2))
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



# [1.146.0](https://github.com/hei-school/hei-admin-api/compare/v1.145.2...v1.146.0) (2025-12-24)


### Features

* (doc) add new endpoint for grade template ([7706546](https://github.com/hei-school/hei-admin-api/commit/77065467e84e1a958077f60a65667c77b87f21ad))



## [1.145.2](https://github.com/hei-school/hei-admin-api/compare/v1.145.1...v1.145.2) (2025-12-23)


### Bug Fixes

* update grade ([0178032](https://github.com/hei-school/hei-admin-api/commit/0178032ad80b1e2b06c28b7cb4c32e811208f2c1))



## [1.145.1](https://github.com/hei-school/hei-admin-api/compare/v1.145.0...v1.145.1) (2025-12-19)


### Bug Fixes

* (doc) update grade via excel file ([36641ab](https://github.com/hei-school/hei-admin-api/commit/36641ab566f12aeba745602782682739ceb0f49d))
* import updated grade ([a197587](https://github.com/hei-school/hei-admin-api/commit/a197587bf118bbc3eac4538f350a1c2fcef4bc3c))



