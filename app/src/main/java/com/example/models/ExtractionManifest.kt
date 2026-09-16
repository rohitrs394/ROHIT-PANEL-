package com.example.models

data class ExtractionRecord(
  val filename: String,
  val relativePath: String,
  val absolutePath: String,
  val fileSize: Long,
  val extractedAt: Long = System.currentTimeMillis()
)

data class ExtractionManifest(
  val targetDirectory: String,
  val extractedFiles: List<ExtractionRecord> = emptyList(),
  val completedAt: Long = System.currentTimeMillis()
)
