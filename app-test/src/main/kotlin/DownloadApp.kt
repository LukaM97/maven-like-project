package rs.raf.sk

import RepoManager
import java.io.File
import java.util.Scanner
import java.util.ServiceLoader

fun main() {
    val file = File("app-test/src/resources/components.txt")

    val serviceLoader = ServiceLoader.load(RepoManager::class.java)
    val exporterServices = mutableMapOf<String,RepoManager> ()

    serviceLoader.forEach{
            service -> exporterServices.put(service.type.toString(), service)
    }

    val lines = file.readLines()

    val scanner = Scanner(System.`in`)

    println("Enter the path where you want to download the library:")
    val downloadPath = scanner.nextLine()

    for (line in lines) {
        val folderNames = line.split(":")

        exporterServices.get(RepoType.DRIVE.toString())!!.download(folderNames, downloadPath)
    }
}