# [1.115.0](https://github.com/hei-school/hei-admin-api/compare/v1.114.0...v1.115.0) (2025-09-05)


### Bug Fixes

* not started courseResult ([e5b03ab](https://github.com/hei-school/hei-admin-api/commit/e5b03ab70085edc0856b3ad4c5329829d92ead29))


### Features

* filter exam grades by student ref ([a1320f2](https://github.com/hei-school/hei-admin-api/commit/a1320f2b334d0eab968f30ca52e0fb259380fc95))



# [1.114.0](https://github.com/hei-school/hei-admin-api/compare/v1.113.1...v1.114.0) (2025-09-05)


### Bug Fixes

* result summary computation ([be8119b](https://github.com/hei-school/hei-admin-api/commit/be8119ba92000785225343b4f90e67ab974a9a39))


### Features

* filter exam by teacher id ([44e68de](https://github.com/hei-school/hei-admin-api/commit/44e68def3e5be2214d49ed682fc3fb55ac512314))



## [1.113.1](https://github.com/hei-school/hei-admin-api/compare/v1.113.0...v1.113.1) (2025-08-29)


### Bug Fixes

* add missing type query parameter to getAdvancedFeeStats ([6f13992](https://github.com/hei-school/hei-admin-api/commit/6f139924eb119a32659058f786898977a6ce6093))
* handle runtime exceptions in mpbs verifications ([6e94e2b](https://github.com/hei-school/hei-admin-api/commit/6e94e2bb9df7c4530c2b92354ac3d597e866e014))



# [1.113.0](https://github.com/hei-school/hei-admin-api/compare/v1.111.0...v1.113.0) (2025-08-29)


### Bug Fixes

* exam participants pagination ([96cea02](https://github.com/hei-school/hei-admin-api/commit/96cea0211225fab2326f210c09ea9c09bf3027bc))
* **SecurityConf:** antMacher(GET, "/students/*/attendance") ([16664ef](https://github.com/hei-school/hei-admin-api/commit/16664ef63663b3d4a3afc58c4840e4eb2e2cfcb4))


### Features

* add receipt advanced fee stats ([cf9d4a8](https://github.com/hei-school/hei-admin-api/commit/cf9d4a81d01948fef8708d4ebbd89cb09e9464d0))
* GET /students/{id}/attendance ([445aed9](https://github.com/hei-school/hei-admin-api/commit/445aed91fb2dff43e3a8ff5ac3980d68c90221da))
* **not-implemented:** pend failed mpbs ([e5db94d](https://github.com/hei-school/hei-admin-api/commit/e5db94dd834f59f4d770a2d764edf2b786effbdb))



# [1.111.0](https://github.com/hei-school/hei-admin-api/compare/v1.110.2...v1.111.0) (2025-08-26)


### Bug Fixes

* mpbs xlsx verification failing on new excel format ([1c4916d](https://github.com/hei-school/hei-admin-api/commit/1c4916dbb719e388f9f6831f8b355fe03abe21e3))


### Features

* **not-implemented:** get grade change history and additional filter ([b626c9b](https://github.com/hei-school/hei-admin-api/commit/b626c9ba939a4c7d3b7dc69628ff98d2914e00f1))



## [1.110.2](https://github.com/hei-school/hei-admin-api/compare/v1.110.1...v1.110.2) (2025-08-22)


### Bug Fixes

* grade mapper ([7ae21f8](https://github.com/hei-school/hei-admin-api/commit/7ae21f8aac9585bc28f3260d7086f8c0834c57de))



## [1.110.1](https://github.com/hei-school/hei-admin-api/compare/v1.110.0...v1.110.1) (2025-08-20)


### Bug Fixes

* create and update grades ([00d4bb3](https://github.com/hei-school/hei-admin-api/commit/00d4bb3431cfcbe1388e7b370a27dd6dc5b574d2))
* handle student without course results ([bc37732](https://github.com/hei-school/hei-admin-api/commit/bc3773287515b3e6abe06f1161061e57da9cf33c))
* increase stack size for build ([2efe473](https://github.com/hei-school/hei-admin-api/commit/2efe473db3f99b9ae62c41958f5e798cb2bdeb17))
* multiple course results on multiple awarded course ([e4ddede](https://github.com/hei-school/hei-admin-api/commit/e4ddede537728830791d4fd25d08f45a29087316))



# [1.110.0](https://github.com/hei-school/hei-admin-api/compare/v1.109.0...v1.110.0) (2025-08-14)


### Bug Fixes

* getResultsSummary error on irrational numbers ([a10ecdd](https://github.com/hei-school/hei-admin-api/commit/a10ecdd6d15231d6029a699b88b6c1fc4c899b84))


### Features

* generate yearly result pdf ([0155b1d](https://github.com/hei-school/hei-admin-api/commit/0155b1d28cd90baa5605fb4891d3c32e34083e1a))
* grade change history ([35c6221](https://github.com/hei-school/hei-admin-api/commit/35c62218f71e55b8495d125a854bb3bd791dc147))



# [1.109.0](https://github.com/hei-school/hei-admin-api/compare/v1.108.0...v1.109.0) (2025-08-12)


### Bug Fixes

* getStudentById Monitor ([d5be86f](https://github.com/hei-school/hei-admin-api/commit/d5be86f554fc2274239fde5eda37ac25aa02b302))


### Features

* YearlyResult transcript ([25f4552](https://github.com/hei-school/hei-admin-api/commit/25f4552735972ba0cc4314490b7d3cb4836ed18e))



# [1.108.0](https://github.com/hei-school/hei-admin-api/compare/v1.107.0...v1.108.0) (2025-08-07)


### Bug Fixes

* do not apply coefficient when crupdate grade ([ebc1f10](https://github.com/hei-school/hei-admin-api/commit/ebc1f100d114f5e30f4367d5e0aa8eeacdad9a13))
* yearly result security conf ([f849816](https://github.com/hei-school/hei-admin-api/commit/f849816e979e5a401df3b9ccf03e3444ec393b6d))


### Features

* add getStudentByIdAndMonitorId for monitors ([fc0afe0](https://github.com/hei-school/hei-admin-api/commit/fc0afe0941868e6c33ba782bf83e8bd4063415ba))



