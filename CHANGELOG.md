# [1.25.0](https://github.com/hei-school/hei-admin-api/compare/v1.24.0...v1.25.0) (2024-06-13)


### Bug Fixes

* max ses send email recipient size is 50, hence group list by 50 if needed ([fa6d180](https://github.com/hei-school/hei-admin-api/commit/fa6d180ec3f33de1b6ac5a321b62c11db55c41b8))
* redirect to HA prod ui ([e40a221](https://github.com/hei-school/hei-admin-api/commit/e40a2213cea0e675fd3c94012f11f0d1c810669c))
* wrong variable in email template ([02cf17f](https://github.com/hei-school/hei-admin-api/commit/02cf17f25f30986098321e7c002a1431d8fc019f))


### Features

* suspend user with late fees and mail them ([9c23ec7](https://github.com/hei-school/hei-admin-api/commit/9c23ec7772c7ebdfad5c4f2a90d7ab6de021ef0e))



# [1.24.0](https://github.com/hei-school/hei-admin-api/compare/v1.23.0...v1.24.0) (2024-06-05)


### Bug Fixes

* add title attribute in event entity ([8dd6c86](https://github.com/hei-school/hei-admin-api/commit/8dd6c865800acfa12a72996ac47ffb200388cb69))
* group null in announcement event model ([14ac1d0](https://github.com/hei-school/hei-admin-api/commit/14ac1d0e1e1d2656c6dd052108de9805a2a80834))
* **hot-fix:**  correctly save fees by only updating status, avoid payment accident removal ([9a6c04b](https://github.com/hei-school/hei-admin-api/commit/9a6c04bd7ad0621a35183c75d3525f71f940cbb2))
* migrations weren't numbered correctly thus failing deploy. ([a1457e8](https://github.com/hei-school/hei-admin-api/commit/a1457e89662ea0350c07d85f4ff1b904551aa111))
* student commitment is not saved when uploading work file  ([3fd5b85](https://github.com/hei-school/hei-admin-api/commit/3fd5b85a7245ad0022c89530a0132f0e351bc0cb))


### Features

* commitment date on student payload  ([4d3783f](https://github.com/hei-school/hei-admin-api/commit/4d3783f3a2bbadc5f6f637d7e13264362d888d3b))


### Reverts

* remove logs ([e6dc4af](https://github.com/hei-school/hei-admin-api/commit/e6dc4af21385afd4292b831a3c6cd6c321e2beb9))



# [1.23.0](https://github.com/hei-school/hei-admin-api/compare/v1.22.0...v1.23.0) (2024-04-30)


### Features

* add pagination when fetching students group  ([1107816](https://github.com/hei-school/hei-admin-api/commit/1107816ce4d196a70982cc6c579f9c1b3a119e8d))



# [1.22.0](https://github.com/hei-school/hei-admin-api/compare/v1.21.0...v1.22.0) (2024-04-30)


### Features

* get announcement by id ([01ea24b](https://github.com/hei-school/hei-admin-api/commit/01ea24bffafbc6019fa5ef5b98dda9a43f34e3b3))
* implement promotion resources ([de6a706](https://github.com/hei-school/hei-admin-api/commit/de6a706098b70f1031ca82e8c66bc5747e2db43f))
* implements announcements resources ([46ddaba](https://github.com/hei-school/hei-admin-api/commit/46ddaba53ae599096c2c6601b696bb37de1705d2))



# [1.21.0](https://github.com/hei-school/hei-admin-api/compare/v1.20.0...v1.21.0) (2024-04-17)


### Features

* get mpbs verifications ([a097964](https://github.com/hei-school/hei-admin-api/commit/a0979649e38667e31b0f27eb0cb6fc13658d3e19))
* mobile payment by student reources  ([a219a8b](https://github.com/hei-school/hei-admin-api/commit/a219a8bc8f52665f90f470fdb74a88cb57284a2d))



# [1.20.0](https://github.com/hei-school/hei-admin-api/compare/v1.19.0...v1.20.0) (2024-04-16)


### Features

* **not-implemented:** mobile payment by student  ([e0d0a41](https://github.com/hei-school/hei-admin-api/commit/e0d0a41257af7da7bb716d5dae7c4b9875a40757))



# [1.19.0](https://github.com/hei-school/hei-admin-api/compare/v1.18.0...v1.19.0) (2024-04-12)


### Features

* insert multiple group flows for a student  ([6c166bb](https://github.com/hei-school/hei-admin-api/commit/6c166bb7d8512af81f286e99435295f3e9270636))



# [1.18.0](https://github.com/hei-school/hei-admin-api/compare/v1.17.0...v1.18.0) (2024-04-12)


### Features

* filter students by commitment begin date  ([cf7ebb9](https://github.com/hei-school/hei-admin-api/commit/cf7ebb947bc0e33628bdefebda01ff744d05a012))



# [1.17.0](https://github.com/hei-school/hei-admin-api/compare/v1.16.0...v1.17.0) (2024-04-09)


### Bug Fixes

* fees remove payment after updating status  ([142c7c5](https://github.com/hei-school/hei-admin-api/commit/142c7c5f76bcd0716c4857e4e919a257cc27a4e0))
* reset remaining amount after deleting payment  ([487c65c](https://github.com/hei-school/hei-admin-api/commit/487c65c6a22f195fd09de564b7b0799f74b2d342))


### Features

* **not-implemented:** promotion endpoint ([034997d](https://github.com/hei-school/hei-admin-api/commit/034997d82801502e490a8b31f76e80eaef1fa849))



# [1.16.0](https://github.com/hei-school/hei-admin-api/compare/v1.15.0...v1.16.0) (2024-04-03)


### Features

* student has work documents  ([805403a](https://github.com/hei-school/hei-admin-api/commit/805403af70f80da7a7f72de62da7e05616ce5bb1))



