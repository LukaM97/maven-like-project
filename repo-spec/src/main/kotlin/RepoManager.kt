import rs.raf.drive.GoogleDriveAuth
import java.io.File
import java.util.Scanner

abstract class RepoManager : RepoUploader, RepoDownloader {
    abstract val type : RepoType
    protected val allowedExtensions : Array<String> = arrayOf("rar","zip","json","jar")
    val drive = GoogleDriveAuth.connect()
    var rcm = RepoConfigManager(allowedExtensions, drive)

    abstract fun initializeRepository()
    abstract fun upload(file: File, nameParts: List<String>, dependencies: Array<String>)
    abstract fun download(downloadPath: String, folderNames: List<String>)

    override fun upload(filePath: String, nameParts: List<String>, dependencies: Array<String>){
        val file : File = File(filePath)
        if(!file.exists()) {
            println("File does not exist at: $filePath")
            return
        }

        if(!allowedExtensions.contains(file.extension)) {
            println("Invalid file extension.")
            return
        }

        upload(file, nameParts, dependencies)
    }

    override fun download(folderNames : List<String>) {
        val scanner = Scanner(System.`in`)

        println("Enter the path where you want to download the library:")
        val downloadPath = scanner.nextLine()

        download(downloadPath, folderNames)
    }
}