import java.io.File
import java.io.FileOutputStream
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile

class DriveFileManager(val drive: Drive) {

    fun getTargetFile(currentParentId: String) : DriveFile {
        val files = drive.files().list()
            .setQ("'$currentParentId' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
            .files

        return files.firstOrNull{file -> !file.name.endsWith(".json")} ?: throw Exception("File not found")
    }

    fun saveFile(downloadPath: String, targetFile: DriveFile) {
        val folder = File(downloadPath)
        folder.mkdirs()

        try {
            FileOutputStream(File(folder, targetFile.name)).use { outputStream ->
                drive.files().get(targetFile.id)
                    .executeMediaAndDownloadTo(outputStream)
            }
            print("Downloading ${targetFile.name} to ${folder.name}")
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}