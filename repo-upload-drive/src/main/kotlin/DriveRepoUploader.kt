import com.google.api.services.drive.model.File as DriveFile
import com.google.api.client.http.FileContent
import rs.raf.drive.GoogleDriveAuth
import java.io.File

class DriveRepoUploader : RepoUploader() {
    override val type = RepoType.DRIVE
    val drive = GoogleDriveAuth.connect()

    override fun initializeRepository() {
        var pageToken: String? = null
        var folder: DriveFile?
        do {
            val page = drive.files().list()
                .setQ("name = '_repo' and mimeType = 'application/vnd.google-apps.folder' " +
                        "and 'root' in parents and trashed = false")
                .setOrderBy("createdTime") // Ako postoje duplikati, koristi najstariji.
                .setPageSize(1)
                .setFields("nextPageToken,files(id)")
                .setPageToken(pageToken)
                .execute()
            folder = page.files.firstOrNull()
            pageToken = page.nextPageToken
        } while (folder == null && pageToken != null)

        //ako _repo ne postoji kreiraj ga
        val folderId = (folder ?: drive.files().create(
            DriveFile().setName("_repo")
                .setMimeType("application/vnd.google-apps.folder")
        ).setFields("id").execute()).id
        println("Folder, folderId=$folderId")
    }

    override fun upload(
        file: File,
        nameParts: Triple<String, String, String>,
        dependencies: Array<String>
    ) {
        var currentParentId = "root"

        nameParts.toList().forEach { name ->
            var pageToken: String? = null
            var existingFolder: DriveFile? = null
            do {
                val page = drive.files().list()
                    .setQ("name = '$name' and mimeType = 'application/vnd.google-apps.folder' and '$currentParentId' in parents and trashed = false")
                    .setPageSize(1)
                    .setFields("nextPageToken,files(id)")
                    .setPageToken(pageToken)
                    .execute()
                existingFolder = page.files.firstOrNull()
                pageToken = page.nextPageToken
            } while (existingFolder == null && pageToken != null)

            currentParentId = existingFolder?.id ?: drive.files().create(
                DriveFile()
                    .setName(name)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(listOf(currentParentId))
            ).setFields("id").execute().id
        }

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
                .setParents(listOf(currentParentId)),
            FileContent(type, file)
        ).setFields("id").execute()

        println("Uploading $uploaded to google drive...")
    }
}