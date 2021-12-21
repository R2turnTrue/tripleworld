package xyz.r2turntrue.tripleworld

import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


object FileUtils {

    private class CopyDirFileVisitor(
        private val sourceDir: Path,
        private val targetDir: Path,
        excludeFiles: List<String>
    ) :
        SimpleFileVisitor<Path>() {
        private val excludeFiles: List<String>?

        init {
            this.excludeFiles = excludeFiles
        }

        @Throws(IOException::class)
        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
            val newDir = targetDir.resolve(sourceDir.relativize(dir))
            if (!Files.isDirectory(newDir)) {
                Files.createDirectory(newDir)
            }
            return FileVisitResult.CONTINUE
        }

        @Throws(IOException::class)
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            // Pass files that are set to ignore
            if (excludeFiles != null && excludeFiles.contains(file.fileName.toString())) return FileVisitResult.CONTINUE
            // Copy the files
            val targetFile = targetDir.resolve(sourceDir.relativize(file))
            Files.copy(file, targetFile, StandardCopyOption.COPY_ATTRIBUTES)
            return FileVisitResult.CONTINUE
        }
    }

    fun copyFolder(source: File, target: File, excludeFiles: List<String>): Boolean {
        val sourceDir = source.toPath()
        val targetDir = target.toPath()
        return try {
            Files.walkFileTree(sourceDir, CopyDirFileVisitor(sourceDir, targetDir, excludeFiles))
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

}