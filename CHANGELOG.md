# [1.105.0](https://github.com/hei-school/hei-admin-api/compare/v1.104.2...v1.105.0) (2025-07-24)


### Features

* **not-implemented:** add course_results and yearly_results endpoint ([5044897](https://github.com/hei-school/hei-admin-api/commit/50448978e5639aa45420fa75b2dfeabed7c96105))



## [1.104.2](https://github.com/hei-school/hei-admin-api/compare/v1.104.1...v1.104.2) (2025-07-24)


### Bug Fixes

* advanced fee stats not updating expired field ([db595c1](https://github.com/hei-school/hei-admin-api/commit/db595c1583bd5f1b6272f5103955ddf3c95a4ede))



## [1.104.1](https://github.com/hei-school/hei-admin-api/compare/v1.104.0...v1.104.1) (2025-07-10)


### Bug Fixes

* advanced fee stats null stat_date on update ([887a199](https://github.com/hei-school/hei-admin-api/commit/887a1994f69dabbb72d15f434c504e1bbdd2745b))
* db backup neon ([2ff60d0](https://github.com/hei-school/hei-admin-api/commit/2ff60d0b4058e775e5d9f0c45c242d2d884c9759))
* not failling mpbs ([45811cf](https://github.com/hei-school/hei-admin-api/commit/45811cfa844a9a4bffb0e9a4ab6e5c66c1d4d0e2))


### Reverts

* Revert "promotion: preprod to prod" ([c21886b](https://github.com/hei-school/hei-admin-api/commit/c21886bf666f864d3043d82adaad540a6bff0cec))



# [1.104.0](https://github.com/hei-school/hei-admin-api/compare/v1.103.1...v1.104.0) (2025-05-27)


### Bug Fixes

* casdoor custom redirect uri, add casdoor env vars ([9be5583](https://github.com/hei-school/hei-admin-api/commit/9be5583937d4734648c67d2f756e040da89c54b3))
* student status going from disabled to suspended ([2d9ffd1](https://github.com/hei-school/hei-admin-api/commit/2d9ffd1d798404459ccbdf9afe3388ffb429558e))


### Features

* casdoor authentication ([#890](https://github.com/hei-school/hei-admin-api/issues/890)) ([5679e1e](https://github.com/hei-school/hei-admin-api/commit/5679e1e68dbfa6a902132204943d4a7864a4c3b9))
* **not-implemented:** course grade and exam grades stats ([b16f960](https://github.com/hei-school/hei-admin-api/commit/b16f960bae3165660fee6758fbba6a2fc6ce9b66))


### Reverts

* Revert "chore: fix casdoor env variables" (#893) ([615ff34](https://github.com/hei-school/hei-admin-api/commit/615ff34cb520cee058e0920a49461ffbfbbc0a6c)), closes [#893](https://github.com/hei-school/hei-admin-api/issues/893)



## [1.103.1](https://github.com/hei-school/hei-admin-api/compare/v1.103.0...v1.103.1) (2025-05-07)


### Bug Fixes

* fee having list of mpbs ([074b6d2](https://github.com/hei-school/hei-admin-api/commit/074b6d2372cb0ad4e6bc99c2da3c22d694f0bcc0))



# [1.103.0](https://github.com/hei-school/hei-admin-api/compare/v1.102.1...v1.103.0) (2025-04-25)


### Bug Fixes

* advanced fee statistics generation endpoint ([e3e6398](https://github.com/hei-school/hei-admin-api/commit/e3e63980a906115df8f04c0be2a0bd7ecfcd9b6a))
* getAllEventParticipants without date parameters ([f0c20f1](https://github.com/hei-school/hei-admin-api/commit/f0c20f190d8657b1853eaa262eb6b245ceb3163e))


### Features

* get all event attendances & filter by status ([4bb28d6](https://github.com/hei-school/hei-admin-api/commit/4bb28d6aec5c84786f1e8d70abb8b6e797c7cc52))
* getAllEventParticipants filters ([a5be603](https://github.com/hei-school/hei-admin-api/commit/a5be60386d3dbc6a9fffc98fae981a7e851a7b94))



## [1.102.1](https://github.com/hei-school/hei-admin-api/compare/v1.102.0...v1.102.1) (2025-04-04)


### Bug Fixes

* modify grade requirement ([703e1d1](https://github.com/hei-school/hei-admin-api/commit/703e1d1d4c73ed7705ccfe63fd4e9fb1ce41b17a))



# [1.102.0](https://github.com/hei-school/hei-admin-api/compare/v1.101.0...v1.102.0) (2025-04-03)


### Bug Fixes

* disabled user re-enabled ([5ede7bb](https://github.com/hei-school/hei-admin-api/commit/5ede7bb2c95d867dc233ad32379fa54a91dd7c80))


### Features

* generate advanced fee stats  for given date range ([979fe44](https://github.com/hei-school/hei-admin-api/commit/979fe44c6248e5c326b665df9108760a6aeb21ce))
* **not-implemented:** get all event attendances ([46a0826](https://github.com/hei-school/hei-admin-api/commit/46a0826d1a582c8cfe7d3f3b52cd3931c06749ec))



# [1.101.0](https://github.com/hei-school/hei-admin-api/compare/v1.100.0...v1.101.0) (2025-03-26)


### Features

* add category and frequency to fees, c9d39277 ([459ddac](https://github.com/hei-school/hei-admin-api/commit/459ddac47a5e06e4cd3c321a8c4a11e9890c5a01))



# [1.100.0](https://github.com/hei-school/hei-admin-api/compare/v1.99.1...v1.100.0) (2025-03-20)


### Bug Fixes

* publish client workflow ([1c50fb1](https://github.com/hei-school/hei-admin-api/commit/1c50fb11c2fd71082bd17e8251ebe728b03ad745))
* S3 bucket key to not include random numbers ([bd1a616](https://github.com/hei-school/hei-admin-api/commit/bd1a616cc489f89fad0a5255db50973ff48caaad))


### Features

* announcement react ([09a1d90](https://github.com/hei-school/hei-admin-api/commit/09a1d9095d3cf21dde55e822bbe23fe6b82d17a3))
* **not-implemented:** update multiple student grade ([c94a35f](https://github.com/hei-school/hei-admin-api/commit/c94a35f89f3e0b67211e68c75b7e7f40fe5c4181))


### Reverts

* Revert "infra(to-revert): rm synchronisation with release" ([da03dc7](https://github.com/hei-school/hei-admin-api/commit/da03dc7fce99ca5dcf26931a6089c7af9206c38b))



