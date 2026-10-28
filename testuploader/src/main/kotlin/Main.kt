package rs.raf.sk

import java.util.Scanner
import RepoUploader

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val ru = RepoUploader()
    val scanner = Scanner(System.`in`)

    val filePath = getFilePath(scanner)
    val nameParts = setFileName(scanner)

    ru.upload(filePath, nameParts)
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