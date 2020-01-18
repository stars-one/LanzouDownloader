package com.wan.view

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput
import com.gargoylesoftware.htmlunit.html.HtmlTextInput
import com.wan.util.HttpUtil
import javafx.scene.control.TextField
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import tornadofx.*
import java.io.IOException
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class MainView : View("蓝奏云批量下载v1.0 by stars-one") {
    var urlTf by singleAssign<TextField>()//网址
    var passTf by singleAssign<TextField>()//提取码
    var threadCountTf by singleAssign<TextField>()//线程数
    var pageTf by singleAssign<TextField>()//自动翻页数
    val controller = MainController()
    val listView = ListView()
    override val root = vbox {
        prefWidth = 630.0
        menubar {
            menu("帮助") {
                item("关于") {
                    action {
                        find(AboutView::class).openModal()
                    }
                }
            }
        }
        form {
            fieldset {
                field {
                    urlTf = textfield {
                        promptText = "输入蓝奏云网址"
                        isFocusTraversable = false
                    }
                    passTf = textfield {
                        promptText = "提取码（若没有则不输入）"
                        isFocusTraversable = false
                    }
                    button("解析地址") {
                        action {
                            println("开始解析")
                            val url = urlTf.text
                            val password = passTf.text
                            listView.clearData()
                            listView.flagText.text = "解析中，请耐心等待.."
                            runAsync {
                                val data = if (password.isBlank()) {
                                    controller.download(url)
                                } else {
                                    val threadCount = if (threadCountTf.text.isBlank() ) 5 else threadCountTf.text.toInt()
                                    val pageCount = if (pageTf.text.isBlank() ) 5 else pageTf.text.toInt()
                                    controller.download(url,password, threadCount, pageCount)
                                }
                                //需要等待，否则最后一条数据解析不到真实地址
                                while (true) {
                                    if (!data.last().downloadLink.isBlank()) {
                                        runLater {
                                            listView.setDataList(data)
                                        }
                                        break
                                    }
                                    Thread.sleep(400)
                                }
                            }
                        }
                    }
                }
                field {
                    threadCountTf = textfield { promptText="输入解析线程数(1-128)，默认为5,理论上线程数越多解析速度越快,但也不要设置过大" }
                }
                field {
                    pageTf = textfield { promptText="输入翻页数（当文件列表不止一页，自动翻页），默认为5，翻5页后开始解析" }
                }
            }
            this += listView
        }
    }
}

class MainController {
    /**
     * @param url 多文件蓝奏云地址，如 https://www.lanzous.com/b607378
     * @param password 提取码，默认为无
     * @return 下载地址 List<String>
     */
    fun download(url: String, password: String = "",threadCount: Int=5,pageCount: Int=5): ArrayList<ItemData> {

        val webClient = WebClient(BrowserVersion.CHROME) //创建一个webclient
        //webclient设置
        webClient.options.isJavaScriptEnabled = true // 启动JS
        webClient.options.isUseInsecureSSL = true//忽略ssl认证
        webClient.options.isCssEnabled = false//禁用Css，可避免自动二次请求CSS进行渲染
        webClient.options.isThrowExceptionOnScriptError = false//运行错误时，不抛出异常
        webClient.options.isThrowExceptionOnFailingStatusCode = false
        webClient.ajaxController = NicelyResynchronizingAjaxController()// 设置Ajax异步

        var page = webClient.getPage<HtmlPage>(url)

        //初始化列表（分享的蓝奏云地址中的所有文件）
        val itemDatas = arrayListOf<ItemData>()

        //文件可能不止一页,为了防止被封IP，限定最大翻页数，由用户输入
        for (i in 0 until pageCount) {
            if (page.getElementById("filemore") != null) {
                page = page.getElementById("filemore").click()
            } else {
                break
            }
        }

        /*while (page.getElementById("filemore") != null) {
            page = page.getElementById("filemore").click()
            webClient.waitForBackgroundJavaScript(2000)
        }*/


        val readyNodes = if (password.isNotBlank()) {
            //有密码的情况
            val pwdInput = page.getElementByName<HtmlTextInput>("pwd")
            val button = page.getElementsById("sub")[0] as HtmlSubmitInput
            pwdInput.valueAttribute = password
            val finishPage = button.click<HtmlPage>()
            webClient.waitForBackgroundJavaScript(2000)
            finishPage.getElementsById("ready")
        } else {
            webClient.waitForBackgroundJavaScript(2000)
            page.getElementsById("ready")
        }
        //readyNode包含name,size,time
        for (readyNode in readyNodes) {
            val childNodes = readyNode.getElementsByTagName("div")
            val nameNode = childNodes[0].lastElementChild
            val sizeNode = childNodes[1]
            val timeNode = childNodes[2]

            val name = nameNode.textContent
            val link = nameNode.getAttribute("href")
            val size = sizeNode.textContent
            val time = timeNode.textContent
            itemDatas.add(ItemData(name, link, "", size, time))
        }
        //多线程解析
        getAllDownloadLink(itemDatas,threadCount)
        return itemDatas
    }

    private fun getAllDownloadLink(itemDatas: ArrayList<ItemData>, threadCount: Int = 5) {

        val countDownLatch = CountDownLatch(threadCount)
        val step = itemDatas.size / threadCount
        val yu = itemDatas.size % threadCount
        val firstList = itemDatas.take(step)
        val lastList = itemDatas.takeLast(step + yu)
        thread {
            for (itemData in firstList) {
                getDownloadLink(itemData)
            }
            countDownLatch.countDown()
        }
        thread {
            for (itemData in lastList) {
                getDownloadLink(itemData)
            }
            countDownLatch.countDown()
        }

        for (i in 1..threadCount - 2) {
            val list = itemDatas.subList(i * step, (i + 1) * step + 1)
            thread {
                for (itemData in list) {
                    getDownloadLink(itemData)
                }
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
    }

    /**
     * 获得真实下载地址，并存入itemData的downloadLink属性中
     * @param itemData 单个文件对象
     */
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

}

data class ItemData(var fileName: String, var url: String, var downloadLink: String, var fileSize: String, var time: String)