import com.google.gson.Gson
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories

abstract class RepoUploader() {

    abstract val type : RepoType

    abstract fun initializeRepository() : Path

    fun upload(filePath: String, nameParts: Triple<String, String, String>, dependencies: Array<String>) {

        val repoPath = initializeRepository()

        if(!checkDependencies(dependencies, repoPath)){
            print("Missing dependencies in the repository")
            return
        }

        val fullPath = getFullPath(nameParts, repoPath)
        fullPath.createDirectories()

        val newFileName = "${nameParts.first}-${nameParts.second}-${nameParts.third}"

        var file : File? = null
        try{
            file = File(filePath)
            file.copyTo(File(fullPath.resolve(newFileName).toString()), false)
            generateFileConfig(nameParts, fullPath, newFileName, dependencies)

        }catch(e : Exception){
            println(e.message)
        }finally {
            if(file != null) {
                println("Uploading ${file.name} to $fullPath ...")
            }
        }
    }

    fun getFullPath(nameParts: Triple<String, String, String>, repoPath: Path) : Path {
        return repoPath.resolve(nameParts.first).resolve(nameParts.second).resolve(nameParts.third)
    }

    fun checkNestedFolders(currentPath: File, parts: Array<String>, fileName : String, index: Int = 0) : Boolean {
        if(index > parts.size - 1) {
            val targetFile = File(currentPath, fileName)
            return targetFile.exists() && targetFile.isFile
        }

        val nextFolder = currentPath.resolve(parts[index])

        return nextFolder.exists() && checkNestedFolders(nextFolder, parts, fileName, index + 1)
    }

    fun checkDependencies(dependencies: Array<String>, repoPath: Path) : Boolean {
        var exists = false
        if(dependencies.isEmpty()) {
            exists = true
        }
        dependencies.forEach { dependency ->
            var nameParts = dependency.split("-").toTypedArray()
            exists = checkNestedFolders(repoPath.toFile(), nameParts, dependency)
            if(!exists) return false
        }

        return exists
    }

    protected fun createRootConfig(repoPath : Path) {
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

    private fun generateFileConfig(nameParts : Triple<String,String,String>, fullPath : Path, newFileName : String, dependencies: Array<String>) {
        val gson : Gson = Gson()

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