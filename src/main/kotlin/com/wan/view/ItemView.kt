package com.wan.view

import javafx.scene.control.CheckBox
import javafx.scene.control.Hyperlink
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import tornadofx.*
import java.io.File
import java.net.URL

/**
 *
 * @author StarsOne
 * @date Create in  2020/1/2 0002 21:59
 * @description
 *
 */
class ItemView : View("My View") {
    var checkBox by singleAssign<CheckBox>()
    var fileNameTv by singleAssign<Text>()
    var fileSizeTv by singleAssign<Text>()
    var downloadHyperLink by singleAssign<Hyperlink>()
    var timeTv by singleAssign<Text>()
    var flagTv by singleAssign<Text>()
    var flag = false //下载完成标志

    override val root = hbox {
        form {
            fieldset {

                field {
                    setOnMouseClicked {
                        checkBox.isSelected = !checkBox.isSelected
                    }
                    checkBox = checkbox {

                    }
                    flagTv = text()
                    fileNameTv = text(""){
                        style {
                            fontWeight = FontWeight.BOLD
                        }
                    }
                    fileSizeTv = text(""){
                        style {
                            fill = c("orange")
                        }
                    }
                    timeTv = text(){
                        style {
                            fill = c("#c792ea")
                        }
                    }
                    downloadHyperLink = hyperlink {
                        prefWidth = 200.0
                    }

                }
            }
        }
    }

    fun downloadFile(dirPath: String) {
        if (dirPath.isBlank()) {
            //下载文件到默认下载目录
            //创建默认的下载目录
            val dirFile = File("蓝奏云下载")
            if (!dirFile.exists()) {
                dirFile.mkdirs()
            }
            runAsync {
                downloadFile(downloadHyperLink.text, File(dirFile, fileNameTv.text))

                runLater {
                    flagTv.text = "已下载"
                    flagTv.fill = c("green")
                    flag = true
                }
            }
        } else {
            //下载文件到用户选择的目录
            runAsync {
                downloadFile(downloadHyperLink.text, File(dirPath, fileNameTv.text))
                runLater {
                    flagTv.text = "已下载"
                    flagTv.fill = c("green")
                    flag = true
                }
            }
        }
    }

    /**
     * 下载文件到本地
     * @param url 网址
     * @param file 文件
     */
    private fun downloadFile(url: String, file: File) {
        if (!file.exists()) {
            val conn = URL(url).openConnection()
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)")
            val bytes = conn.getInputStream().readBytes()
            file.writeBytes(bytes)
        }
    }

    fun updateUi(itemData: ItemData) {
        flagTv.text = "未下载"
        downloadHyperLink.autosize()
        downloadHyperLink.text = itemData.downloadLink
        fileNameTv.text = itemData.fileName
        fileSizeTv.text = itemData.fileSize
        timeTv.text = itemData.time
    }

    fun select(flag: Boolean) {
        checkBox.isSelected = flag
    }
}
