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



