package local_repo

import FileManager
import java.io.File

class LocalFileManager : FileManager() {

    fun processAndCopyFile(downloadPath: String, fileInRepo: File) {
        val downloadDir = File(downloadPath)
        if(!downloadDir.exists()) downloadDir.mkdirs()

        val fileBaseName = fileInRepo.name.substringBeforeLast("-")
        val dirFiles = downloadDir.listFiles(){file -> file.isFile && file.name.startsWith(fileBaseName) }
        if(dirFiles.isNullOrEmpty()){
            fileInRepo.copyTo(File(downloadDir, fileInRepo.name), overwrite = false)
            println("Downloading ${fileInRepo.name} to $downloadPath ...")
            return
        }
        val fileInDir = dirFiles.first()

        val v1 = extractVersion(fileInRepo.name)
        val v2 = extractVersion(fileInDir.name)

        val comparedValue = compare(v1, v2)

        if(comparedValue > 0){
            try {
                fileInRepo.copyTo(File(downloadPath.plus("\\${fileInRepo.name}")), true)
                fileInDir.delete()
            }catch (e : Exception){
                println(e.message)
            }
            println("Updated to newer version: ${fileInRepo.name}")
        }else {
            println("Skipping ${fileInRepo.name}; an equal or newer version already exists.")
        }

    }
}