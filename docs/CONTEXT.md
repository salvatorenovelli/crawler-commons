# crawler-commons — CONTEXT.md

## Model

`com.myseotoolbox:crawler-commons` is a plain Java library of static functions, shared by the
services. It is **the one implementation of URL parsing and RFC 3986 case and scheme-based normalisation**:
every service that takes a URL in calls it instead of keeping its own copy, so no two services can
normalise a URL differently.

Everything is in the package `com.myseotoolbox.crawler.utils`:

- `UriUtils` — `toValidUri` (validates, trims, removes the fragment, decodes and re-escapes, keeping
  the brackets of an IPv6 host unescaped, then normalises), `normalise` (for an http or https URL
  with an authority, the URL as OkHttp's `HttpUrl` parses it: the scheme and the host are
  lower-cased, a non-ASCII host becomes punycode, an empty path becomes `/`, the scheme's default
  port and an empty port are removed, `.` and `..` path segments are removed; user info, path,
  query and fragment keep their case; a URL `HttpUrl.parse` refuses is returned unchanged),
  `toAbsoluteUri`, `isValidUrl`, `getFolder`, `extractHost`, `isValidHost`, `filterValidUrls`,
  `isEmptyLink`, `isAbsolute`.
- `UriCreator.create` — `toValidUri`, throwing `IllegalArgumentException` when the URL is invalid.
- `LinkResolver.resolve` — resolves a link against the page it was found on the way a browser does
  (a `?query` or `#fragment` link keeps the base path), and returns the normalised URI.
- `WebsiteOriginUtils` — origin and host comparisons; `extractOrigin` returns the normalised origin
  (`https://x:443/a` → `https://x/`).
- `PathMatcher` — path-scope matching used by `WebsiteOriginUtils.isChildOf`.
- `RemoveUrlFragment`, `UrlDecoder`, `SafeStringEscaper` — the string steps `toValidUri` is built from.

The functions that return a URL return it normalised, so a caller passes a plain `URI` or string and
never calls `normalise` itself.

## OkHttp is a dependency for `HttpUrl` only

OkHttp's `HttpUrl` parses a URL the way a browser does (the WHATWG URL standard), which is what the
crawler has to request and what Google reports. Writing that by hand meant a regex per rule and
still missed punycode and dot segments, so `normalise` delegates to `HttpUrl` instead. Nothing here
uses OkHttp as an HTTP client. `toValidUri` still decodes and re-escapes on its own: `HttpUrl`
refuses a host whose IPv6 brackets were escaped to `%5B`/`%5D`, which is why `toValidUri` keeps them
unescaped.

## Consumers: local source against the published version

A consumer's `settings.gradle` has

```groovy
if (file('../crawler-commons').exists()) includeBuild '../crawler-commons'
```

With this folder checked out next to the service, Gradle substitutes the dependency
`com.myseotoolbox:crawler-commons:<version>` with this source, so an edit here is compiled into the
service's build and test run with no publish and no version bump. Without the folder (CI, a fresh
checkout) the service uses the published version it names. `./gradlew dependencies` in the service
shows `com.myseotoolbox:crawler-commons:1.2.0 -> project :crawler-commons` when the local source is
in use.

A release is pushing a `v<version>` tag (for example `v1.2.0`): the GitHub action
`.github/workflows/gradle-publish.yaml` builds, publishes to the OSSRH staging API and uploads the
staging repository to the Central Portal, which releases it to Maven Central. The publishing notes are
in `README_PUBLISH.md`. After a change here, publish a new version and bump the version string in
each consumer, or CI builds against the old one.

## Gotchas

- **The consumer's Gradle builds this project.** Under `includeBuild`, this `build.gradle` is
  evaluated by the consumer's Gradle, not by this repo's wrapper. Both are Gradle 9.2.1 today; when
  either is upgraded, the build script has to keep working on both.
- **The dependency is `okhttp-jvm`, not `okhttp`.** OkHttp 5 is a Kotlin multiplatform library:
  its `okhttp` artifact is an empty jar that only Gradle module metadata redirects to
  `okhttp-jvm`, so a Maven consumer reading this library's POM would get no `HttpUrl` class.
  Depending on `okhttp-jvm` also keeps Spring Boot consumers on this version: their dependency
  management pins `okhttp` (to 4.12.0 in crawler-service) but not `okhttp-jvm`.
- **Tests run on the JUnit Platform with both engines**: Jupiter for JUnit 5 tests, Vintage for the
  older JUnit 4 ones (`WebsiteOriginUtilsTest`, `PathMatcherTest`). A test class whose engine is
  missing from the test runtime is not run, and nothing reports it.
