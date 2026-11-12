package rs.raf.sk

import RepoManager
import java.io.File
import java.util.ServiceLoader

fun main() {
    val file = File("app-test/src/resources/components.txt")

    val serviceLoader = ServiceLoader.load(RepoManager::class.java)
    val exporterServices = mutableMapOf<String,RepoManager> ()

    serviceLoader.forEach{
            service -> exporterServices.put(service.type.toString(), service)
    }

    val lines = file.readLines()
    for (line in lines) {
        val folders = line.split(":")

        val folderNames = Triple(folders[0], folders[1], folders[2])

        exporterServices.get(RepoType.LOCAL.toString())!!.download(folderNames)
    }


}