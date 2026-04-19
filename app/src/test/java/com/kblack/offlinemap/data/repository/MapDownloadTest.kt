package com.kblack.offlinemap.data.repository

import android.content.Context
import androidx.work.WorkManager
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.AppLifecycleProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class MapDownloadTest {

    //todo: It can be replaced by robolectric @RunWith(RobolectricTestRunner::class) | context = ApplicationProvider.getApplicationContext()
    // https://developer.android.com/training/testing/local-tests/robolectric
    private val context: Context = mockk(relaxed = true)
    private val lifecycleProvider: AppLifecycleProvider = mockk(relaxed = true)
    private val workManager: WorkManager = mockk()
    private lateinit var repo: MapDownloadRepositoryImpl
    private lateinit var mockDir: File
    private val mockMap = MapModel(
        mapId = "vn",
        name = "Vietnam_test",
        time = "2026-3-13",
        description = "Test description",
        sizeInBytes = 757657149L,
        continent = "Asia",
        allow = true,
        normalizedName = "vn_map_test",
        downloadFileName = "vn_map_test.tar.zst",
        pmtilesName = "vn_test.pmtiles",
        url = "https://test/vn_map_test.tar.zst",
        totalBytes = 757657149L
    )

    @Before
    fun setup() {
        mockDir = Files.createTempDirectory(mockMap.normalizedName).toFile()

        every { context.getExternalFilesDir(null) } returns mockDir
        every { lifecycleProvider.isAppInForeground } returns false

        repo = MapDownloadRepositoryImpl(
            context = context,
            lifecycleProvider = lifecycleProvider,
            workManager = workManager
        )
    }

    @After
    fun tearDown() {
        mockDir.deleteRecursively()
    }

    @Test
    fun `MapDownloadRepositoryImpl should NOT_DOWNLOADED when no files exist`() {
        val actual = repo.getLocalMapStatus(mockMap)
        assertEquals(MapDownloadStatusType.NOT_DOWNLOADED, actual.status)
    }

    @Test
    fun `MapDownloadRepositoryImpl getStyleJsonPath should null when style_runtime json does not exist`() {
        assertNull(repo.getStyleJsonPath(mockMap))
    }

    @Test
    fun `MapDownloadRepositoryImpl getGraphPath should path when graph-cache directory exists`() {
        val graphDir = File(mockDir, "${mockMap.normalizedName}/graph-cache")
        graphDir.mkdirs()

        assertEquals(graphDir.absolutePath, repo.getGraphPath(mockMap))
    }

    @Test
    fun `MapDownloadRepositoryImpl getGraphPath should null when graph-cache does not exist`() {
        assertNull(repo.getGraphPath(mockMap))
    }

    //PARTIALLY_DOWNLOADED, SUCCEEDED , deleteMap

}