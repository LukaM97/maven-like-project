import com.google.gson.Gson
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Scanner
import kotlin.io.path.createDirectories

class RepoUploader {
    val rootPath: Path = Paths.get(System.getProperty("user.home"), "Desktop")

    fun upload(filePath: String, nameParts: Triple<String, String, String>) {
        val repoDir = rootPath.resolve("repo_").toFile()
        var repoPath : Path? = null
        if(!repoDir.exists()) {
            repoPath = Files.createDirectory(repoDir.toPath())
            println("----------------------------------")
            println("root folder created at $rootPath")
            println("----------------------------------")

            createRootConfig(repoPath)
        }else{
            repoPath = repoDir.toPath()
        }

        val fullPath = repoPath.resolve(nameParts.first).resolve(nameParts.second).resolve(nameParts.third)
        fullPath.createDirectories()

        val newFileName = "${nameParts.first}-${nameParts.second}-${nameParts.third}"

        var file : File? = null
        try{
            file = File(filePath)
            file.copyTo(File(fullPath.resolve(newFileName).toString()), false)
            generateFileConfig(nameParts, fullPath, newFileName)

        }catch(e : Exception){
            println(e.message)
        }finally {
            if(file != null) {
                println("Uploading ${file.name} to $fullPath ...")
            }
        }

    }

    fun createRootConfig(repoPath : Path) {
        val gson : Gson = Gson()

        val allowedExtensions : Array<String> = arrayOf(".rar",".zip",".json",".jar")

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

    fun generateFileConfig(nameParts : Triple<String,String,String>, fullPath : Path, newFileName : String) {
        val gson : Gson = Gson()

        val jsonMap =  mapOf(
            "group" to nameParts.first,
            "project" to nameParts.second,
            "version" to nameParts.third
        )

        val fileConfig = gson.toJson(jsonMap)

        val targetFile = File(fullPath.toFile(), "$newFileName.json")
        targetFile.writeText(fileConfig)

    }
}