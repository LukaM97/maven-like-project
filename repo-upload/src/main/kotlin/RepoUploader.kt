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
            println("root folder created at $repoPath")
            println("----------------------------------")

            createRootConfig()
        }else{
            repoPath = repoDir.toPath()
        }

        val scanner : Scanner = Scanner(System.`in`)

        //val filePath : String = getFilePath(scanner)
        //val nameParts = setFileName(scanner)

        val fullPath = repoPath.resolve(nameParts.first).resolve(nameParts.second).resolve(nameParts.third)
        fullPath.createDirectories()

        val newFileName = "${nameParts.first}-${nameParts.second}-${nameParts.third}"

        var file : File? = null
        try{
            file = File(filePath)
            file.copyTo(File(fullPath.resolve(newFileName).toString()), false)
            generateFileConfig(nameParts)

        }catch(e : Exception){
            println(e.message)
        }finally {
            if(file != null) {
                println("Uploading ${file.name} to ${fullPath.toString()} ...")
            }
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

    fun generateFileConfig(nameParts : Triple<String,String,String>) {
        val fileConfig : Gson = Gson()

        fileConfig.toJson(mapOf("group" to nameParts.first))
        fileConfig.toJson(mapOf("project" to nameParts.second))
        fileConfig.toJson(mapOf("version" to nameParts.third))

    }

    fun getFilePath(scanner : Scanner): String{
        var filePath : String = ""

        do {
            println("Enter path to file:")
            filePath = scanner.nextLine()
            if(filePath == ""){
                println("path can not be null")
            }
        }while(filePath == "")

        return filePath
    }

    fun setFileName(scanner: Scanner) : Triple<String,String, String> {
        var groupName : String? = null
        var projectName : String? = null
        var version : String? = null

        println("groupName: ")
        groupName = scanner.nextLine()
        println("projectName: ")
        projectName = scanner.nextLine()
        println("version: ")
        version = scanner.nextLine()

        return Triple(groupName, projectName, version)
    }
}