# NEXUS Security Audit

> **Date:** 2026-10-06
> **Scope:** credential handling in backups/sync, tracker OAuth secrets, extension-store
> trust, CI secret exposure, and release hygiene in the NEXUS fork.
> **Method:** static review of the full diff surface, CI/workflow inspection, and
> on-device verification of the credential migration on an Android 14 x86_64 emulator.

Credential **key names** appear throughout this document. Credential **values never do** —
they are not recorded here, in the commit history of this change, or in any log.

---

## 1. Executive summary

The audit found nine issues. Seven are fixed and merged, one is fixed in this change, and
two are accepted risks that cannot be fixed retroactively.

The most significant finding was that **credentials were exported in plaintext** by both
the backup and sync subsystems. Any user who created a backup, or synced with private
preferences enabled, wrote their WebDAV password, sync API key, connections password and
Discord account tokens into a shareable file.

| # | Finding | Severity | Status |
|---|---------|----------|--------|
| 1 | Credentials exported in plaintext by backups | High | **Fixed** `0c22ece` |
| 2 | Credentials exported in plaintext by sync | High | **Fixed** `3b9b85a` |
| 3 | Kavita `APIKEY` extension credential exported | High | **Fixed** `0c22ece` |
| 4 | Tracker OAuth client secrets committed to source | High | **Fixed** `f817b6e` |
| 5 | `.gitignore` did not cover keystores/keys/credentials | Medium | **Fixed** `5a6ba52` |
| 6 | Extension stores added with no trust confirmation | Medium | **Fixed** `2148e55` |
| 7 | Preview CI builds shipped telemetry | Low | **Fixed** `606fbb0` |
| 8 | Dependabot PRs could not build (secret steps) | Low | **Fixed** `1a4efe4` |
| 9 | Tracker OAuth client **ids** hardcoded in Kotlin | Low | **Fixed** *this change* |
| 10 | Stale `update_website.yml` deploy workflow on 2 branches | Low | **Fixed** `722a02d`, `4552261` |
| 11 | Historical secrets remain in git history and in every APK | Medium | **Accepted** §6.1 |
| 12 | Pre-existing backups remain unsanitized | Medium | **Accepted** §6.2 |

---

## 2. Credential storage model (after the fix)

Credentials historically lived under plain `SharedPreferences` keys, which made them part
of the default backup and sync payload.

They now live under **`Preference.privateKey`**, which prefixes the key with `__PRIVATE_`.
The backing helper is `core/common/.../PrivatePreferenceFallback.kt`.

| Key (before) | Key (after) |
|---|---|
| `connection_webdav_password` | `__PRIVATE_connection_webdav_password` |
| `connection_sync_client_api_key` | `__PRIVATE_connection_sync_client_api_key` |
| `discord_accounts` | `__PRIVATE_discord_accounts` |
| `pref_connections_password_<service>` | `__PRIVATE_pref_connections_password_<service>` |

**Read semantics:** `privateStringWithLegacyFallback` reads the private key first, then
falls back to the legacy plain key so a not-yet-migrated install still authenticates.

**Write semantics:** writes target the private key **and delete the legacy key**, so a
single successful write migrates that credential permanently.

### Migration

`PrivateSyncCredentialMigration` is registered in `Migrations.kt` with `Migration.ALWAYS`
(`version = -1f`), so it is eligible on every migration pass. It is idempotent: it only
moves a value that still sits under its legacy key.

**Important:** `Migration.ALWAYS` does *not* mean "runs on every app start". The strategy
is chosen by `MigrationStrategyFactory`:

| Condition | Strategy | Migrations run |
|---|---|---|
| `old == 0` (fresh install) | `InitialMigrationStrategy` | `isAlways` only |
| `old >= new` | `NoopMigrationStrategy` | **none** |
| `old < new` (upgrade) | `VersionRangeMigrationStrategy` | `isAlways \|\| version in (old+1..new)` |

`old` is read from `__APP_STATE_eh_last_version_code`; `new` is `BuildConfig.VERSION_CODE`.
This is why the versionCode bump was mandatory: without `versionCode` changing, an
upgrading user would hit the `Noop` branch and the migration would never run.

`PreferenceRestorer` additionally re-invokes
`PrivateSyncCredentialMigration.moveLegacyCredentialsToPrivateKeys` after a restore,
because a restore can write a legacy key back *after* the migration already ran.

---

## 3. On-device verification

Run against an Android 14 (API 34) x86_64 emulator, debug build `com.nexus.app.dev`,
`versionCode=10`, `versionName=1.5.2-47`.

Method: seed plaintext credentials into `shared_prefs`, then force-stop and relaunch so the
migration path executes, and assert on the resulting preference XML.

| Scenario | Strategy exercised | Result |
|---|---|---|
| Upgrade, stored `old=8` → `new=10` | `VersionRangeMigrationStrategy` | **PASS** — all 4 legacy keys removed, all 4 `__PRIVATE_` counterparts present with byte-identical values, version state advanced to 10 |
| Second restart, no state change | `NoopMigrationStrategy` | **PASS** — no change, no corruption |
| Fresh install, `old=0` | `InitialMigrationStrategy` | **PASS** — no legacy keys created, state initialised to 10 |

**Assertion detail** (4 checks × 3 states, 0 failures):

- each of the 4 legacy keys **absent**
- each of the 4 private keys **present**
- each private value an **exact match** of the seeded value (proves lossless migration)
- `__APP_STATE_eh_last_version_code == 10`

### Backup and sync exclusion

Findings 1–3 are now covered by `PreferenceBackupCreatorTest`, which injects a fake
`PreferenceStore` and asserts that with `includePrivatePreferences = false`:

- all four `__PRIVATE_*` credentials are absent
- the `APIKEY` extension credential is absent
- ordinary settings are still exported, with their values intact

and that with `includePrivatePreferences = true` the credentials *are* exported (the
opt-in still works), while `__APP_STATE_*` keys are never exported on either path.

**The test was mutation-checked:** disabling the filter in
`PreferenceBackupCreator.withPrivatePreferences` makes
`excludesCredentialsWhenPrivateSettingsAreNotRequested` fail at the credential assertion,
while the other two tests correctly still pass. So it is not passing trivially.

**Still not verified at runtime:** no backup was executed on-device. Doing so requires
automating both the app's settings flow and the Android SAF file picker. The unit test
runs in CI on every change instead, which is the stronger guarantee.

**Tracker OAuth logins** were not exercised; they require third-party accounts and are
blocked on the rotation in §5.

---

## 4. Build and CI secret handling

Tracker OAuth client secrets were moved out of source control in `f817b6e`:

- `app/build.gradle.kts` loads a **gitignored** `tracker_secrets.properties`
- values are injected as `buildConfigField` entries
- CI writes the file from the `TRACKER_SECRETS` Actions secret
- `34d18fd` makes CI **fail** if the file exists but is empty (a silent empty secret
  would otherwise produce a build that logs users out rather than failing visibly)
- absence locally is **non-fatal** — a fresh clone warns and still builds

`build_pull_request.yml` gates every secret-dependent step with
`github.event.pull_request.user.login != 'dependabot[bot]'`, because Dependabot pull
requests do not receive Actions secrets and were therefore failing red. `Build app` is
deliberately left unguarded so Dependabot PRs still compile.

---

## 5. Secret rotation runbook (blocked — requires provider accounts)

> **Blocker:** rotating means registering NEXUS's *own* OAuth applications with
> Shikimori, Bangumi, Kitsu, AniList and MyAnimeList. The currently embedded
> registrations belong to **Komikku upstream** (`komikku://...-auth` redirect URIs), so
> rotating them in place would break upstream. This must be done by a human with
> accounts at those providers.

After this change the rotation is a **configuration-only** operation: no Kotlin edit and
no release branch required.

### Steps

1. **Register an application** at each provider, using NEXUS's own redirect URI
   (`nexus://<provider>-auth` — confirm the URI registered matches what the app expects
   before switching).
2. **Add the ids** to `tracker_secrets.properties` (gitignored):

   ```properties
   shikimori.client.id=<new>
   shikimori.client.secret=<new>
   bangumi.client.id=<new>
   bangumi.client.secret=<new>
   kitsu.client.id=<new>
   kitsu.client.secret=<new>
   anilist.client.id=<new>
   myanimelist.client.id=<new>
   ```

3. **Update the `TRACKER_SECRETS` Actions secret** with the same file contents so CI and
   release builds match local builds. (`34d18fd` will fail the build if the file is
   written but empty.)
4. **Revoke the old registrations** at each provider only after a build from step 3
   successfully logs in.
5. **Verify** a tracker login on-device from a release build.

Every id has its current upstream Komikku value as the BuildConfig default, so omitting a
key keeps today's behaviour exactly. That makes this change safe to land before the
provider registrations exist.

---

## 6. Accepted risks

### 6.1 Secrets remain in git history and inside every APK

`f817b6e` removed the client secrets from *source*, but they remain reachable in earlier
commits, and they must ship inside the APK regardless — a client secret used by an
installed app is never confidential to anyone who can download that app.

**Consequence:** anyone with repository read access, or anyone with a copy of a distributed
APK, can extract these values. This is inherent to the OAuth installed-client model and is
the primary reason rotation to NEXUS-owned registrations (§5) matters.

**Mitigation:** full history rewrite would invalidate every clone and is not worth it for
a personal fork; rotation at the provider is the correct remedy.

### 6.2 Pre-existing backups are unsanitized

Backups created **before** `0c22ece` contain plaintext credentials, and nothing in the app
can retroactively scrub a file the user already exported, shared, or stored in cloud sync.

**Action required by the user:** delete or re-create any backup produced by a version
older than 1.5.2.

---

## 7. Recommendations

1. ~~Unit-test `PreferenceBackupCreator`~~ — **done**: `PreferenceBackupCreatorTest`
   covers it, is mutation-checked, and runs in CI (§3).
2. **Complete the rotation** in §5 — the highest-value remaining action, since §6.1 makes
   the current registrations permanently extractable.
3. **Re-create pre-1.5.2 backups** (§6.2).
4. **Install and exercise 1.5.2 on a real device** — migration is verified on an emulator
   only; tracker logins after the `BuildConfig` change remain unexercised.

---

## 8. Change log of the audit itself

| Commit | Change |
|---|---|
| `0c22ece` | Credentials out of default backups/sync; Kavita `APIKEY` export filter |
| `3b9b85a` | `sync_privateSettings` defaults to `false` |
| `5a6ba52` | Closed `.gitignore` secret gaps; dropped stale upstream doc links |
| `2148e55` | Extension-store add requires confirmation (dialog **and** deep link) |
| `606fbb0` | Preview CI builds no longer pass `-Pinclude-telemetry` |
| `f817b6e` | Client secrets → `BuildConfig` fed by gitignored properties file |
| `34d18fd` | CI fails if the tracker secrets file is empty |
| `1a4efe4` | Dependabot PRs skip secret-writing CI steps |
| `722a02d`, `4552261` | Removed stale `update_website.yml` from two branches |
| *this change* | Tracker client ids → `BuildConfig`; this document |
| *follow-up* | `PreferenceBackupCreatorTest` closes the last verification gap (§3) |

Merged as PR #7 (`aea00e9`), PR #8 (`db934cf`), PR #9 (`ce841cd`).
