import com.google.api.services.drive.model.File as DriveFile
import com.google.api.client.http.FileContent
import java.io.File

class DriveRepoDriver : RepoManager() {
    override val type = RepoType.DRIVE
    private val folderManager = FolderManager(drive)
    private val fileManager = DriveFileManager(drive)
    private val dv = DriveDependencyValidator(drive)

    override fun initializeRepository() {
        val folder = folderManager.createRootFolder()
        rcm.createDriveRootConfig()
        println("----------------------------------")
        println("root folder ${folder.id} created")
        println("----------------------------------")
    }

    override fun upload(file: File, nameParts: List<String>, dependencies: Array<String>) {

        initializeRepository()

        val currentParent = folderManager.createFolder(nameParts)

        val type = when{
            file.name.endsWith(".zip") -> "application/zip"
            file.name.endsWith(".rar") -> "application/vnd.rar"
            file.name.endsWith(".jar") -> "application/java-archive"
            file.name.endsWith(".json") -> "application/json"
            else -> return
        }

        val fullFileName = "${nameParts[0]}-${nameParts[1]}-${nameParts[2]}.${file.extension}"

        val uploaded = drive.files().create(
            DriveFile()
                .setName(fullFileName)
                .setParents(listOf(currentParent.id)),
            FileContent(type, file)
        ).setFields("id, name").execute()

        rcm.generateDriveFileConfig(nameParts,currentParent.id, dependencies)

        println("Uploading ${uploaded.name} to google drive...")
    }

    override fun download(downloadPath: String, folderNames: List<String>) {
        val targetFolderId = folderManager.resolveFolderPath(folderNames)
        val targetFiles = fileManager.getTargetFile(targetFolderId)
        val file = targetFiles.firstOrNull{!it.name.endsWith(".json")}?:throw Exception("File not found")
        val config = targetFiles.firstOrNull{it.name.endsWith(".json")}?:throw Exception("Config file not found")
        val dependencyFolders = dv.getPreparedDependencyFolders(config)
        if(dependencyFolders.isNotEmpty()){
            dependencyFolders.forEach {
                    folderNames -> download(downloadPath, folderNames)
            }
        }

        fileManager.processAndSaveFile(downloadPath, file)
    }
}