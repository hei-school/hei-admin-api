# [1.43.0](https://github.com/hei-school/hei-admin-api/compare/v1.42.0...v1.43.0) (2024-08-21)


### Bug Fixes

* group sorted by ref ordered ASC ([96faf97](https://github.com/hei-school/hei-admin-api/commit/96faf9702c7f365d2f090f6debb789aca74c054a))
* template image won't load, fallback to background-image  ([168c76e](https://github.com/hei-school/hei-admin-api/commit/168c76ef1ddf431511bcf85ed7baba1c31a073e0))


### Features

* filter for security conf to disable user suspended  ([4c8494b](https://github.com/hei-school/hei-admin-api/commit/4c8494bc1fe962267cb14abe508ee22c2d6ae81d))
* implementation of repeating student of current year  ([bd6aa91](https://github.com/hei-school/hei-admin-api/commit/bd6aa91152d7f7d3e4e794f5ca8aa7109f925aa6))



# [1.42.0](https://github.com/hei-school/hei-admin-api/compare/v1.41.0...v1.42.0) (2024-08-09)


### Features

* add mpbs field for the last verification datetime  ([f1c56e9](https://github.com/hei-school/hei-admin-api/commit/f1c56e90b86c8b0c4484ed40ec5c56dc236e7093))
* **not-implemented:** add endpoint for courses with teacher ([adb454b](https://github.com/hei-school/hei-admin-api/commit/adb454b6292aba95fc29ae59369656e8df0409c5))
* **not-implemented:** add field for repeating student  ([2b263cb](https://github.com/hei-school/hei-admin-api/commit/2b263cbc9e47b41264504b889902b5b08a1e3914))
* notify user after fee is paid by mpbs  ([05ff9fb](https://github.com/hei-school/hei-admin-api/commit/05ff9fb9e2bc1429d1a132b17869a4a73046b23d))



# [1.41.0](https://github.com/hei-school/hei-admin-api/compare/v1.40.0...v1.41.0) (2024-08-01)


### Features

* compute user status by mpbs ([3afce0b](https://github.com/hei-school/hei-admin-api/commit/3afce0b3a8846bed2d09232a50247d77e15e69ac))
* **not-implemented:** add field for mpbs payload and add last verification  ([95c5193](https://github.com/hei-school/hei-admin-api/commit/95c51937e37d0cf52023478da7bdcdd3f5cdd1fe))
* sort group and promotion by ref and creation datetime DESC  ([04e688a](https://github.com/hei-school/hei-admin-api/commit/04e688af52ba9053e3cc5634ca5d2b6c0c5baeea))



# [1.40.0](https://github.com/hei-school/hei-admin-api/compare/v1.39.0...v1.40.0) (2024-07-25)


### Features

* new type for professional experience and add new field on student information  ([66c383e](https://github.com/hei-school/hei-admin-api/commit/66c383e5f5b7d8b39adefe0d5347d4b6231f574b))



# [1.39.0](https://github.com/hei-school/hei-admin-api/compare/v1.38.0...v1.39.0) (2024-07-25)


### Bug Fixes

* students alternating stats are now exact ([b21671f](https://github.com/hei-school/hei-admin-api/commit/b21671f900402c23e84578c179a4e62e087ae0c3))


### Features

* **not-implemented:** student professional experience status documentation  ([d05a6b9](https://github.com/hei-school/hei-admin-api/commit/d05a6b97ed7f4e2a1a78ac70bdc5e395d0bd0b95))



# [1.38.0](https://github.com/hei-school/hei-admin-api/compare/v1.37.0...v1.38.0) (2024-07-25)


### Bug Fixes

* change basic auth encoding ([175a7cb](https://github.com/hei-school/hei-admin-api/commit/175a7cba50063a53b2a95051cf279edce9120783))


### Features

* **not-implemented:** upload new student work document file type  ([178a241](https://github.com/hei-school/hei-admin-api/commit/178a241590ce7941286d32bd8009ea445fb450ba))



# [1.37.0](https://github.com/hei-school/hei-admin-api/compare/v1.36.0...v1.37.0) (2024-07-24)


### Bug Fixes

* set up security conf properly for share link endpoint ([51cfeec](https://github.com/hei-school/hei-admin-api/commit/51cfeec48e98137baf4ffda1a817638440452148))


### Features

* filter fees by student ref ([c01cf7c](https://github.com/hei-school/hei-admin-api/commit/c01cf7c42bef5580408765dcc6cbeba55b3380de))



# [1.36.0](https://github.com/hei-school/hei-admin-api/compare/v1.35.0...v1.36.0) (2024-07-19)


### Features

* add student ref in fee payload and filter group student by firstname  ([47d2819](https://github.com/hei-school/hei-admin-api/commit/47d28192145581596c64067df285225b59488fbd))



# [1.35.0](https://github.com/hei-school/hei-admin-api/compare/v1.34.0...v1.35.0) (2024-07-19)


### Bug Fixes

* amount value when saving payment is given from mobile transaction  ([ca7e5f6](https://github.com/hei-school/hei-admin-api/commit/ca7e5f638223aa4367b0d60f58811a6a767afd0c))
* date validator when inserting work student file  ([38b918d](https://github.com/hei-school/hei-admin-api/commit/38b918d9a3029fa801ec199217cce4c8fbf6090a))
* orange transaction is not stored when computing student ref  ([c8ecd0e](https://github.com/hei-school/hei-admin-api/commit/c8ecd0e77385303bbdf261447b4754014b8d7419))


### Features

* filter students by exclude group id ([d26b542](https://github.com/hei-school/hei-admin-api/commit/d26b5426daeb3c521f29cc2fb6ead6b0b84da161))
* **temporary:** fetch transaction from 2024-07-12T08:00:00Z from orange  ([74060c6](https://github.com/hei-school/hei-admin-api/commit/74060c676fa5a5a997c82d26fe789ce620cc00b9))
* **temporary:** fetch transaction from 2024-07-12T08:00:00Z from orange  ([dbc7bd0](https://github.com/hei-school/hei-admin-api/commit/dbc7bd064835dc118f8f467d7a63abb9722eb7e7))



# [1.34.0](https://github.com/hei-school/hei-admin-api/compare/v1.33.0...v1.34.0) (2024-07-11)


### Bug Fixes

* comment and fix the algorithm process to compute mpbs status  ([e2e4e5d](https://github.com/hei-school/hei-admin-api/commit/e2e4e5de486c9eb31106d8e4a58b9ab30f55315e))
* computing mpbs status fail if the transaction details is null …  ([16497f7](https://github.com/hei-school/hei-admin-api/commit/16497f7ce57041879d7797858d2615d23a820a9a))


### Features

* filter group by ref and student ref ([3ff5b82](https://github.com/hei-school/hei-admin-api/commit/3ff5b82cfb13ba14f435412d9ac7b13cbdb90b3d))
* payment by mobile money  ([09732dc](https://github.com/hei-school/hei-admin-api/commit/09732dc5333665160f3a7b3ea07705541acb65f5))



