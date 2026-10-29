package rs.raf.sk

import java.util.Scanner
import RepoUploader
import java.util.ServiceLoader

fun main() {
    val scanner = Scanner(System.`in`)

    val filePath = getFilePath(scanner)
    val nameParts = setFileName(scanner)
    val dependencies = arrayOf("ls-ideademos-1.0.0","ls-proj1-1.2.0")

    val serviceLoader = ServiceLoader.load(RepoUploader::class.java)
    val exporterServices = mutableMapOf<String,RepoUploader> ()

    serviceLoader.forEach{
            service -> exporterServices.put(service.type.toString(), service)
    }

    exporterServices.get(RepoType.LOCAL.toString())!!.upload(filePath,nameParts, dependencies)
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