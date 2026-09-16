package tachiyomi.domain.release.interactor

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.release.interactor.GetApplicationRelease.Companion.compareVersionParts
import tachiyomi.domain.release.interactor.GetApplicationRelease.Companion.parseVersionParts

/**
 * Focused tests for the version-tag parsing/comparison utility used by [GetApplicationRelease].
 * These mirror the exact cases called out in the update-system audit.
 */
class VersionComparisonTest {

    private fun isUpdate(old: String, new: String): Boolean {
        val oldParts = parseVersionParts(old) ?: return false
        val newParts = parseVersionParts(new) ?: return false
        return compareVersionParts(newParts, oldParts) > 0
    }

    @Test
    fun `detects updates across patch, minor and major versions`() {
        isUpdate(old = "1.2.2", new = "1.2.3") shouldBe true
        isUpdate(old = "1.2.2", new = "1.3.0") shouldBe true
        isUpdate(old = "1.2.2", new = "2.0.0") shouldBe true
    }

    @Test
    fun `does not flag equal or older versions as updates`() {
        isUpdate(old = "1.2.2", new = "1.2.2") shouldBe false
        isUpdate(old = "1.2.2", new = "1.2.1") shouldBe false
    }

    @Test
    fun `compares numerically, not lexicographically`() {
        isUpdate(old = "1.2.10", new = "1.2.9") shouldBe false
        isUpdate(old = "1.2.9", new = "1.2.10") shouldBe true
    }

    @Test
    fun `treats a missing patch component as zero`() {
        isUpdate(old = "1.2", new = "1.2.0") shouldBe false
        isUpdate(old = "1.2.0", new = "1.2") shouldBe false
        isUpdate(old = "1.2", new = "1.2.1") shouldBe true
    }

    @Test
    fun `handles a leading v prefix the same as no prefix`() {
        isUpdate(old = "1.2.2", new = "v1.2.3") shouldBe true
        parseVersionParts("v1.2.3") shouldBe parseVersionParts("1.2.3")
    }

    @Test
    fun `drops pre-release or build suffixes instead of merging their digits in`() {
        // Must not become "1.2.32" - the trailing "2" from "-beta2" must not merge into "3"
        parseVersionParts("1.2.3-beta2") shouldBe listOf(1, 2, 3)
        parseVersionParts("v1.2.3-rc1") shouldBe listOf(1, 2, 3)
        parseVersionParts("1.2.3+build4") shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `malformed tags fail safely instead of crashing`() {
        parseVersionParts("not-a-version") shouldBe null
        parseVersionParts("") shouldBe null
        parseVersionParts("1..2") shouldBe null
        parseVersionParts("v") shouldBe null
        parseVersionParts("1.2.") shouldBe null
    }
}
