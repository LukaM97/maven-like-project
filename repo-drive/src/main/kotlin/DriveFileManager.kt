import java.io.File
import java.io.FileOutputStream
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile

class DriveFileManager(val drive: Drive) : FileManager() {

    fun getTargetFile(currentParentId: String) : List<DriveFile> {
        val files = drive.files().list()
            .setQ("'$currentParentId' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
            .files

        return files ?: throw Exception("Files not found")
    }

    fun processAndSaveFile(downloadPath: String, fileInRepo: DriveFile) {
        val downloadDir = File(downloadPath)
        if(!downloadDir.exists()) downloadDir.mkdirs()

        val fileBaseName = fileInRepo.name.substringBeforeLast("-")
        val dirFiles = downloadDir.listFiles(){file -> file.isFile && file.name.startsWith(fileBaseName) }
        if(dirFiles.isNullOrEmpty()) {

            try {
                FileOutputStream(File(downloadDir, fileInRepo.name)).use { outputStream ->
                    drive.files().get(fileInRepo.id)
                        .executeMediaAndDownloadTo(outputStream)
                }
                println("Downloading ${fileInRepo.name} to ${downloadDir.name}")
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val fileInDir = dirFiles.first()

        val v1 = extractVersion(fileInRepo.name)
        val v2 = extractVersion(fileInDir.name)

        val comparedValue = compare(v1, v2)

        if(comparedValue > 0){
            try{
                FileOutputStream(File(downloadDir, fileInRepo.name)).use { outputStream ->
                    drive.files().get(fileInRepo.id)
                        .executeMediaAndDownloadTo(outputStream)
                }
                fileInDir.delete()
            }catch (e: Exception){
                println(e.message)
            }
            println("Updated to newer version: ${fileInRepo.name}")
        }else {
            println("Skipping ${fileInRepo.name}; an equal or newer version already exists.")
        }

    }
}