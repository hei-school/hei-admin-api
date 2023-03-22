Community contributors are welcome to:
* Improve our code quality.
  [Our Sonarcloud analysis](https://sonarcloud.io/dashboard?id=hei-admin-api) is publicly available for this matter.
* Report any [issue](https://github.com/hei-school/hei-admin-api/issues) you encountered
  when using our [released Docker images](https://gallery.ecr.aws/q6i6y5o4/hei-admin-api).
  We especially welcome security reports.

In case you open a PR, please meet the following conditions, most of which are automatically checked by our CI:
* Tests are passing
* Coverage on new code is 80% or more.
  Please note that this is not line coverage, but [Sonar coverage](https://docs.sonarqube.org/latest/user-guide/metric-definitions/).
  Sonar coverage is more strict as it takes into account condition coverage (how much branching conditions were tested).
* No major bug|vulnerability|security hostpot detected by Sonar
* Format with respect to [checkstyle](https://github.com/hei-school/hei-admin-api/wiki/Checkstyle)
* Use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/)
  and [sign](https://docs.github.com/en/github/authenticating-to-github/managing-commit-signature-verification/about-commit-signature-verification) them
