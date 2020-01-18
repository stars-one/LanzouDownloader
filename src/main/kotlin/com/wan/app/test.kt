package com.wan.app

import com.wan.view.MainController
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @author StarsOne
 * @date Create in  2019/12/31 0031 23:00
 * @description
 *
 */
fun main(args: Array<String>) {

    /*val controller = MainController()
    val itemDatas = controller.download("https://www.lanzous.com/b607378", "ggdw")
    val dir = File("R:\\神话终将来临的放学后战争epub合集")

    val dirFile = File("下载")
    if (!dirFile.exists()) {
        dirFile.mkdirs()
    }
    for (itemData in itemDatas) {
        val file = File(dirFile, itemData.fileName)
        downloadFile(itemData.url, file)
        println(itemData.fileName + "已下载")
    }*/
//    val url = "https://vip.d0.baidupan.com/file/?BGIHOQg5Dj9UXQszU2ZSPlBvADhR6QOkA5oE6lekB5tVsgHtD9oEtQPlU4MKuFXJUK9QsQTjUecA4FvHVo4G4QSQB+4IsA73VKQLuFOXUt1Q7ACNUZYD5QO+BItXKwfgVcIB8w+3BN4D01PiCvFV01AkUDAEK1EmAGVbe1ZsBm4EaAc1CAoOM1RmC2BTMFJiUDMAMVE4AzQDMwQkV20HdVVoAWQPYgRgA2NTNApnVWJQLFAlBCtRbgA1W21WOwY+BCsHYAhnDnVUMAtrUyhSZVBpADxRNwNjAzcEZFc8BzZVMQFnDzAEZwNhUzAKbFVkUG9QNgRsUWQANltqVjIGNwQ8B2AIZg4+VDALbFNiUn1QbwB1UXsDYwMiBHdXeAdjVScBPw82BG0DbVMyCmpVZlA7UGMEfVEnAG5bMFZvBmEEOQdhCGAObVQ7C29TM1JrUDsANVE8Ay8DIgR3V3sHO1VkAXgPdAQ2AzlTcgpjVWVQP1BiBGJRYQAzW2hWOQY+BDQHdgggDipUdQtgUzZSZlA/ADRRPwM5AzMEM1czBzNVcwEjDzsEIANoUzQKb1VjUCRQZARjUWAAKVtvVjoGNQQqB2EIZg5v"
    val mainController = MainController()
    val download = mainController.download("https://www.lanzous.com/b0cpr90ti")
    for (itemData in download) {
        println(itemData.toString())
    }


}


/**
 * 下载文件到本地
 * @param url 网址
 * @param file 文件
 */
private fun downloadFile(url: String, file: File) {
    HttpURLConnection.setFollowRedirects(false)
    val conn = URL(url).openConnection()
    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)")
    val url = conn.getHeaderField("Location")
    println(url)
    val bytes = conn.getInputStream().readBytes()
    file.writeBytes(bytes)
}