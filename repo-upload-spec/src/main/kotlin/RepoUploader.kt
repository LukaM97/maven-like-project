import java.io.File

abstract class RepoUploader() {
    abstract val type : RepoType
    protected val allowedExtensions : Array<String> = arrayOf("rar","zip","json","jar")
    var rcm = RepoConfigManager(allowedExtensions)

    abstract fun initializeRepository()
    abstract fun upload(file: File, nameParts: Triple<String, String, String>, dependencies: Array<String>)

    fun upload(filePath: String, nameParts: Triple<String, String, String>, dependencies: Array<String>){
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

}