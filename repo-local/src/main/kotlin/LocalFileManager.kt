package local_repo

import java.io.File

class LocalFileManager {

    fun processAndCopyFile(downloadPath: String, fileInRepo: File) {
        val downloadDir = File(downloadPath)
        if(!downloadDir.exists()) downloadDir.mkdirs()

        val baseFileName = fileInRepo.name.substringBeforeLast("-")
        val dirFiles = downloadDir.listFiles(){file -> file.isFile && file.name.startsWith(baseFileName) }
        if(dirFiles.isNullOrEmpty()){
            fileInRepo.copyTo(File(downloadDir, fileInRepo.name), overwrite = false)
            println("Downloading ${fileInRepo.name} to $downloadPath ...")
            return
        }
        val fileInDir = dirFiles.first()

        val v1 = extractVersion(fileInRepo)
        val v2 = extractVersion(fileInDir)

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

    private fun compare(firstVersion : List<Int>,secondVersion : List<Int>) : Int{
        var i = 0
        var q = 0
        while(i < firstVersion.size && i < secondVersion.size) {
            if(firstVersion[i] != secondVersion[i]) {
                q = firstVersion[i].compareTo(secondVersion[i])
                break
            }
            i++
        }
        return q
    }

    private fun extractVersion(file : File) : List<Int> {
        return file.name.split("-").last()
            .split(".")
            .dropLast(1)
            .map { it.toInt() }
    }
}