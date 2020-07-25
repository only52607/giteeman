package com.ooooonly.giteeman

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

private fun String.trimSlash() = replace(Regex("^/"), "").replace(Regex("/$"), "")
private fun String.rightSlash() = trimSlash() + "/"
private fun String.leftSlash() = "/" + trimSlash()
private fun String.getFileName() = split("/").last()
private fun URLConnection.applyAgent() = apply {
    setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36"
    )
}
private fun URLConnection.alsoConnect() = apply {
    connect()
}
private fun InputStream.readAllBytesAndClose():ByteArray{
    val bytes = readAllBytes()
    close()
    return bytes
}
private val ByteArray.stringValue:String
    get() = String(this)


@ExperimentalStdlibApi
class GiteeFile(
    val owner: String,
    val repository: String,
    val path: String,
    val branch: String = "master",
    private val fileAssert: Boolean? = null
) {
    private val rawPage: String by lazy {
        url.openConnection().applyAgent().alsoConnect().getInputStream().readAllBytesAndClose().stringValue//.also {println(it)}
    }
    private val childFiles: List<GiteeFile> by lazy {
        if (isFile) return@lazy emptyList<GiteeFile>()
        buildList {
            Regex("""data-path='(.*?)'\s*data-type='(.*?)'>[\s\S]*?href=".*?" title=".*?">.*?</a>""").findAll(rawPage)
                .all {
                    add(
                        GiteeFile(
                            owner,
                            repository,
                            it.groupValues[1],
                            branch,
                            it.groupValues[2] == "file"
                        )
                    )
                }
        }
    }

    val absolutePath by lazy {
        "https://gitee.com/${owner.trimSlash()}/${repository.trimSlash()}/tree/${branch.trimSlash()}/${path.trimSlash()}"
    }
    val dataPath by lazy {
        "https://gitee.com/${owner.trimSlash()}/${repository.trimSlash()}/raw/${branch.trimSlash()}/${path.trimSlash()}".takeIf { isFile }
    }
    val url: URL by lazy {
        URL(absolutePath)
    }
    val dataUrl: URL? by lazy {
        dataPath?.let {
            URL(dataPath)
        }
    }
    val fileName by lazy {
        path.getFileName()
    }
    val isFile: Boolean by lazy {
        fileAssert ?: rawPage.contains("redirected")
    }
    val isDictionary: Boolean by lazy {
        !isFile
    }
    val dataInputStream: InputStream? by lazy {
        dataUrl?.openConnection()?.applyAgent()?.alsoConnect()?.getInputStream()
    }
    val dataString:String? by lazy {
        dataInputStream?.readAllBytesAndClose()?.stringValue
    }
    val data:ByteArray? by lazy {
        dataInputStream?.readAllBytesAndClose()
    }
    val isRoot: Boolean by lazy {
        !path.trimSlash().contains("/")
    }
    val parent: GiteeFile? by lazy{
        if(isRoot) return@lazy null
        GiteeFile(owner,repository, path.trimSlash().replace(Regex("/.*?$"), ""), branch, false)
    }
    fun listFiles() = childFiles
    fun forEachChildFiles(block: (GiteeFile) -> Unit) = childFiles.forEach(block)
    fun saveToFile(fileAbsolutePath:String):Boolean = data?.let {
            FileOutputStream(fileAbsolutePath,false).apply {
                write(it)
            }.apply { close() }
            true
        }?:false
}
