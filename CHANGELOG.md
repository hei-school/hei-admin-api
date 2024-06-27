# [1.31.0](https://github.com/hei-school/hei-admin-api/compare/v1.30.0...v1.31.0) (2024-06-27)


### Features

* **not-implemented:** filter fees by payment by mobile  ([d264b7a](https://github.com/hei-school/hei-admin-api/commit/d264b7ad66613aebf24f3ba8f313fe6a2f1b9b26))
* orange and telma mobile payment  ([5fd9743](https://github.com/hei-school/hei-admin-api/commit/5fd974362155f882ef26c815c1658e1eb5002e16))



# [1.30.0](https://github.com/hei-school/hei-admin-api/compare/v1.29.0...v1.30.0) (2024-06-27)


### Features

* **not-implemented:** documentation for fetched mobile transaction  ([d393aa9](https://github.com/hei-school/hei-admin-api/commit/d393aa9aff51f86ee17bbe8907fb49ba2e8becb8))



# [1.29.0](https://github.com/hei-school/hei-admin-api/compare/v1.28.1...v1.29.0) (2024-06-25)


### Bug Fixes

* students works stats size  ([23c95bf](https://github.com/hei-school/hei-admin-api/commit/23c95bf67f5fce5a3c351491f3da658610ec1dfb))


### Features

* **not-implemented:** group statistics documentation  ([44ef424](https://github.com/hei-school/hei-admin-api/commit/44ef4240234845589d41ca3ef39bcb91aed88b56))



## [1.28.1](https://github.com/hei-school/hei-admin-api/compare/v1.28.0...v1.28.1) (2024-06-20)


### Bug Fixes

* change statistics details model  ([4448724](https://github.com/hei-school/hei-admin-api/commit/4448724f27e7088fdc05c32d7930dd8df5346cc4))



# [1.28.0](https://github.com/hei-school/hei-admin-api/compare/v1.27.0...v1.28.0) (2024-06-20)


### Bug Fixes

* cron expressions in unPaidFeesReminder scheduler ([7c5613a](https://github.com/hei-school/hei-admin-api/commit/7c5613a8a8990dbb513fff493366a7befdf2cf70))


### Features

* add enabled and suspended in statistics model  ([b41fe68](https://github.com/hei-school/hei-admin-api/commit/b41fe68c762d371c29a42adf795a36ba6d129596))
* send an email reminder for unpaid fees ([d0ba735](https://github.com/hei-school/hei-admin-api/commit/d0ba735007750bc4d25e082706f03739537fe028))



# [1.27.0](https://github.com/hei-school/hei-admin-api/compare/v1.26.1...v1.27.0) (2024-06-19)


### Features

* implement a specific endpoint for students statistics ([ee2ebd4](https://github.com/hei-school/hei-admin-api/commit/ee2ebd4cc85ed79ce906840c73cde8be593a1298))



## [1.26.1](https://github.com/hei-school/hei-admin-api/compare/v1.26.0...v1.26.1) (2024-06-18)


### Bug Fixes

* migration error when updating commitment begin default value  ([57382f8](https://github.com/hei-school/hei-admin-api/commit/57382f8589f4a7b7d5a30a5edf0da8f27da70689))
* students stat are now exact ([179dbb6](https://github.com/hei-school/hei-admin-api/commit/179dbb6c9b5f60f27f8c6b6023cd9a09e5d073ef))



# [1.26.0](https://github.com/hei-school/hei-admin-api/compare/v1.25.0...v1.26.0) (2024-06-14)


### Features

* compute the work status during fetching students data  ([9f998e0](https://github.com/hei-school/hei-admin-api/commit/9f998e040d670bc843bfe7aaf4edd99f82bf6fbb))



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



