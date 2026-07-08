This project both specifies the
[HEI Admin API](https://petstore.swagger.io/?url=https://raw.githubusercontent.com/hei-school/hei-admin-api/dev/doc/api.yml)
and implements it in Java.

[Releases](https://github.com/hei-school/hei-admin-api/releases) are published [here](https://gallery.ecr.aws/q6i6y5o4/hei-admin-api) as Docker images. Feel free to use them.

We welcome [contributions](https://github.com/hei-school/hei-admin-api/blob/dev/CONTRIBUTING.md).

## FEES_ONLY mode

Set the environment variable `FEES_ONLY=true` to restrict the API to a read-only subset of endpoints.
In this mode, only requests targeting the following URI prefixes are allowed:

| Prefix | Description |
|--------|-------------|
| `/fees` | Fee management |
| `/students` | Student management |
| `/whoami` | Current user identity |
| `/ping` | Liveness check |
| `/authentication` | Authentication |
| `/health` | Health checks |
| `/mpbs` | Mobile Payment by SMS |

All other endpoints return **HTTP 403 Forbidden** with the message
`"This endpoint is disabled in FEES_ONLY mode"`.

When `FEES_ONLY` is unset or `false`, the API behaves normally.
