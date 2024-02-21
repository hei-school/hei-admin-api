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



# [0.13.0](https://github.com/hei-school/hei-admin-api/compare/v0.12.0...v0.13.0) (2023-12-21)


### Bug Fixes

* map fee status to paid if remaining amount is 0 ([d0abc2b](https://github.com/hei-school/hei-admin-api/commit/d0abc2b1179ce01a2f8d9369696705b40f50394b))
* missing update adding migration to update paid fee status ([135ea1a](https://github.com/hei-school/hei-admin-api/commit/135ea1ad6ef06ed239712cfeb6acad0237a3af57))


### Features

* add birth place and nic to user attributes ([0ba9ac2](https://github.com/hei-school/hei-admin-api/commit/0ba9ac24cc0549eb541aa3b03b38a7d7cee28f63))
* permit teacher, manager, student update ([4eec73b](https://github.com/hei-school/hei-admin-api/commit/4eec73b586b739e921a27b192051c3887031f5b0))



# [0.12.0](https://github.com/hei-school/hei-admin-api/compare/v0.11.0...v0.12.0) (2023-12-01)


### Features

* **docs/api:** permit student, manager, teacher update ([6d3e009](https://github.com/hei-school/hei-admin-api/commit/6d3e009e56dd4464cd62759588c0b6761c558163))
* filter users by status and sex ([0c3b713](https://github.com/hei-school/hei-admin-api/commit/0c3b71368eb94b123910f3fc876152c3d81177f3))



# [0.11.0](https://github.com/hei-school/hei-admin-api/compare/v0.10.0...v0.11.0) (2023-12-01)


### Features

* upgrade poja to v4.0.0 ([9a55b49](https://github.com/hei-school/hei-admin-api/commit/9a55b495d6579d70d4988c581866679a05e7f17d))



# [0.10.0](https://github.com/hei-school/hei-admin-api/compare/v0.9.0...v0.10.0) (2023-11-30)


### Bug Fixes

* status in creation fees ([525c5b3](https://github.com/hei-school/hei-admin-api/commit/525c5b3070e60fd5e620f12ef6cf95c869625206))


### Features

* add update fees in spec ([b031fbe](https://github.com/hei-school/hei-admin-api/commit/b031fbef56684348f15cf888c6d30559f4253e7e))
* **doc/api:** filter users by sex (not-implemented) ([2511c10](https://github.com/hei-school/hei-admin-api/commit/2511c1041168bf88607cec3d0f1a97e7f7ff237b))
* **doc/api:** filter users by status with new Suspended status (not-implemented) ([357059d](https://github.com/hei-school/hei-admin-api/commit/357059dbb4cd469bdabf7019d635bdeda13fd7d9))
* implement update fees in the code ([296412c](https://github.com/hei-school/hei-admin-api/commit/296412c27e0f1d0c1cca72812cadae974b3e9d09))



