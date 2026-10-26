# Publishing

A release is pushing a `v<version>` tag, e.g. `git tag v1.2.0 && git push origin v1.2.0`.
The GitHub action `.github/workflows/gradle-publish.yaml` builds, signs and publishes to the
Central Portal OSSRH staging API, then uploads the staging repository to the Central Portal.

Then open `https://central.sonatype.com/publishing/deployments`: when the deployment shows
"Validated", press Publish. It is on Maven Central a few minutes later.

The upload step's `curl` has no `--fail`: if the deployment doesn't appear, read that step's log.

## Secrets

The local values are in `~/.gradle/gradle.properties`:

```
cat ~/.gradle/gradle.properties
```

The action reads four GitHub secrets. They are per repo, so a new library needs all four set again:

| GitHub secret    | Value                                                      |
|------------------|------------------------------------------------------------|
| `OSSRH_USERNAME` | `ossrhUsername` (Central Portal token username)            |
| `OSSRH_PASSWORD` | `ossrhPassword` (Central Portal token password)            |
| `PGP_PASSPHRASE` | `signing.password`                                         |
| `PGP_KEY`        | the signing key, exported with `gpg` using `signing.keyId` |

From the repo root:

```
gpg --export-secret-keys --armor <signing.keyId> > local.asc
gh secret set PGP_KEY < local.asc
rm local.asc                      # unencrypted private key
gh secret set PGP_PASSPHRASE      # paste signing.password
gh secret set OSSRH_USERNAME      # paste ossrhUsername
gh secret set OSSRH_PASSWORD      # paste ossrhPassword
```

### Refreshing the token

`ossrhUsername` / `ossrhPassword` are a Central Portal user token, not the login. To get a new one:
`https://central.sonatype.com` → your account (top right) → View Account → Generate User Token.

username: corto
password: stored in ~/.gradle/gradle.properties

Put both values in `~/.gradle/gradle.properties`, then set `OSSRH_USERNAME` and `OSSRH_PASSWORD`
again here and in every other library that publishes (gcp-commons).

## Publishing by hand

`./gradlew clean build sign publish` uses the same values from `~/.gradle/gradle.properties`. It
only uploads to the staging API; nothing shows in the portal until the upload is handed over. The
portal has no button for that: run, from the same machine,

```
U=$(grep '^ossrhUsername' ~/.gradle/gradle.properties | cut -d= -f2- | tr -d ' ')
P=$(grep '^ossrhPassword' ~/.gradle/gradle.properties | cut -d= -f2- | tr -d ' ')
curl -sS -X POST \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.myseotoolbox" \
  -H "Authorization: Bearer $(printf '%s:%s' "$U" "$P" | base64 | tr -d '\n')"
```

then press Publish on the deployment as above.
