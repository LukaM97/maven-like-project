import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

abstract class RepoUploader() {
    abstract val type : RepoType
    private val allowedExtensions : Array<String> = arrayOf("rar","zip","json","jar")
    var rcm = RepoConfigManager(allowedExtensions)
    private val dv = DependencyValidator()

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

        if(!dv.checkDependencies(dependencies, repoPath)){
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
            rcm.generateFileConfig(nameParts, fullPath, newFileName, dependencies)

        }catch(e : Exception){
            println(e.message)
        }finally {
            println("Uploading ${file.name} to $fullPath ...")
        }
    }

    fun getFullPath(nameParts: Triple<String, String, String>, repoPath: Path) : Path {
        return repoPath.resolve(nameParts.first).resolve(nameParts.second).resolve(nameParts.third)
    }
}