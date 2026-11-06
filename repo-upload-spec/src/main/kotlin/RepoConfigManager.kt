import com.google.gson.Gson
import java.io.File
import java.nio.file.Path

class RepoConfigManager(private val allowedExtensions : Array<String>) {
    private val gson : Gson = Gson()
    fun createRootConfig(repoPath : Path) {
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

    internal fun generateFileConfig(nameParts : Triple<String,String,String>, fullPath : Path, newFileName : String, dependencies: Array<String>) {
        val jsonMap =  mapOf(
            "group" to nameParts.first,
            "project" to nameParts.second,
            "version" to nameParts.third,
            "dependencies" to dependencies
        )

        val fileConfig = gson.toJson(jsonMap)

        val targetFile = File(fullPath.toFile(), "$newFileName.json")
        targetFile.writeText(fileConfig)
    }
}