import com.google.api.services.drive.model.File as DriveFile
import com.google.api.services.drive.Drive
import com.google.api.client.http.FileContent
import com.google.gson.Gson
import java.io.File
import java.nio.file.Path

class RepoConfigManager(private val allowedExtensions : Array<String>, var drive: Drive) {
    private val gson : Gson = Gson()

    fun createLocalRootConfig(repoPath : Path){
        val scopes = arrayOf(
            mapOf("name" to "compile", "transitive" to "true"),
            mapOf("name" to "runtime", "transitive" to "true"),
            mapOf("name" to "test", "transitive" to "false"))

        val jsonMap =  mapOf(
            "allowedExtensions" to allowedExtensions,
            "scopes" to scopes
        )

        val rootConfig = gson.toJson(jsonMap)

        val targetFile = File(repoPath.toFile(), "repo_config.json")
        targetFile.writeText(rootConfig)
    }

    fun generateLocalFileConfig(nameParts : Triple<String,String,String>, fullPath : Path, dependencies: Array<String>){
        val jsonMap =  mapOf(
            "group" to nameParts.first,
            "project" to nameParts.second,
            "version" to nameParts.third,
            "dependencies" to dependencies
        )

        val fileConfig = gson.toJson(jsonMap)
        val newFileName = "${nameParts.first}-${nameParts.second}-${nameParts.third}"

        val targetFile = File(fullPath.toFile(), "$newFileName.json")
        targetFile.writeText(fileConfig)
    }

    fun createDriveRootConfig() {
        val scopes = arrayOf(
            mapOf("name" to "compile", "transitive" to "true"),
            mapOf("name" to "runtime", "transitive" to "true"),
            mapOf("name" to "test", "transitive" to "false"))

        val jsonMap =  mapOf(
            "allowedExtensions" to allowedExtensions,
            "scopes" to scopes
        )

        val rootConfig = gson.toJson(jsonMap)
        val configFile = File.createTempFile("repo_config", ".json")
            .apply {
                writeText(rootConfig)
                deleteOnExit()
            }

        val parentId = getFolderId("_repo")

        try{
            drive.files().create(
                DriveFile()
                    .setName("repo_config.json")
                    .setParents(listOf(parentId)),
                FileContent("application/json", configFile)
            ).setFields("id").execute()
        }catch (e: Exception){
            println(e)
        }
    }

    fun generateDriveFileConfig(
        nameParts: Triple<String, String, String>,
        parentId: String,
        dependencies: Array<String>
    ) {
        val jsonMap =  mapOf(
            "group" to nameParts.first,
            "project" to nameParts.second,
            "version" to nameParts.third,
            "dependencies" to dependencies
        )

        val fileConfig = gson.toJson(jsonMap)
        val configFile = File.createTempFile("${nameParts.second}_config", ".json")
            .apply {
                writeText(fileConfig)
                deleteOnExit()
            }

        drive.files().create(
            DriveFile()
                .setName("${nameParts.first}_config.json")
                .setParents(listOf(parentId)),
            FileContent("application/json", configFile)
        ).setFields("id").execute()
    }

    private fun getFolderId(folderName: String): String {
        val query = "mimeType = 'application/vnd.google-apps.folder' and name = '$folderName' and trashed = false"
        val result = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val folders = result.files
        return folders[0].id
    }
}