# [1.7.0](https://github.com/hei-school/hei-admin-api/compare/v1.6.0...v1.7.0) (2024-03-01)


### Features

* manager and teacher can comment about student ([d293f43](https://github.com/hei-school/hei-admin-api/commit/d293f43e0f2b5e5ddff2d68ccae8507059eb0362))



# [1.6.0](https://github.com/hei-school/hei-admin-api/compare/v1.5.0...v1.6.0) (2024-03-01)


### Features

* validate filename to disable file extension when given it ([7336974](https://github.com/hei-school/hei-admin-api/commit/733697466134a871514c83158204df5f3c8928ef))



# [1.5.0](https://github.com/hei-school/hei-admin-api/compare/v1.4.1...v1.5.0) (2024-02-28)


### Bug Fixes

* manager can now update own ([c05698e](https://github.com/hei-school/hei-admin-api/commit/c05698e5b8a33ef4f51afbef7c0cfeaafbeb5f6b))


### Features

* event resources for attendance (not-implemented) ([c5f4d03](https://github.com/hei-school/hei-admin-api/commit/c5f4d033321e9902b5834f4a017a74f25d8cea36))
* get school file by id  ([83213b1](https://github.com/hei-school/hei-admin-api/commit/83213b118c53e4ca5e70f00ccc37397251926813))
* users have files stored on S3 ([94368fe](https://github.com/hei-school/hei-admin-api/commit/94368fe50593f3e6a87bd529ec28af3a9dd910d7))



## [1.4.1](https://github.com/hei-school/hei-admin-api/compare/v1.4.0...v1.4.1) (2024-02-23)


### Bug Fixes

* handle file uploads with MultipartFile ([7c9eaea](https://github.com/hei-school/hei-admin-api/commit/7c9eaea0434e46e329f7fee0a7356f19ed41d11c))



# [1.4.0](https://github.com/hei-school/hei-admin-api/compare/v1.3.0...v1.4.0) (2024-02-21)


### Features

* users have now longitude and latitude attributes as coordinates ([8d7e157](https://github.com/hei-school/hei-admin-api/commit/8d7e157a438cf34f6f22faa2b3311157aaada5d8))



# [1.3.0](https://github.com/hei-school/hei-admin-api/compare/v1.2.1...v1.3.0) (2024-02-15)


### Bug Fixes

* fee template crupdate endpoint supports both update and create ([e10e25f](https://github.com/hei-school/hei-admin-api/commit/e10e25f41a0a3918af6b8fdd169c15f92ebc22ff))
* missing school year in scholarship certificate  ([c04187f](https://github.com/hei-school/hei-admin-api/commit/c04187f9d5fe30c2bd7f24eb80fa2ea615ac9299))
* unauthorize student update profile  ([5838db1](https://github.com/hei-school/hei-admin-api/commit/5838db1c283e7915ee661e9eb954856f20ceca5a))


### Features

* enable soft delete for payment and fee ([1f1df9e](https://github.com/hei-school/hei-admin-api/commit/1f1df9ec8bc210334e595ea0b282b679f383005b))
* user upload profile picture  ([fc75db9](https://github.com/hei-school/hei-admin-api/commit/fc75db9b0798877dc71c3b54290c2acdb7520674))



## [1.2.1](https://github.com/hei-school/hei-admin-api/compare/v1.2.0...v1.2.1) (2024-01-26)


### Bug Fixes

* default user specialization field to common core ([042c9c1](https://github.com/hei-school/hei-admin-api/commit/042c9c11c0046c8202a6dea328c08478b0cfb85c))
* encode images to base64 before injecting in thymeleaf ([35da8aa](https://github.com/hei-school/hei-admin-api/commit/35da8aa8e01ecaf95acb59c40e6ddf29836c9c1e))
* implement fee template ([64c1439](https://github.com/hei-school/hei-admin-api/commit/64c143994fbbae819703aa1652e38ddf11371686))
* load css when generating pdf  ([4de3445](https://github.com/hei-school/hei-admin-api/commit/4de3445f0815087c6de93e9e7e197ba4cf7fffe2))
* payment validator ignores seconds when creating payment ([3bfa397](https://github.com/hei-school/hei-admin-api/commit/3bfa397939972a19ed7ded03af786c3a4a15a315))
* some user fields (nic, birthdate, birthplace) were not updated correctly ([1729d71](https://github.com/hei-school/hei-admin-api/commit/1729d712d46b33da6ef82714786fb8bc12c03123))



# [1.2.0](https://github.com/hei-school/hei-admin-api/compare/v1.1.0...v1.2.0) (2024-01-05)


### Bug Fixes

* use common core as default value for student ([ddff084](https://github.com/hei-school/hei-admin-api/commit/ddff08470b7393a192109fe6de728be51d68b8d9))


### Features

* **api:** add fee types endpoint(not-implemented) ([10e9608](https://github.com/hei-school/hei-admin-api/commit/10e9608e714f1bda39551df7b134cf1e1b44db4a))



# [1.1.0](https://github.com/hei-school/hei-admin-api/compare/v1.0.0...v1.1.0) (2024-01-05)


### Features

* handle scholarship certificate gen for a Student ([62f83e7](https://github.com/hei-school/hei-admin-api/commit/62f83e7856aa9274593ee1afdf729549cbda1f14))



# [1.0.0](https://github.com/hei-school/hei-admin-api/compare/v0.13.0...v1.0.0) (2024-01-05)


* feat!: add profile picture to user attributes, also separate crupdate payload from response payload for Users (Student, Manager, Teacher) ([0536cdf](https://github.com/hei-school/hei-admin-api/commit/0536cdf1b7b3d22fef6e7e5645fb8fef1d0cd023))


### BREAKING CHANGES

* separate crupdate payload from response components



