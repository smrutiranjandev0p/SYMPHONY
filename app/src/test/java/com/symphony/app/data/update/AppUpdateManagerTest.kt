package com.symphony.app.data.update

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppUpdateManagerTest {

    private val sampleAssets = listOf(
        ReleaseAsset(
            name = "SYMPHONY-v1.1-android7.apk",
            downloadUrl = "https://github.com/smrutiranjandev0p/SYMPHONY/releases/download/1.1/SYMPHONY-v1.1-android7.apk",
        ),
        ReleaseAsset(
            name = "SYMPHONY-v1.1-universal-uncompressed.apk",
            downloadUrl = "https://github.com/smrutiranjandev0p/SYMPHONY/releases/download/1.1/SYMPHONY-v1.1-universal-uncompressed.apk",
        ),
        ReleaseAsset(
            name = "SYMPHONY-v1.1-universal.apk",
            downloadUrl = "https://github.com/smrutiranjandev0p/SYMPHONY/releases/download/1.1/SYMPHONY-v1.1-universal.apk",
        ),
        ReleaseAsset(
            name = "SHA256SUMS.txt",
            downloadUrl = "https://github.com/smrutiranjandev0p/SYMPHONY/releases/download/1.1/SHA256SUMS.txt",
        ),
    )

    @Test
    fun classifyAsset_recognizesVariantsCorrectly() {
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-android7.apk"))
            .isEqualTo(ApkVariant.ANDROID7)
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-legacy.apk"))
            .isEqualTo(ApkVariant.ANDROID7)

        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-universal-uncompressed.apk"))
            .isEqualTo(ApkVariant.UNCOMPRESSED)
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-raw.apk"))
            .isEqualTo(ApkVariant.UNCOMPRESSED)
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-beta-raw.apk"))
            .isEqualTo(ApkVariant.UNCOMPRESSED)

        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-universal.apk"))
            .isEqualTo(ApkVariant.UNIVERSAL)
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-release.apk"))
            .isEqualTo(ApkVariant.UNIVERSAL)
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-beta.apk"))
            .isEqualTo(ApkVariant.UNIVERSAL)
    }

    @Test
    fun selectBestAsset_standardAndroid10Plus_selectsUniversalApk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = false,
            sdkInt = 34, // Android 14
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("SYMPHONY-v1.1-universal.apk")
        assertThat(selected?.downloadUrl).contains("SYMPHONY-v1.1-universal.apk")
    }

    @Test
    fun selectBestAsset_uncompressedBuild_selectsUncompressedApk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = true,
            sdkInt = 34, // Android 14
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("SYMPHONY-v1.1-universal-uncompressed.apk")
        assertThat(selected?.downloadUrl).contains("SYMPHONY-v1.1-universal-uncompressed.apk")
    }

    @Test
    fun selectBestAsset_legacyAndroid_selectsAndroid7Apk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = false,
            sdkInt = 26, // Android 8.0 Oreo
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("SYMPHONY-v1.1-android7.apk")
        assertThat(selected?.downloadUrl).contains("SYMPHONY-v1.1-android7.apk")
    }

    @Test
    fun selectBestAsset_ignoresNonApkAssets() {
        val nonApkOnly = listOf(
            ReleaseAsset(name = "SHA256SUMS.txt", downloadUrl = "https://example.com/sha"),
            ReleaseAsset(name = "notice.md", downloadUrl = "https://example.com/md"),
        )
        val selected = AppUpdateManager.selectBestAsset(
            assets = nonApkOnly,
            isUncompressed = false,
            sdkInt = 34,
        )
        assertThat(selected).isNull()
    }

    @Test
    fun selectBestAsset_fallbackWhenUniversalMissing_fallsBackToUncompressedOrAndroid7() {
        val assetsWithoutUniversal = listOf(
            ReleaseAsset(name = "SYMPHONY-v1.1-android7.apk", downloadUrl = "https://example.com/a7"),
            ReleaseAsset(name = "SYMPHONY-v1.1-universal-uncompressed.apk", downloadUrl = "https://example.com/raw"),
        )
        val selected = AppUpdateManager.selectBestAsset(
            assets = assetsWithoutUniversal,
            isUncompressed = false,
            sdkInt = 34,
        )
        assertThat(selected).isNotNull()
        assertThat(selected?.name).isEqualTo("SYMPHONY-v1.1-universal-uncompressed.apk")
    }

    private val assetsWithVivo = sampleAssets + ReleaseAsset(
        name = "SYMPHONY-v1.1-vivo-origin.apk",
        downloadUrl = "https://example.com/vivo",
    )

    @Test
    fun classifyAsset_recognizesVivoOrigin() {
        assertThat(AppUpdateManager.classifyAsset("SYMPHONY-v1.1-vivo-origin.apk"))
            .isEqualTo(ApkVariant.VIVO_ORIGIN)
    }

    @Test
    fun selectBestAsset_vivoBuild_onlySelectsVivoApk() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = assetsWithVivo,
            isUncompressed = false,
            sdkInt = 34,
            isVivoOrigin = true,
        )
        assertThat(selected?.name).isEqualTo("SYMPHONY-v1.1-vivo-origin.apk")
    }

    @Test
    fun selectBestAsset_vivoBuild_withoutVivoApk_selectsNothing() {
        val selected = AppUpdateManager.selectBestAsset(
            assets = sampleAssets,
            isUncompressed = false,
            sdkInt = 34,
            isVivoOrigin = true,
        )
        assertThat(selected).isNull()
    }

    @Test
    fun selectBestAsset_normalBuild_neverSelectsVivoApk() {
        val onlyVivo = listOf(ReleaseAsset(name = "SYMPHONY-v1.1-vivo-origin.apk", downloadUrl = "https://example.com/vivo"))
        assertThat(
            AppUpdateManager.selectBestAsset(assets = onlyVivo, isUncompressed = false, sdkInt = 34),
        ).isNull()
        assertThat(
            AppUpdateManager.selectBestAsset(assets = assetsWithVivo, isUncompressed = false, sdkInt = 34)?.name,
        ).isEqualTo("SYMPHONY-v1.1-universal.apk")
    }

    @Test
    fun digestMatches_acceptsOnlyTheGitHubSha256OfTheFile() {
        val file = java.io.File.createTempFile("update", ".apk").apply {
            writeText("abc")
            deleteOnExit()
        }
        val abcSha256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertThat(AppUpdateManager.digestMatches(file, "sha256:$abcSha256")).isTrue()
        assertThat(AppUpdateManager.digestMatches(file, "sha256:${abcSha256.uppercase()}")).isTrue()
        assertThat(AppUpdateManager.digestMatches(file, "sha256:" + "0".repeat(64))).isFalse()
        assertThat(AppUpdateManager.digestMatches(file, abcSha256)).isFalse()
        assertThat(AppUpdateManager.digestMatches(file, "")).isFalse()
    }
}
