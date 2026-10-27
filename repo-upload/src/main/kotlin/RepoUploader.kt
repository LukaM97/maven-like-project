import com.google.gson.Gson
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory

class RepoUploader {
    val rootPath: String = "C:\\Users\\Ryzen\\Desktop"

    fun upload(file: File) {
        var path : Path? = null
        if(!File(rootPath).exists()) {
            path = createTempDirectory(Paths.get("C:\\Users\\Ryzen\\Desktop"),"repo_")
            println("root folder created at $path")

            createRootConfig()
        }
    }

    fun createRootConfig(){
        val rootConfig : Gson = Gson()

        val allowedExtensions : Array<String> = arrayOf(".rar",".zip",".json",".jar")
        rootConfig.toJson(allowedExtensions)

        val scopes = arrayOf(
            mapOf("name" to "compile", "transitive" to "true"),
            mapOf("name" to "runtime", "transitive" to "true"),
            mapOf("name" to "test", "transitive" to "false"))
        rootConfig.toJson(scopes)
    }
}