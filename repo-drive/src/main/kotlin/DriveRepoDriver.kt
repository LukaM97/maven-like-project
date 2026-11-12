import com.google.api.services.drive.model.File as DriveFile
import com.google.api.client.http.FileContent
import java.io.File

class DriveRepoDriver : RepoManager() {
    override val type = RepoType.DRIVE
    val folderManager = FolderManager()

    override fun initializeRepository() {
        val folder = folderManager.createRootFolder(drive)
        rcm.createDriveRootConfig()
        println("----------------------------------")
        println("root folder ${folder.id} created")
        println("----------------------------------")
    }

    override fun upload(file: File, nameParts: Triple<String, String, String>, dependencies: Array<String>) {

        initializeRepository()

        val currentParent = folderManager.createFolder(drive,nameParts)

        val type = when{
            file.name.endsWith(".zip") -> "application/zip"
            file.name.endsWith(".rar") -> "application/vnd.rar"
            file.name.endsWith(".jar") -> "application/java-archive"
            file.name.endsWith(".json") -> "application/json"
            else -> return
        }

        val uploaded = drive.files().create(
            DriveFile()
                .setName(file.name)
                .setParents(listOf(currentParent.id)),
            FileContent(type, file)
        ).setFields("id").execute()

        rcm.generateDriveFileConfig(nameParts,currentParent.id, dependencies)

        println("Uploading ${uploaded.name} to google drive...")
    }

    override fun download(downloadPath: String, folderNames: Triple<String, String, String>) {
        TODO("Not yet implemented")
    }
}