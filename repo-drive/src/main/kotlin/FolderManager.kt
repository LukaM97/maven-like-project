import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile

class FolderManager {
    var folder: DriveFile? = null

    fun createRootFolder(drive: Drive): DriveFile {
        var pageToken: String? = null
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
        folder = (folder ?: drive.files().create(
            DriveFile().setName("_repo")
                .setMimeType("application/vnd.google-apps.folder")
        ).setFields("id").execute())

        return folder!!
    }

    fun createFolder(drive: Drive, nameParts: List<String>): DriveFile {
        val currentParent = folder

        nameParts.toList().forEach { name ->
            var pageToken: String? = null
            var existingFolder: DriveFile? = null
            do {
                val page = drive.files().list()
                    .setQ("name = '$name' and mimeType = 'application/vnd.google-apps.folder' and '${currentParent!!.id}' in parents and trashed = false")
                    .setPageSize(1)
                    .setFields("nextPageToken,files(id)")
                    .setPageToken(pageToken)
                    .execute()
                existingFolder = page.files.firstOrNull()
                pageToken = page.nextPageToken
            } while (existingFolder == null && pageToken != null)

            //Ako folder ne postoji, kreiraj ga
            currentParent.id = existingFolder?.id ?: drive.files().create(
                DriveFile()
                    .setName(name)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(listOf(currentParent.id))
            ).setFields("id").execute().id
        }
        return currentParent!!
    }
}