import com.google.api.services.drive.Drive
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import com.google.api.services.drive.model.File as DriveFile

class DriveDependencyValidator(val drive: Drive) : DependencyValidator<DriveFile>() {

    override fun checkDependencies(dependencies: Array<String>, repoPath: DriveFile): Boolean {
        if(dependencies.isEmpty()) {
            return true
        }

        val rootFolderId = drive.files().list()
            .setQ("name = '\$_repo' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
            .setSpaces("drive")
            .setFields("files(id)")
            .setPageSize(1)
            .execute()
            .files
            .firstOrNull()
            ?.id

        for (dependency in dependencies) {
            val nameParts = dependency.split("-").toMutableList()
            prepareFolderNames(nameParts)

            val exists = checkNestedFolders(
                currentFolderId = rootFolderId!!,
                parts = nameParts,
                fileName = dependency
            )
            if (!exists) return false
        }
        return true
    }

    private fun checkNestedFolders(
        currentFolderId: String,
        parts: MutableList<String>,
        fileName: String,
        index: Int = 0
    ): Boolean {
        if (index >= parts.size) {
            return fileExistsInFolder(fileName = fileName, parentFolderId = currentFolderId)
        }

        val nextFolderName = parts[index]
        val nextFolderId = findFolderId(name = nextFolderName, parentId = currentFolderId) ?: return false

        return checkNestedFolders(
            currentFolderId = nextFolderId,
            parts = parts,
            fileName = fileName,
            index = index + 1
        )
    }

    private fun findFolderId(name: String, parentId: String?): String? {
        val escapedName = name.replace("'", "\\'")
        val parentClause = if (parentId != null) " and '$parentId' in parents" else ""
        val query = "name = '$escapedName'$parentClause and mimeType = 'application/vnd.google-apps.folder' and trashed = false"

        return drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id)")
            .setPageSize(1)
            .execute()
            .files
            .firstOrNull()
            ?.id
    }

    private fun fileExistsInFolder(fileName: String, parentFolderId: String): Boolean {
        val escapedFileName = fileName.replace("'", "\\'")
        val query = "name = '$escapedFileName' and '$parentFolderId' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false"

        return drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id)")
            .setPageSize(1)
            .execute()
            .files
            .isNotEmpty()
    }

    fun getDependencies(configFile: DriveFile): List<String> {
        val jsonContent = drive.files().get(configFile.id)
            .executeMediaAsInputStream()
            .use { inputStream ->
                inputStream
                    .bufferedReader(StandardCharsets.UTF_8)
                    .readText()
            }

        val configObj = Gson().fromJson(jsonContent, fileConfig::class.java)
        return configObj?.dependencies?.toList() ?: emptyList()
    }

    fun getPreparedDependencyFolders(configFile : DriveFile) : List<List<String>> {
        val dependencies = getDependencies(configFile)
        return dependencies.map { dep ->
            val folderNames = dep.split("-").toMutableList()
            prepareFolderNames(folderNames)
            folderNames
        }
    }
}