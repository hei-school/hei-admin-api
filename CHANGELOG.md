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



# [0.2.0](https://github.com/hei-school/hei-admin-api/compare/v0.1.0...v0.2.0) (2021-09-10)


### Features

* authenticated GET /students/{id} and /teachers/{id} ([665cef5](https://github.com/hei-school/hei-admin-api/commit/665cef5ede25c3253cf727c5c9dcb56fdec1a66c))



# [0.1.0](https://github.com/hei-school/hei-admin-api/compare/91930769a08d93b29b795201c438f464e7697018...v0.1.0) (2021-08-11)


### Features

* ping-pong endpoint ([9193076](https://github.com/hei-school/hei-admin-api/commit/91930769a08d93b29b795201c438f464e7697018))



