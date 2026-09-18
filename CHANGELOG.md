## [1.173.1](https://github.com/hei-school/hei-admin-api/compare/v1.173.0...v1.173.1) (2026-09-18)


### Bug Fixes

* duplicate payment ([49c54a9](https://github.com/hei-school/hei-admin-api/commit/49c54a948baf24eaa187c6008ab3edd516dda470))
* transfert create over_payment transaction ([fd0a5e4](https://github.com/hei-school/hei-admin-api/commit/fd0a5e4ee8c45ca541e9b6812f9248abb87551cd))



# [1.173.0](https://github.com/hei-school/hei-admin-api/compare/v1.172.0...v1.173.0) (2026-09-17)


### Bug Fixes

* **casdoor:** handle client auth errors correctly ([42fb277](https://github.com/hei-school/hei-admin-api/commit/42fb277f6d83ac76198a4b9dac40be8f81ab8cbc))
* **ci:** raise the gradle daemon heap so sonar can finish ([8795c64](https://github.com/hei-school/hei-admin-api/commit/8795c64820a28e228a8a77601bcaf3438ae2ba32))
* create credit transaction for payment created by admin ([bb4b672](https://github.com/hei-school/hei-admin-api/commit/bb4b672f37cabf5ec953de8e27b0f9805122832f))
* **documenso:** acknowledge webhooks of unknown documents ([5df8ac0](https://github.com/hei-school/hei-admin-api/commit/5df8ac0b72983266d6f4006e1f1c07bbc02df683))
* **documenso:** download the signed pdf as bytes, not as a File ([a126b41](https://github.com/hei-school/hei-admin-api/commit/a126b41abb7849e4e8a6d6ea6a52e52be4e05732))
* format number ([3562cea](https://github.com/hei-school/hei-admin-api/commit/3562ceafe76c37e152e7b9bf397e9b85916bbe90))
* get student group flows at level ([a983fa8](https://github.com/hei-school/hei-admin-api/commit/a983fa8947cbd41737ecdd9dc9ad00dff751dfc4))
* incorrect return result (instead of an unique object, it returned a list) ([384d191](https://github.com/hei-school/hei-admin-api/commit/384d1911f5149b5fddb728b97dc62e4e9c8c9ddf))
* **promotion:** report a not-yet-started promotion as such instead of an error ([c804dc0](https://github.com/hei-school/hei-admin-api/commit/c804dc04191825172b4b3264dd13a641b7bdaf5f))
* trigger patch release ([1c73935](https://github.com/hei-school/hei-admin-api/commit/1c73935249b4571b00bbbd5bdc52d09e78de564f))


### Features

* add reason before rejecting credit payment ([5020ee4](https://github.com/hei-school/hei-admin-api/commit/5020ee498f9ea78943ed2c69c3729cff107a164d))
* add security rule for /fees/advanced-stats ([788e627](https://github.com/hei-school/hei-admin-api/commit/788e627ea5974d8f4c9142daa04c93678a574eb8))
* advanced fees stats update ([4e50783](https://github.com/hei-school/hei-admin-api/commit/4e507838c87044c2fc25b7c2cf5db19d6f413602))
* **documenso:** tell the signature date from the archiving date ([19c7e7f](https://github.com/hei-school/hei-admin-api/commit/19c7e7f9699f6d7c7eec705f3eaa599cec5d0963))



# [1.172.0](https://github.com/hei-school/hei-admin-api/compare/v1.171.0...v1.172.0) (2026-09-09)


### Bug Fixes

* teacherController and CreditService ([a8caf57](https://github.com/hei-school/hei-admin-api/commit/a8caf57427f6638b0bcdfe76a7d39bccd2473d2c))


### Features

* add new attributes for student insurance ([a1d3b87](https://github.com/hei-school/hei-admin-api/commit/a1d3b87e638d258ffaf9ff04a21f2fe9bc4207bf))
* **documenso:** generate fiches only for the students paying monthly ([0692741](https://github.com/hei-school/hei-admin-api/commit/0692741ecd55393d33da0dc7f2f2b9e697ea60f7))
* **documenso:** only offer the promotions a template's level targets ([4bae50e](https://github.com/hei-school/hei-admin-api/commit/4bae50e72f0a01e922cefb7011481d5d10a86aee))
* get student group flows and update advanced fees stats ([a9ffb22](https://github.com/hei-school/hei-admin-api/commit/a9ffb2232ee6a96448dbcda6ff6dcb996e654460))
* get student group flows and update group flow ([6ef8f56](https://github.com/hei-school/hei-admin-api/commit/6ef8f567087a73b611528c23e72e88601c69d0e2))



# [1.171.0](https://github.com/hei-school/hei-admin-api/compare/v1.170.0...v1.171.0) (2026-09-02)


### Bug Fixes

* **documenso:** drop the templates Documenso no longer has when syncing ([3ad6707](https://github.com/hei-school/hei-admin-api/commit/3ad6707745829737b12648329f318eb3bd5c0987))
* get student group flows ([57d3029](https://github.com/hei-school/hei-admin-api/commit/57d30295ffd2cae4f030ce5902fe3cb7b4f945f1))
* get student group flows at level ([a3048a4](https://github.com/hei-school/hei-admin-api/commit/a3048a4e52251d28d67abe55079873acda04862c))
* student yearly result ([49e046a](https://github.com/hei-school/hei-admin-api/commit/49e046afefbc8032a733405297da53e58ffc4519))
* test documensoIT ([ea29913](https://github.com/hei-school/hei-admin-api/commit/ea29913dce1590d95b27b7b446b23ebbc9716971))


### Features

* add student insurance fee count ([882ac53](https://github.com/hei-school/hei-admin-api/commit/882ac53c829e4c6578d06bdebe034faf7205ac29))



# [1.170.0](https://github.com/hei-school/hei-admin-api/compare/v1.169.1...v1.170.0) (2026-08-26)


### Bug Fixes

* create credit transaction ([f78c6f9](https://github.com/hei-school/hei-admin-api/commit/f78c6f9bb4e0fac40cc046bef55a74cf30604fd1))
* trigger CD ([132d622](https://github.com/hei-school/hei-admin-api/commit/132d622f5d98b976cce2885a5d3fd177a23d5282))
* trigger CD ([328341f](https://github.com/hei-school/hei-admin-api/commit/328341f8e7f237b32ac803a5b9b4a339252e8b9c))


### Features

* add Documenso document distribution ([b952b82](https://github.com/hei-school/hei-admin-api/commit/b952b8244da97d9a1a28cede7b31f6562988b910))
* expose credit transaction creation datetime ([491e827](https://github.com/hei-school/hei-admin-api/commit/491e827dcbec988da306ad7918f8e1beeb810eb0))



## [1.169.1](https://github.com/hei-school/hei-admin-api/compare/v1.169.0...v1.169.1) (2026-08-25)


### Bug Fixes

* **test:** scope global search test to its own marker ([4621402](https://github.com/hei-school/hei-admin-api/commit/4621402d3365482981b160702cc4550ecf5cb38a))



# [1.169.0](https://github.com/hei-school/hei-admin-api/compare/v1.166.1...v1.169.0) (2026-08-21)


### Features

* documenso integration ([e5ad697](https://github.com/hei-school/hei-admin-api/commit/e5ad6972f73514bd163b9858fdb614a403edfffb))



## [1.166.1](https://github.com/hei-school/hei-admin-api/compare/v1.166.0...v1.166.1) (2026-08-12)


### Bug Fixes

* pin axios version to 1.18.1 ([54199a6](https://github.com/hei-school/hei-admin-api/commit/54199a63f8b236a5b5d2bb41ba644f2deb9bf99d))



# [1.166.0](https://github.com/hei-school/hei-admin-api/compare/v1.165.1...v1.166.0) (2026-08-11)


### Bug Fixes

* get student credit transactions ([a5f583e](https://github.com/hei-school/hei-admin-api/commit/a5f583ed88a402b1367a560ca843421dc19cd941))


### Features

* implement fees archiving and credit movement tracking ([f6f2ffd](https://github.com/hei-school/hei-admin-api/commit/f6f2ffd8c83833fdfdf0c704ee0d8c6635ddc291))
* reject credit payment ([8497a7f](https://github.com/hei-school/hei-admin-api/commit/8497a7f713b82d0d514711c6b68ce8d35c0fb4e1))



## [1.165.1](https://github.com/hei-school/hei-admin-api/compare/v1.165.0...v1.165.1) (2026-08-06)


### Bug Fixes

* pin axios version to 1.18.1 ([5e271d3](https://github.com/hei-school/hei-admin-api/commit/5e271d3aaf043f4838c6896e2bd557d8431f5717))



