package com.example.extraction

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.models.ExtractionManifest
import com.example.models.ExtractionRecord
import com.example.security.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipAssetExtractor {

  private const val TAG = "ZipAssetExtractor"
  private const val ASSET_ZIP_NAME = "vip_assets.zip"
  private const val TARGET_SUBDIR = "pdfreader/folder"

  data class ExtractionProgress(
    val currentFile: String = "",
    val filesExtracted: Int = 0,
    val totalFiles: Int = 0,
    val percent: Float = 0f,
    val statusMessage: String = "",
    val isComplete: Boolean = false,
    val error: String? = null,
    val destinationPath: String = ""
  )

  /**
   * Resolves the safest, highest-priority writable destination directory.
   * 1. /storage/emulated/0/Documents/pdfreader/folder/
   * 2. App-specific external Documents fallback if scoped storage blocks root public documents
   */
  fun getDestinationDirectory(context: Context): File {
    val primary = File("/storage/emulated/0/Documents", TARGET_SUBDIR)
    try {
      if (primary.exists() || primary.mkdirs()) {
        if (primary.canWrite()) {
          return primary
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Primary destination not writable: ${e.message}")
    }

    // Android 10+ standard public directory
    val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val publicTarget = File(publicDocs, TARGET_SUBDIR)
    try {
      if (publicTarget.exists() || publicTarget.mkdirs()) {
        if (publicTarget.canWrite()) {
          return publicTarget
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Public docs destination not writable: ${e.message}")
    }

    // Modern Scoped Storage guaranteed app external files fallback
    val appDocs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
    val fallback = File(appDocs, TARGET_SUBDIR)
    fallback.mkdirs()
    return fallback
  }

  suspend fun extractVipAssets(
    context: Context,
    onProgress: (ExtractionProgress) -> Unit
  ): Result<ExtractionManifest> = withContext(Dispatchers.IO) {
    try {
      onProgress(ExtractionProgress(statusMessage = "Locating bundled asset package..."))

      // 1. Verify asset exists
      val assetList = context.assets.list("") ?: emptyArray()
      if (!assetList.contains(ASSET_ZIP_NAME)) {
        val errorMsg = "ASSET NOT FOUND: $ASSET_ZIP_NAME missing from APK"
        onProgress(ExtractionProgress(statusMessage = errorMsg, error = errorMsg))
        return@withContext Result.failure(IOException(errorMsg))
      }

      // 2. Count entries & validate archive integrity
      onProgress(ExtractionProgress(statusMessage = "Validating package integrity..."))
      var entryCount = 0
      context.assets.open(ASSET_ZIP_NAME).use { input ->
        ZipInputStream(BufferedInputStream(input)).use { zis ->
          var entry: ZipEntry? = zis.nextEntry
          while (entry != null) {
            entryCount++
            entry = zis.nextEntry
          }
        }
      }

      if (entryCount == 0) {
        val errorMsg = "CORRUPT ARCHIVE: Empty zip package"
        onProgress(ExtractionProgress(statusMessage = errorMsg, error = errorMsg))
        return@withContext Result.failure(IOException(errorMsg))
      }

      val destDir = getDestinationDirectory(context)
      val canonicalDestDirPath = destDir.canonicalPath
      val extractedRecords = mutableListOf<ExtractionRecord>()
      val sessionManager = SessionManager(context)
      val trackedPaths = sessionManager.getExtractedFilesManifest().toMutableSet()

      onProgress(
        ExtractionProgress(
          statusMessage = "Extracting to ${destDir.absolutePath}...",
          totalFiles = entryCount,
          destinationPath = destDir.absolutePath
        )
      )

      // 3. Extract each file safely with Zip-Slip protection
      context.assets.open(ASSET_ZIP_NAME).use { input ->
        ZipInputStream(BufferedInputStream(input)).use { zis ->
          var entry: ZipEntry? = zis.nextEntry
          var currentCount = 0

          while (entry != null) {
            currentCount++
            val entryName = entry.name

            // Path Traversal (Zip-Slip) Security Check
            val targetFile = File(destDir, entryName).canonicalFile
            if (!targetFile.path.startsWith(canonicalDestDirPath)) {
              val secError = "SECURITY VIOLATION: Path traversal attempt ($entryName)"
              Log.e(TAG, secError)
              onProgress(ExtractionProgress(statusMessage = secError, error = secError))
              return@withContext Result.failure(SecurityException(secError))
            }

            if (entry.isDirectory) {
              targetFile.mkdirs()
            } else {
              targetFile.parentFile?.mkdirs()

              var bytesWritten = 0L
              FileOutputStream(targetFile).use { fos ->
                BufferedOutputStream(fos).use { bos ->
                  val buffer = ByteArray(8192)
                  var read: Int
                  while (zis.read(buffer).also { read = it } != -1) {
                    bos.write(buffer, 0, read)
                    bytesWritten += read
                  }
                  bos.flush()
                }
              }

              val record = ExtractionRecord(
                filename = targetFile.name,
                relativePath = entryName,
                absolutePath = targetFile.absolutePath,
                fileSize = bytesWritten
              )
              extractedRecords.add(record)
              trackedPaths.add(targetFile.absolutePath)
            }

            val progressPercent = (currentCount.toFloat() / entryCount.toFloat()) * 100f
            onProgress(
              ExtractionProgress(
                currentFile = entryName,
                filesExtracted = currentCount,
                totalFiles = entryCount,
                percent = progressPercent,
                statusMessage = "Extracted: $entryName (${currentCount}/$entryCount)",
                destinationPath = destDir.absolutePath
              )
            )

            zis.closeEntry()
            entry = zis.nextEntry
          }
        }
      }

      // Persist the list of newly created files so we can surgically delete them when turned OFF
      sessionManager.saveExtractedFilesManifest(trackedPaths)

      val manifest = ExtractionManifest(
        targetDirectory = destDir.absolutePath,
        extractedFiles = extractedRecords
      )

      onProgress(
        ExtractionProgress(
          statusMessage = "EXTRACTION COMPLETE - ${extractedRecords.size} FILES READY",
          filesExtracted = entryCount,
          totalFiles = entryCount,
          percent = 100f,
          isComplete = true,
          destinationPath = destDir.absolutePath
        )
      )

      Result.success(manifest)

    } catch (e: Exception) {
      Log.e(TAG, "Extraction failed: ${e.message}", e)
      val errorMsg = "EXTRACTION FAILED: ${e.localizedMessage ?: "Unknown Error"}"
      onProgress(ExtractionProgress(statusMessage = errorMsg, error = errorMsg))
      Result.failure(e)
    }
  }

  /**
   * Surgical Cleanup when feature is turned OFF:
   * Only deletes files tracked in the manifest, leaving all unrelated user files untouched.
   */
  fun cleanupExtractedFiles(context: Context): Int {
    val sessionManager = SessionManager(context)
    val filesToDelete = sessionManager.getExtractedFilesManifest()
    var deletedCount = 0

    for (filePath in filesToDelete) {
      try {
        val file = File(filePath)
        if (file.exists() && file.isFile) {
          if (file.delete()) {
            deletedCount++
            Log.d(TAG, "Surgically deleted: $filePath")
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed to delete $filePath: ${e.message}")
      }
    }

    sessionManager.clearExtractedFilesManifest()
    return deletedCount
  }
}
