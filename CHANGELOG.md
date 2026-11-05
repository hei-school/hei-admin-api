# [1.185.0](https://github.com/hei-school/hei-admin-api/compare/v1.184.1...v1.185.0) (2026-10-30)


### Bug Fixes

* badges fixed at 88 x 56 mm, without page margin ([cd36371](https://github.com/hei-school/hei-admin-api/commit/cd3637128bb36316c0f8456d08f5cc91a0aa8eb3))


### Features

* add another status for complaint ([d97c9b8](https://github.com/hei-school/hei-admin-api/commit/d97c9b864c80561d37e8cf387884915f2363e57a))
* reclamation system ([9d097e1](https://github.com/hei-school/hei-admin-api/commit/9d097e11aa280698fa16ac2ee3771a702e8d24ff))



## [1.184.1](https://github.com/hei-school/hei-admin-api/compare/v1.184.0...v1.184.1) (2026-10-16)


### Bug Fixes

* befiana bearer token error ([ffd8209](https://github.com/hei-school/hei-admin-api/commit/ffd8209b05d0cad16911d7d4bd410db2ec17a644))
* befiana number format to e164 and set sendAt mandatory ([f724470](https://github.com/hei-school/hei-admin-api/commit/f724470bc8bfede84352ffa0a7b6420a7a013435))



# [1.184.0](https://github.com/hei-school/hei-admin-api/compare/v1.180.0...v1.184.0) (2026-10-16)


### Bug Fixes

* badge QR code links to /badges/{id} ([dc427c4](https://github.com/hei-school/hei-admin-api/commit/dc427c4896beb7138c2d26fadf4ade0296545b82))
* FailedFeeByMpbsNotification npe block mailling service ([a90bda5](https://github.com/hei-school/hei-admin-api/commit/a90bda5e16cc9a28b216c09454567477ebaf8a9e))
* increase client version ([cbedd6e](https://github.com/hei-school/hei-admin-api/commit/cbedd6e740ba2a5e87b8ee3daaf2274f0a86a024))
* retake exam and student result overview pagination ([7bd8703](https://github.com/hei-school/hei-admin-api/commit/7bd87030fbd4548db0e02bcfdcb62a9a06b8a2eb))
* save badges once their pdf is printed, download photos in parallel, no badge for students who left ([39c027c](https://github.com/hei-school/hei-admin-api/commit/39c027c91e913e9d2b1356a0f908535f9877c5ca))


### Features

* create retake exam fee auto while creating retake exam ([436ce8f](https://github.com/hei-school/hei-admin-api/commit/436ce8fb653774fc6340100fc78e41cd0d550a6b))
* encrypted public badge with late fees, automatic attendance for teachers, no expiration after licence ([8afd902](https://github.com/hei-school/hei-admin-api/commit/8afd9020ae93eb9ffcc689e6c5748fe9ab34dde8))
* export student retake exam per session or course ([f5a3b10](https://github.com/hei-school/hei-admin-api/commit/f5a3b10afd3d101ee66341b3667cde2f52650987))
* migration Sms By Befiana V1 to V2 ([035a2c4](https://github.com/hei-school/hei-admin-api/commit/035a2c4b14df723539e6c8a2194ba6d94b94c19c))
* public badge at /badges, nothing on revoked badges, no expiration from L3 and student situation ([a5091f9](https://github.com/hei-school/hei-admin-api/commit/a5091f9baef179ab27dc2e833b78afa16880c864))



# [1.180.0](https://github.com/hei-school/hei-admin-api/compare/v1.179.0...v1.180.0) (2026-10-07)


### Bug Fixes

* public badge denied behind the lambda function url ([e051b21](https://github.com/hei-school/hei-admin-api/commit/e051b21bc73f2098701f0a66e22d0beef91b6da8))
* update API URLs ([ca27c96](https://github.com/hei-school/hei-admin-api/commit/ca27c9628b8f99b68c79814c11af0804848ed90f))


### Features

* add actual balance in credit transaction ([36433e3](https://github.com/hei-school/hei-admin-api/commit/36433e32e336b1a9e78c7d6c6a1fe6154223125e))
* badges valid for one academic year, level from student promotions ([1d78b5c](https://github.com/hei-school/hei-admin-api/commit/1d78b5ca1adc08b93aa2e7dee5f04e0170731ec1))
* only one badge in circulation per student and academic year ([5aeca71](https://github.com/hei-school/hei-admin-api/commit/5aeca7168441e2215cdefcaaa3e561f9d80d2b46))
* print or remove the badge of one student ([3493564](https://github.com/hei-school/hei-admin-api/commit/3493564ce17c1bc2449214bdda053b0c2e6436a3))



# [1.179.0](https://github.com/hei-school/hei-admin-api/compare/v1.178.0...v1.179.0) (2026-10-01)


### Bug Fixes

* fetch contact's group ([8e85b1c](https://github.com/hei-school/hei-admin-api/commit/8e85b1ce284780409523cb596a8fadf7da53a29e))


### Features

* implementation badge ([0fb9f52](https://github.com/hei-school/hei-admin-api/commit/0fb9f521447af4647d75c2bf33c5887cc9c8e2d6))



# [1.178.0](https://github.com/hei-school/hei-admin-api/compare/v1.177.0...v1.178.0) (2026-10-01)


### Features

* return linked student while getting monitor's contact ([a592dc6](https://github.com/hei-school/hei-admin-api/commit/a592dc6461a46f93117f94d0246a6ca87668aa46))



# [1.177.0](https://github.com/hei-school/hei-admin-api/compare/v1.176.0...v1.177.0) (2026-09-30)


### Bug Fixes

* **documenso:** keep a failed filing from rolling the fiche back ([1faab58](https://github.com/hei-school/hei-admin-api/commit/1faab585a46c620558497e7cc89c2e06fd274c76))
* payment creation by admin ([f3571e2](https://github.com/hei-school/hei-admin-api/commit/f3571e2c386381ba6b3c192ea74c6ea91a852ba1))
* payment duplication ([129eb1f](https://github.com/hei-school/hei-admin-api/commit/129eb1f36f784d093cd3a070b81c2f648b10baf5))


### Features

* create group contact automatically while creating a student group ([82c34dd](https://github.com/hei-school/hei-admin-api/commit/82c34dd4f23dda38d34432f3adfe0afaaecfea9d))



# [1.176.0](https://github.com/hei-school/hei-admin-api/compare/v1.175.0...v1.176.0) (2026-09-25)


### Bug Fixes

* remove unused status and send sms by file ([d33ab00](https://github.com/hei-school/hei-admin-api/commit/d33ab0008a4458ac9ae419ef8447d335dab54ba1))


### Features

* documenso folders ([1cb2d07](https://github.com/hei-school/hei-admin-api/commit/1cb2d07f46f062e6c98c787b9c029460ff321450))



# [1.175.0](https://github.com/hei-school/hei-admin-api/compare/v1.174.0...v1.175.0) (2026-09-24)


### Features

* file generated fiches by year and level ([3de781e](https://github.com/hei-school/hei-admin-api/commit/3de781e7a4ab3d9da28676105b850da88960e852))



# [1.174.0](https://github.com/hei-school/hei-admin-api/compare/v1.173.1...v1.174.0) (2026-09-24)


### Bug Fixes

* allow user search without accents ([be290b0](https://github.com/hei-school/hei-admin-api/commit/be290b05d991abb7b690a3d6ecc9052e8c5b3e4b))
* duplicate payment and repeated verification ([3d3961f](https://github.com/hei-school/hei-admin-api/commit/3d3961f03837b5791091a07f485b96ec8cf8d469))
* send sms compaign ([cf8c7db](https://github.com/hei-school/hei-admin-api/commit/cf8c7dbd3f62380c57dfc2299e9d5c5704e5cce3))


### Features

* SmsBefiana integration ([f503b30](https://github.com/hei-school/hei-admin-api/commit/f503b30cfad326cf6a5a28536cff3bbee7c4b2f7))



