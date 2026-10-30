import com.google.gson.Gson
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

abstract class RepoUploader() {

    abstract val type : RepoType
    internal val allowedExtensions : Array<String> = arrayOf("rar","zip","json","jar")

    abstract fun initializeRepository() : Path

    fun upload(filePath: String, nameParts: Triple<String, String, String>, dependencies: Array<String>) {

        val repoPath = initializeRepository()

        val file : File = File(filePath)
        if(!file.exists()) {
            println("File does not exist at: $filePath")
            return
        }

        if(!allowedExtensions.contains(file.extension)) {
            println("Invalid file extension.")
            return
        }

        if(!checkDependencies(dependencies, repoPath)){
            print("Missing dependencies in the repository")
            return
        }

        val newFileName = "${nameParts.first}-${nameParts.second}-${nameParts.third}.${file.extension}"

        val fullPath = getFullPath(nameParts, repoPath)
        if(!fullPath.exists()){
            fullPath.createDirectories()
        }
        if(fullPath.resolve(newFileName).exists()){
            println("File already exists")
            return
        }

        try{
            file.copyTo(File(fullPath.resolve(newFileName).toString()), false)
            generateFileConfig(nameParts, fullPath, newFileName, dependencies)

        }catch(e : Exception){
            println(e.message)
        }finally {
            println("Uploading ${file.name} to $fullPath ...")
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
            val nameParts = dependency.split("-").toTypedArray()
            exists = checkNestedFolders(repoPath.toFile(), nameParts, dependency)
            if(!exists) return false
        }

        return exists
    }

    protected fun createRootConfig(repoPath : Path) {
        val gson : Gson = Gson()

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