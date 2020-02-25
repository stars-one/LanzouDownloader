package com.wan.app

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.wan.util.HttpUtil
import com.wan.view.ItemData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

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
   /* val mainController = MainController()
    val download = mainController.download("https://www.lanzous.com/b0cpr90ti")
    for (itemData in download) {
        println(itemData.toString())
    }
*/


}


fun getDownloadLink(itemData: ItemData) {
    val url = itemData.url
    val webClient = WebClient(BrowserVersion.CHROME) //创建一个webclient
    //webclient设置
    webClient.options.isJavaScriptEnabled = true // 启动JS
    webClient.options.isUseInsecureSSL = true//忽略ssl认证
    webClient.options.isCssEnabled = false//禁用Css，可避免自动二次请求CSS进行渲染
    webClient.options.isThrowExceptionOnScriptError = false//运行错误时，不抛出异常
    webClient.options.isThrowExceptionOnFailingStatusCode = false
    webClient.ajaxController = NicelyResynchronizingAjaxController()// 设置Ajax异步

    val page = webClient.getPage<HtmlPage>(url)

    val srcText = page.getElementsByTagName("iframe")[0].getAttribute("src")
    val downloadHtmlUrl = "https://www.lanzous.com$srcText"

    val downloadPage = webClient.getPage<HtmlPage>(downloadHtmlUrl)
    webClient.waitForBackgroundJavaScript(1000)
    val address = downloadPage.getElementById("go").firstElementChild.getAttribute("href")

    HttpUtil.sendOkHttpRequest(address, object : Callback {
        override fun onFailure(p0: Call?, p1: IOException?) {
            println("error")
        }

        override fun onResponse(p0: Call?, response: Response?) {
            itemData.downloadLink = response?.request()?.url().toString()
            response?.close()
        }
    })

}