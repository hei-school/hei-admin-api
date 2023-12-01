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



# [0.9.0](https://github.com/hei-school/hei-admin-api/compare/v0.7.1...v0.9.0) (2023-11-28)


### Bug Fixes

* api.yaml ([76b0825](https://github.com/hei-school/hei-admin-api/commit/76b082595a9abc12ef81b11f3835dbd8e326b621))
* **attendance-migration:** stable version of migration ([c74395c](https://github.com/hei-school/hei-admin-api/commit/c74395c72953ebfc290d46bff1ecbb1de5037ed5))
* calling cd-compute workflow in Sched-Depl-Compute ([4d42a23](https://github.com/hei-school/hei-admin-api/commit/4d42a235cc84822fa76880814e5e2cd43f0aba61))
* CI build app failed ([7f9407d](https://github.com/hei-school/hei-admin-api/commit/7f9407d9317cbe7159a19554e4770f1ba6cb99e9))
* conflict from somme nullable in user ([f75dca5](https://github.com/hei-school/hei-admin-api/commit/f75dca5b890df7a32f2d8b721da3591620c04c26))
* correct errors using tests that didn't work ([269e78c](https://github.com/hei-school/hei-admin-api/commit/269e78c4404348dcead90532f86c3905c741a0a2))
* ExamIT ([0ae7f83](https://github.com/hei-school/hei-admin-api/commit/0ae7f83a67e7e59d25fae557115bc3519454dcef))
* fix all code to follow the new specification ([da287c5](https://github.com/hei-school/hei-admin-api/commit/da287c5f2f29a05b2e45c12211246de57ef17939))
* grade/course/exam test and code ([32ac9a0](https://github.com/hei-school/hei-admin-api/commit/32ac9a013a057092b5f0a943e54eee22946507cd))
* make lamda using nat GW ([20c2f2a](https://github.com/hei-school/hei-admin-api/commit/20c2f2aec1a94fcbbf02a2d86f84e2e820054123))
* migration  ([a467801](https://github.com/hei-school/hei-admin-api/commit/a467801abf7e14d1d840c2896cfb9ce09fe884a0))
* migration order ([f1ef7ca](https://github.com/hei-school/hei-admin-api/commit/f1ef7ca006da4a483da0b2c701dfd364629fbbef))
* migration V_13 ([3db6220](https://github.com/hei-school/hei-admin-api/commit/3db62202268343a45052c542e87cba86edf005c5))
* payment creation datetime  ([49613d0](https://github.com/hei-school/hei-admin-api/commit/49613d0f9998dd1bcec136d70f1f3f7a8d62e87b))
* return empty list if course_session_id is null ([a25e7e7](https://github.com/hei-school/hei-admin-api/commit/a25e7e7d05366d3f283fd1d89ed7f635be7caa81))
* scan only all attendance one hour before course begining in scheduler ([9829e21](https://github.com/hei-school/hei-admin-api/commit/9829e21e9cd87a64651687a2a25ca66a1a21119f))
* **schedule-migration:** stable version of attendance migration ([f3f9d1a](https://github.com/hei-school/hei-admin-api/commit/f3f9d1a3b86d723a975de9c5f1d8915192a021cb))
* scheduler didn't run after building the apk ([be377c7](https://github.com/hei-school/hei-admin-api/commit/be377c7df842432edf85ab2be6ca5fa3258ad221))
* Specification ([a9b2acb](https://github.com/hei-school/hei-admin-api/commit/a9b2acbdc1704c54d850d6a56941933985f38dee))
* **test-variables:** add missing sentry dsn and env because they somehow aren't loaded in context ([9f054bc](https://github.com/hei-school/hei-admin-api/commit/9f054bcf5a3e965bf9ea1ca85de7bf68d32fe480))
* UserManagerDAO ([cd74349](https://github.com/hei-school/hei-admin-api/commit/cd74349e3d5ccb422d7f651ffd35a310fc41da62))


### Features

* add course details ([e2646d5](https://github.com/hei-school/hei-admin-api/commit/e2646d5cd29d7a7b907641fd8fe3092e177db39a))
* add deadletter queue for schedulers ([8878e88](https://github.com/hei-school/hei-admin-api/commit/8878e8819d52f69e8ecff98d07e317f5ca51d355))
* add GET /students/courses/{course_id} ([b634521](https://github.com/hei-school/hei-admin-api/commit/b6345212afe115c7702e5098f3ec5ab8e5e52397))
* add GET and PUT /courses ([9a069b0](https://github.com/hei-school/hei-admin-api/commit/9a069b0df7ead5f40bc080f2ce36014e50cbd1e5))
* add GET and PUT /students/{student_id}/courses ([8c9ad93](https://github.com/hei-school/hei-admin-api/commit/8c9ad93fd4f02c7cff06f46d31e3586c618a4fe9))
* add get by criteria awarded course en point ([b43ed70](https://github.com/hei-school/hei-admin-api/commit/b43ed70f90ce71357f6015404a5aba9978b50711))
* add liste of students in crupdate groups ([5c9cea7](https://github.com/hei-school/hei-admin-api/commit/5c9cea7c4c7d9d61fbfb09a4b36ba5ec998e7a71))
* add todo for review ([40d1731](https://github.com/hei-school/hei-admin-api/commit/40d1731ad46a564741ead81636bca778fe2e977a))
* attendance and course session model ([f9aba5e](https://github.com/hei-school/hei-admin-api/commit/f9aba5e6896a71a7feb60b388bf241879d2e7901))
* attendance controller to post a movement ([82df7c9](https://github.com/hei-school/hei-admin-api/commit/82df7c92a95b82133577690730416ab7200beb8e))
* attendance mapper ([a8cf774](https://github.com/hei-school/hei-admin-api/commit/a8cf774ea3beb51c21438ce248e6abfeb6fca2f5))
* bank transfer type for creating payment ([04d2b75](https://github.com/hei-school/hei-admin-api/commit/04d2b75fc20850e8ba784ab6b62ef3a6ef58f4aa))
* change migration to implement goup and awarded_cource ([2d23b13](https://github.com/hei-school/hei-admin-api/commit/2d23b131d4c57b16615a809c32da0e3b68dbc7b8))
* configure aws schedulers ([8fbd843](https://github.com/hei-school/hei-admin-api/commit/8fbd84347f9db01947bad80fc041d34797b43e6a))
* GET /attendance with some filter ([b71f164](https://github.com/hei-school/hei-admin-api/commit/b71f164c38e3a102f40dd262a6a01a922314f263))
* make fisrt name nullable ([db8bb1b](https://github.com/hei-school/hei-admin-api/commit/db8bb1b553f4918b86d58b58c8e0d1a9202e8924))
* make fisrt name nullable ([88ef663](https://github.com/hei-school/hei-admin-api/commit/88ef663d4aaa43e51d0b69211a5a90439e3f833a))
* make fisrt name nullable ([187c037](https://github.com/hei-school/hei-admin-api/commit/187c037061789deb1a51f906aa6919e3af7fb262))
* make phone, sex, adress, birth_date nullable ([d058497](https://github.com/hei-school/hei-admin-api/commit/d05849771b963ba2955ecb837c5aa78a694fc9d0))
* openapi-generator for Java client ([e10ad78](https://github.com/hei-school/hei-admin-api/commit/e10ad78c0f6af3ce44edd2b562a2bd0103d5fe47))
* upgrade poja ([3a7e6bf](https://github.com/hei-school/hei-admin-api/commit/3a7e6bf86228cadfcf81c3ab6ea0b8c0e160de56))


### Performance Improvements

* add Serializable in model ([da8c7ee](https://github.com/hei-school/hei-admin-api/commit/da8c7ee5a555cf877580bc9ad23c7ccdb85430e4))


### Reverts

* Revert "debug: disable snapstart" ([06fab90](https://github.com/hei-school/hei-admin-api/commit/06fab90a56c12762ddff6f744b164bbd1cc68d48))



## [0.7.1](https://github.com/hei-school/hei-admin-api/compare/v0.7.0...v0.7.1) (2023-03-17)


### Bug Fixes

* calling cd-compute workflow in Sched-Depl-Compute ([5ccba39](https://github.com/hei-school/hei-admin-api/commit/5ccba39cb1daa9f95bb102b7d9eba9c38635b799))
* database security group ([069df1f](https://github.com/hei-school/hei-admin-api/commit/069df1fe35c46d182242a52588f29b6f3ef4876a))
* event-stack syntax ([#97](https://github.com/hei-school/hei-admin-api/issues/97)) ([7b78f83](https://github.com/hei-school/hei-admin-api/commit/7b78f8341c137b9eada7cdb391a8634e13e2af1e))
* security group for database ([47362f4](https://github.com/hei-school/hei-admin-api/commit/47362f4e6dd5df608e40465d9ebcc23439958e13))
* stack name ([#91](https://github.com/hei-school/hei-admin-api/issues/91)) ([6c73dbe](https://github.com/hei-school/hei-admin-api/commit/6c73dbe419292714ae9335d09669c55b4cc011f1))



# [0.7.0](https://github.com/hei-school/hei-admin-api/compare/v0.6.0...v0.7.0) (2022-07-26)


### Features

* filter fees by status without cache ([2c27555](https://github.com/hei-school/hei-admin-api/commit/2c27555f7fe06284f9a70e144a7399f2a5ae2524))



# [0.6.0](https://github.com/hei-school/hei-admin-api/compare/v0.5.1...v0.6.0) (2022-07-26)


### Bug Fixes

* compute remaining amount ([572cc9f](https://github.com/hei-school/hei-admin-api/commit/572cc9f01bedd1689c81edb9a68b43bdcb210c91))
* LATE status is correctly computed ([756e366](https://github.com/hei-school/hei-admin-api/commit/756e3669f911a457c9388311001389a14f695a9b)), closes [#61](https://github.com/hei-school/hei-admin-api/issues/61) [#62](https://github.com/hei-school/hei-admin-api/issues/62)
* multiple fees in payment are not implemented ([fc1deeb](https://github.com/hei-school/hei-admin-api/commit/fc1deeb13f9c772908c6a4d0b7ffd9772ed0de1f))


### Features

* GET fees/{id}/payments and POST fees/{id}/payments ([8a15311](https://github.com/hei-school/hei-admin-api/commit/8a15311af3152061a5bd7c7f332d248afede05d4))
* POST and GET /students/{studentId}/fees ([6a2be51](https://github.com/hei-school/hei-admin-api/commit/6a2be515c76959fe12ced16a122f658d8443782b))



## [0.5.1](https://github.com/hei-school/hei-admin-api/compare/v0.5.0...v0.5.1) (2022-03-03)


### Bug Fixes

* PUT /users validation ([a1b3fab](https://github.com/hei-school/hei-admin-api/commit/a1b3fabf692cf4a75b64a37393e8604fc80c99db)), closes [#50](https://github.com/hei-school/hei-admin-api/issues/50)



# [0.5.0](https://github.com/hei-school/hei-admin-api/compare/v0.4.0...v0.5.0) (2022-02-24)


### Features

* filter users by params ignoring case ([d151927](https://github.com/hei-school/hei-admin-api/commit/d151927dd01bdb31604af85b69c7031aedf2b71f)), closes [#47](https://github.com/hei-school/hei-admin-api/issues/47)



# [0.4.0](https://github.com/hei-school/hei-admin-api/compare/v0.3.0...v0.4.0) (2022-02-08)


### Bug Fixes

* build.gradle works for both unix and windows os ([ddfc835](https://github.com/hei-school/hei-admin-api/commit/ddfc83510b68bec2212ec7feaa5980f239071c8c))
* limit user upsertion to EventBridge max ([9be6693](https://github.com/hei-school/hei-admin-api/commit/9be66939823eecfbdf6c4c134a1c1f5b630415a8)), closes [#37](https://github.com/hei-school/hei-admin-api/issues/37)
* users can be filter by ref and names ([0dd534f](https://github.com/hei-school/hei-admin-api/commit/0dd534f2952d8a835180c0c39aa2e355958a0fe2)), closes [#39](https://github.com/hei-school/hei-admin-api/issues/39)


### Features

* add cors headers for all methods ([799bed0](https://github.com/hei-school/hei-admin-api/commit/799bed0d7fe66e9e8ec1ac28e1ddff335a86db19))
* GET /fees/{id} ([8ed301c](https://github.com/hei-school/hei-admin-api/commit/8ed301ca58cbf2b767940dba0da2a7e676e720f8))



# [0.3.0](https://github.com/hei-school/hei-admin-api/compare/v0.2.0...v0.3.0) (2022-01-14)


### Bug Fixes

* authentication ([662faad](https://github.com/hei-school/hei-admin-api/commit/662faad963bbb267a5f32c31e4c0d5ca8c8ef15c))
* cors ([ecefa89](https://github.com/hei-school/hei-admin-api/commit/ecefa8917f50ae6cdf8b88fb2097d067cd28c50e))
* Poller.WAIT_TIME cannot exceed 20s ([8b214ba](https://github.com/hei-school/hei-admin-api/commit/8b214ba89deda3f74591f332f3a067becd9b0a4b))
* region for eventbridge ([dbf4979](https://github.com/hei-school/hei-admin-api/commit/dbf4979a37f8e7d250869c6d40c09e7a9dff33d8))
* spring.EnableScheduling ([2ae74eb](https://github.com/hei-school/hei-admin-api/commit/2ae74eb7c9f98aa231d0b0ae6930026e8326558e))
* TypeEvent.typeName ([6d92fb7](https://github.com/hei-school/hei-admin-api/commit/6d92fb71e413694a488890819ea3d26849755712))
* use available random port for tests ([de34fb8](https://github.com/hei-school/hei-admin-api/commit/de34fb891b0bace6ff4ce130c8c6e4a1f187c7ce))


### Features

* add bearer to whoami ([9d10108](https://github.com/hei-school/hei-admin-api/commit/9d10108f14038dff2cb8d5d5a371181710adbd65))
* add EventServiceInvoker ([970faad](https://github.com/hei-school/hei-admin-api/commit/970faadecf37a58978cca4c11cfc59bbfd85f92f))
* cors ([849fb4d](https://github.com/hei-school/hei-admin-api/commit/849fb4dccd4175cc92e39ca840d6899a0972d46e))
* create Cognito users ([b5e6d0e](https://github.com/hei-school/hei-admin-api/commit/b5e6d0ec92cd9975d80fd28c15ca83f75e3e9fb2))
* event consumer ([fbfc2eb](https://github.com/hei-school/hei-admin-api/commit/fbfc2ebd7c762ebb3e6e85ad98e8bd1b6a719943))
* generate ts client ([1179a0f](https://github.com/hei-school/hei-admin-api/commit/1179a0f5354de564a5fb7721d6cfc1fb15d7b4d9))
* GET /groups ([e78c744](https://github.com/hei-school/hei-admin-api/commit/e78c744d4d3b4bc76519e0d2eaf4da470ae70fd1))
* GET /managers ([dd0c2d7](https://github.com/hei-school/hei-admin-api/commit/dd0c2d78b12f5640321337869538e18001f79e34))
* GET /students/{id} ([19d8d24](https://github.com/hei-school/hei-admin-api/commit/19d8d24e6557c58124d9c69e2cedff634e1ab56a))
* GET /teachers ([1254945](https://github.com/hei-school/hei-admin-api/commit/12549453d8407d98cf326adbf775afaeeeb22869))
* GET /teachers/{id} ([bb88155](https://github.com/hei-school/hei-admin-api/commit/bb88155b20dc0c6e8af934432b34be09d9efb6b5))
* GET groups/{id} ([87705b8](https://github.com/hei-school/hei-admin-api/commit/87705b87650f8223a6f285b69115f7673fdba1bd))
* getManagerById ([b1caaab](https://github.com/hei-school/hei-admin-api/commit/b1caaab9121d89c044e79bfe258aa179bd6c6f2c))
* log caller info ([11819de](https://github.com/hei-school/hei-admin-api/commit/11819de66dcda4e97084afc359aea5721d874c2a))
* log requests ([f1b2898](https://github.com/hei-school/hei-admin-api/commit/f1b28988df5ab30a976494665948f23ba6823b0d))
* POST /groups ([0b9f2e7](https://github.com/hei-school/hei-admin-api/commit/0b9f2e7bc72332b8feeb7da170c658aea6fc4f86))
* PUT /groups absorbs POST /groups ([3669442](https://github.com/hei-school/hei-admin-api/commit/3669442f4d1ccbb8b99242953d8fffbec713a56a))
* PUT /students ([7382f72](https://github.com/hei-school/hei-admin-api/commit/7382f7236914b31f4bf56f09fde4355d4b99f283))
* PUT /teachers ([87cbec8](https://github.com/hei-school/hei-admin-api/commit/87cbec80f5f3dd961ee0bdad74fa40d5839c20cc))
* send events on PUT users ([8fb8ad5](https://github.com/hei-school/hei-admin-api/commit/8fb8ad58f975fe45a27bac36777f560e42af44c4))
* whoami ([0563e9c](https://github.com/hei-school/hei-admin-api/commit/0563e9c8d1cee528429a35f34365ec7dc4ad84cd))



