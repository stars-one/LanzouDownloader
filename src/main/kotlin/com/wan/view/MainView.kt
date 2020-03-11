package com.wan.view

import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput
import com.gargoylesoftware.htmlunit.html.HtmlTextInput
import com.wan.util.LanzouParse
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import tornadofx.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class MainView : View("蓝奏云批量下载v1.1 by stars-one") {
    var urlTf by singleAssign<TextField>()//网址
    var passTf by singleAssign<TextField>()//提取码
    var threadCountTf by singleAssign<TextField>()//线程数
    var pageTf by singleAssign<TextField>()//自动翻页数
    var toggle by singleAssign<ToggleGroup>()
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
                    text("地址是否为自定义分享地址")
                    toggle = togglegroup {
                        radiobutton("是") {
                            userData = 1
                        }
                        radiobutton("否") {
                            isSelected = true
                            userData = 0
                        }
                    }
                    hyperlink("什么是自定义分享地址？") {
                        action {
                            find(DescView::class).openWindow()
                        }
                    }
                }
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
                            val flag = toggle.selectedToggle.userData as Int
                            val threadCount = if (threadCountTf.text.isBlank()) 5 else threadCountTf.text.toInt()
                            val pageCount = if (pageTf.text.isBlank()) 5 else pageTf.text.toInt()
                            runAsync {
                                val data = controller.download(url, password, threadCount, pageCount, flag)
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
                    threadCountTf = textfield { promptText = "输入解析线程数(1-128)，默认为5,理论上线程数越多解析速度越快,但也不要设置过大" }
                }
                field {
                    pageTf = textfield { promptText = "输入翻页数（当文件列表不止一页，自动翻页），默认为5，翻5页后开始解析" }
                }
            }
            this += listView
        }
    }
}

class MainController {
    /**
     * 此方法是适合普通的那些蓝奏云列表分享地址
     * @param url 多文件蓝奏云地址，如 https://www.lanzous.com/b607378
     * @param password 提取码，默认为无
     * @return 下载地址 List<String>
     */
    fun download(url: String, password: String , threadCount: Int , pageCount: Int , flag: Int): ArrayList<ItemData> {
        return if (flag == 1) {
            //解析自定义分享链接的
            parseCustomShare(url, password, threadCount, pageCount)
        } else {
            parseNormalShare(url, password, threadCount, pageCount)
        }
    }

    /**
     * 解析自定义分享地址
     */
    private fun parseCustomShare(url: String, password: String , threadCount: Int, pageCount: Int): ArrayList<ItemData> {
        val webClient = WebClient(BrowserVersion.CHROME) //创建一个webclient
        //webclient设置
        webClientConfig(webClient)
        var page = webClient.getPage<HtmlPage>(url)
        //自动翻页
        for (i in 0 until pageCount) {
            if (page.getElementById("filemore") != null) {
                page = page.getElementById("filemore").click()
            } else {
                break
            }
        }
        //等待数据加载
        webClient.waitForBackgroundJavaScript(2000)
        val readyNodes = page.getElementsById("ready")
        println(readyNodes.size)
        val itemDatas = arrayListOf<ItemData>()
        for (readyNode in readyNodes) {
            val link = readyNode.getElementsByTagName("a")[0].getAttribute("href")
            val size = readyNode.getElementsByTagName("div")[3].textContent
            val fileName = readyNode.textContent.replace(size, "")
            itemDatas.add(ItemData(fileName, link, "", size, ""))
        }
        getAllDownloadLink(itemDatas, threadCount)
        return itemDatas
    }

    /**
     * 普通链接分享
     */
    private fun parseNormalShare(url: String, password: String , threadCount: Int, pageCount: Int ): ArrayList<ItemData> {
        val webClient = WebClient(BrowserVersion.CHROME) //创建一个webclient
        //webclient设置
        webClientConfig(webClient)

        var page = webClient.getPage<HtmlPage>(url)
        val readyNodes = if (password.isNotBlank()) {
            //有密码的情况
            val pwdInput = page.getElementByName<HtmlTextInput>("pwd")
            val button = page.getElementsById("sub")[0] as HtmlSubmitInput
            pwdInput.valueAttribute = password
            val finishPage = button.click<HtmlPage>()
            webClient.waitForBackgroundJavaScript(2000)

            //文件可能不止一页,为了防止被封IP，限定最大翻页数，由用户输入
            for (i in 0 until pageCount) {
                if (page.getElementById("filemore") != null) {
                    page = page.getElementById("filemore").click()
                } else {
                    break
                }
            }
            finishPage.getElementsById("ready")
        } else {
            //文件可能不止一页,为了防止被封IP，限定最大翻页数，由用户输入
            for (i in 0 until pageCount) {
                if (page.getElementById("filemore") != null) {
                    page = page.getElementById("filemore").click()
                } else {
                    break
                }
            }
            webClient.waitForBackgroundJavaScript(2000)
            page.getElementsById("ready")
        }
        //初始化列表（分享的蓝奏云地址中的所有文件）
        val itemDatas = arrayListOf<ItemData>()
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
        getAllDownloadLink(itemDatas, threadCount)
        return itemDatas
    }

    /**
     * 获得列表中每个单条蓝奏云地址的真实地址（普通）
     */
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
     * 获得真实下载地址，并存入itemData的downloadLink属性中（普通）
     * @param itemData 单个文件对象
     */
    fun getDownloadLink(itemData: ItemData) {
        val url = itemData.url
        itemData.downloadLink = LanzouParse().getDownloadLink(url)
    }

    /**
     * webclient设置
     */
    private fun webClientConfig(webClient: WebClient) {
        webClient.options.isJavaScriptEnabled = true // 启动JS
        webClient.options.isUseInsecureSSL = true//忽略ssl认证
        webClient.options.isCssEnabled = false//禁用Css，可避免自动二次请求CSS进行渲染
        webClient.options.isThrowExceptionOnScriptError = false//运行错误时，不抛出异常
        webClient.options.isThrowExceptionOnFailingStatusCode = false
        webClient.ajaxController = NicelyResynchronizingAjaxController()// 设置Ajax异步
    }
}

data class ItemData(var fileName: String, var url: String, var downloadLink: String, var fileSize: String, var time: String)