import java.io.File
import java.io.FileOutputStream
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile

class DriveFileManager(val drive: Drive) : FileManager() {

    fun getTargetFile(currentParentId: String) : DriveFile {
        val files = drive.files().list()
            .setQ("'$currentParentId' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
            .files

        return files.firstOrNull{file -> !file.name.endsWith(".json")} ?: throw Exception("File not found")
    }

    fun processAndSaveFile(downloadPath: String, targetFile: DriveFile) {
        val downloadDir = File(downloadPath)
        if(!downloadDir.exists()) downloadDir.mkdirs()

        val fileBaseName = targetFile.name.substringBeforeLast("-")
        val dirFiles = downloadDir.listFiles(){file -> file.isFile && file.name.startsWith(fileBaseName) }
        if(dirFiles.isNullOrEmpty()) {

            try {
                FileOutputStream(File(downloadDir, targetFile.name)).use { outputStream ->
                    drive.files().get(targetFile.id)
                        .executeMediaAndDownloadTo(outputStream)
                }
                print("Downloading ${targetFile.name} to ${downloadDir.name}")
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val fileInDir = dirFiles.first()

        val v1 = extractVersion(targetFile.name)
        val v2 = extractVersion(fileInDir.name)

        val comparedValue = compare(v1, v2)

        if(comparedValue > 0){
            try{
                FileOutputStream(File(downloadDir, targetFile.name)).use { outputStream ->
                    drive.files().get(targetFile.id)
                        .executeMediaAndDownloadTo(outputStream)
                }
                fileInDir.delete()
            }catch (e: Exception){
                println(e.message)
            }
            println("Updated to newer version: ${targetFile.name}")
        }else {
            println("Skipping ${targetFile.name}; an equal or newer version already exists.")
        }

    }
}