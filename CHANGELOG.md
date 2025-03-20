# [1.100.0](https://github.com/hei-school/hei-admin-api/compare/v1.99.1...v1.100.0) (2025-03-20)


### Bug Fixes

* publish client workflow ([1c50fb1](https://github.com/hei-school/hei-admin-api/commit/1c50fb11c2fd71082bd17e8251ebe728b03ad745))
* S3 bucket key to not include random numbers ([bd1a616](https://github.com/hei-school/hei-admin-api/commit/bd1a616cc489f89fad0a5255db50973ff48caaad))


### Features

* announcement react ([09a1d90](https://github.com/hei-school/hei-admin-api/commit/09a1d9095d3cf21dde55e822bbe23fe6b82d17a3))
* **not-implemented:** update multiple student grade ([c94a35f](https://github.com/hei-school/hei-admin-api/commit/c94a35f89f3e0b67211e68c75b7e7f40fe5c4181))


### Reverts

* Revert "infra(to-revert): rm synchronisation with release" ([da03dc7](https://github.com/hei-school/hei-admin-api/commit/da03dc7fce99ca5dcf26931a6089c7af9206c38b))



## [1.99.1](https://github.com/hei-school/hei-admin-api/compare/v1.99.0...v1.99.1) (2025-03-12)


### Bug Fixes

* advanced fee statistics scheduler wrong event type sent ([7546133](https://github.com/hei-school/hei-admin-api/commit/754613334feb5a5a60ec0bd3f569b4913cf1b87e))
* wrong event consumer class name ([2efd6e8](https://github.com/hei-school/hei-admin-api/commit/2efd6e81816589886d84e25908e1dc3fe7d71c34))



# [1.99.0](https://github.com/hei-school/hei-admin-api/compare/v1.98.0...v1.99.0) (2025-03-04)


### Bug Fixes

* advanced fee stats compute event ([72f94a8](https://github.com/hei-school/hei-admin-api/commit/72f94a8b99b5d1cb084cb7dc1302589e2c737792))
* correct count of event create by frequency ([69da6ad](https://github.com/hei-school/hei-admin-api/commit/69da6adfc93aeebb756171a40aad4e34ab835151))
* creation event by frequency ([76b6cdb](https://github.com/hei-school/hei-admin-api/commit/76b6cdb0da66a01a787fa62586545e70b643d647))
* handle null comment on fee advanced stats ([75973dd](https://github.com/hei-school/hei-admin-api/commit/75973dd607cd9aa888c994be926bec95af6defe6))
* handle null value in export students as xlsx function params ([d304806](https://github.com/hei-school/hei-admin-api/commit/d304806349e3b8dfe9f8a0eb333f1d8f32a40647))
* start of event create by frequency ([0aec301](https://github.com/hei-school/hei-admin-api/commit/0aec3010483cffa0fc28a0448a4ee965ce4b1509))


### Features

* **not-implemented:** announcements reaction ([01d72cd](https://github.com/hei-school/hei-admin-api/commit/01d72cdaad44d97cf33c6ba2bb76c031b5f095e1))


### Reverts

* Revert "promotion: preprod to prod" ([9047d5d](https://github.com/hei-school/hei-admin-api/commit/9047d5d1a535750320e757df768f9dff85b4ec6a))



# [1.98.0](https://github.com/hei-school/hei-admin-api/compare/v1.97.0...v1.98.0) (2025-02-14)


### Features

* add advanced fees statistics ([c9d6e87](https://github.com/hei-school/hei-admin-api/commit/c9d6e873524f0854d82c050e6ee0a7cc6f4d8aa6))



# [1.97.0](https://github.com/hei-school/hei-admin-api/compare/v1.96.0...v1.97.0) (2025-02-11)


### Features

* crupdate organizers ([a7fe681](https://github.com/hei-school/hei-admin-api/commit/a7fe681a3fa1230d3026f7257ebff95e43d73780))
* role organizer ([9cf27fd](https://github.com/hei-school/hei-admin-api/commit/9cf27fd7e3785988b873424ec750f3675c104917))



# [1.96.0](https://github.com/hei-school/hei-admin-api/compare/v1.95.0...v1.96.0) (2025-02-06)


### Features

* GET /event/participants/{participant_id}/stats ([6c752a5](https://github.com/hei-school/hei-admin-api/commit/6c752a53611a27bb3dabae8372d26ea28b8332cb))
* get event stats ([eb7bd48](https://github.com/hei-school/hei-admin-api/commit/eb7bd48b5a0d0108ad1f8715e5572a14edef1516))
* get students exam grades  ([0fe0c42](https://github.com/hei-school/hei-admin-api/commit/0fe0c426bec33b7b68ddd960b65503a098f47cd5))
* mail student on failed mpbs  ([aebd92c](https://github.com/hei-school/hei-admin-api/commit/aebd92c8d6d9b8e1f64ef58308cb09244da2b4da))



# [1.95.0](https://github.com/hei-school/hei-admin-api/compare/v1.94.0...v1.95.0) (2025-01-29)


### Bug Fixes

* event participant  ([7338d7b](https://github.com/hei-school/hei-admin-api/commit/7338d7b1cc78d0bcdbf6c1af5b0102bb23c58d78))
* make PUT /events indempotant  ([1d7ca19](https://github.com/hei-school/hei-admin-api/commit/1d7ca196549e6e17fcba8ab9890ee43596153a7c))


### Features

* mail on participant missing event ([8ccff8d](https://github.com/hei-school/hei-admin-api/commit/8ccff8d218ce02db642ceeaca070432e15e1fd39))
* **not-implemented:** delete events by id  ([ee8ff2c](https://github.com/hei-school/hei-admin-api/commit/ee8ff2cfa8162994ce6a92ebb35ac0ae29d5121f))



# [1.94.0](https://github.com/hei-school/hei-admin-api/compare/v1.93.0...v1.94.0) (2025-01-22)


### Bug Fixes

* utc when generating datetime  ([8d14d3d](https://github.com/hei-school/hei-admin-api/commit/8d14d3df5f0b18683e8e197caa153708822c0630))


### Features

* create event with some days frequence  ([cfdc043](https://github.com/hei-school/hei-admin-api/commit/cfdc043f619cfad7f11a00e76285f7005afa8b16))
* export students and teachers as xlsx  ([a61ddc5](https://github.com/hei-school/hei-admin-api/commit/a61ddc57d0f8d5060412f5084c4c51571fa3d598))



# [1.93.0](https://github.com/hei-school/hei-admin-api/compare/v1.92.0...v1.93.0) (2025-01-17)


### Features

* group has color and mapp group color from event creation  ([55c3634](https://github.com/hei-school/hei-admin-api/commit/55c3634540e78f964760b018847b05db596bc72e))



# [1.92.0](https://github.com/hei-school/hei-admin-api/compare/v1.91.0...v1.92.0) (2025-01-17)


### Features

* **not-implemented:** add event frequency number  ([41fc946](https://github.com/hei-school/hei-admin-api/commit/41fc94674cb01cfe847048e8ee51d096a3b4d5b9))



