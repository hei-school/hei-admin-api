# [1.34.0](https://github.com/hei-school/hei-admin-api/compare/v1.33.0...v1.34.0) (2024-07-11)


### Bug Fixes

* comment and fix the algorithm process to compute mpbs status  ([e2e4e5d](https://github.com/hei-school/hei-admin-api/commit/e2e4e5de486c9eb31106d8e4a58b9ab30f55315e))
* computing mpbs status fail if the transaction details is null …  ([16497f7](https://github.com/hei-school/hei-admin-api/commit/16497f7ce57041879d7797858d2615d23a820a9a))


### Features

* filter group by ref and student ref ([3ff5b82](https://github.com/hei-school/hei-admin-api/commit/3ff5b82cfb13ba14f435412d9ac7b13cbdb90b3d))
* payment by mobile money  ([09732dc](https://github.com/hei-school/hei-admin-api/commit/09732dc5333665160f3a7b3ea07705541acb65f5))



# [1.33.0](https://github.com/hei-school/hei-admin-api/compare/v1.32.0...v1.33.0) (2024-07-10)


### Bug Fixes

* handle day validity checker for mpbs status  ([0e5079e](https://github.com/hei-school/hei-admin-api/commit/0e5079edf792c9e3b8e6529e41a529e8acd18947))


### Features

* endpoint who will execute the sceduler task  ([30807fb](https://github.com/hei-school/hei-admin-api/commit/30807fbd3249a6eddd25350d524ac4a985aeb095))
* group attribute in user rest model ([764bad9](https://github.com/hei-school/hei-admin-api/commit/764bad9c149941c62ae36e492b42c37f620b3f65))
* update user status accordingly after paying fee ([ddff281](https://github.com/hei-school/hei-admin-api/commit/ddff28172aa224c3e47c81b9b44cad70fe6f2577))



# [1.32.0](https://github.com/hei-school/hei-admin-api/compare/v1.28.1...v1.32.0) (2024-07-03)


### Bug Fixes

* add @AllArgsConstructor to endpoint.event.gen classes otherwise they cannot be deserialized from empty bean ([19a87a5](https://github.com/hei-school/hei-admin-api/commit/19a87a502c008881011230b18eeae3934d92ffb2))
* check mobile transaction scheduler stack  ([3987703](https://github.com/hei-school/hei-admin-api/commit/3987703c04d534d6b8922845c4cff6fce380e204))
* mpbs status default values  ([becab74](https://github.com/hei-school/hei-admin-api/commit/becab748c5037bee78bd91d353cbdc21b08dd5da))
* scheduled events are processed by event stack 1 ([266bf79](https://github.com/hei-school/hei-admin-api/commit/266bf79be7837a040fab52b731603dda2258bffb))
* students works stats size  ([23c95bf](https://github.com/hei-school/hei-admin-api/commit/23c95bf67f5fce5a3c351491f3da658610ec1dfb))
* update event classes imports ([a70b160](https://github.com/hei-school/hei-admin-api/commit/a70b16000ed1979d7c2f064e6336372e333d0c83))
* worker function timeout must be less than sqs visibility timeout ([9dbf48b](https://github.com/hei-school/hei-admin-api/commit/9dbf48b47affc1e4c9c262ef9c08b0dfc5c816ba))


### Features

* add size to group model ([c7abded](https://github.com/hei-school/hei-admin-api/commit/c7abded36e63e339a82fc9da0c1fb52d612f10a9))
* add status to MPBS to know if PENDING, SUCCESS or FAILED  ([af23531](https://github.com/hei-school/hei-admin-api/commit/af23531404a4b8434ecafe6a5bfeba03ae01dc01))
* **not-implemented:** documentation for fetched mobile transaction  ([7ea15ee](https://github.com/hei-school/hei-admin-api/commit/7ea15eec67fa355a9e2a4b3bcc96b2ef3265f877))
* **not-implemented:** filter fees by payment by mobile  ([a5ba67a](https://github.com/hei-school/hei-admin-api/commit/a5ba67ac90ce340ca143c5e0aee05e609dc5ef4a))
* **not-implemented:** group statistics documentation  ([5527f65](https://github.com/hei-school/hei-admin-api/commit/5527f652056ef534bddbc6cde708538533c07a3b))
* orange and telma mobile payment  ([2a6f0bb](https://github.com/hei-school/hei-admin-api/commit/2a6f0bb57b12ed60264b3477240a6dfcd713153c))



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



