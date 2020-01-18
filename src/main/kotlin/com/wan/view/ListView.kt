package com.wan.view

import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.text.Text
import tornadofx.*

/**
 *
 * @author StarsOne
 * @date Create in  2020/1/2 0002 22:16
 * @description
 *
 */
class ListView : View("My View") {
    var contentVbox = vbox {}
    var scrollpane by singleAssign<ScrollPane>()
    var dirInput by singleAssign<TextField>()
    var itemViews = arrayListOf<ItemView>()
    var flagText by singleAssign<Text>()

    override val root = vbox {
        spacing = 15.0
        scrollpane = scrollpane {
            prefHeight = 300.0
            this += contentVbox
        }
        hbox {
            spacing = 20.0
            checkbox("全选") {
                action {
                    if (this.isSelected) {
                        for (itemView in itemViews) {
                            itemView.select(true)
                        }
                    } else {
                        for (itemView in itemViews) {
                            itemView.select(false)
                        }
                    }
                }
            }
            button("下载已选") {
                action {
                    flagText.text = "下载中"
                    flagText.fill = c("black")
                    val list = itemViews.filter { itemView -> itemView.checkBox.isSelected }
                    for (itemView in list) {
                        itemView.downloadFile(dirInput.text)
                    }
                    runAsync {
                        while (true) {
                            if (list.filter { it.flag }.size == list.size) {
                                ui {
                                    flagText.text = "下载完毕"
                                    flagText.fill = c("green")
                                }
                                break
                            }
                            Thread.sleep(500)
                        }
                    }
                }
            }
            button("下载全部") {
                action {
                    flagText.text = "下载中"
                    flagText.fill = c("black")
                    for (itemView in itemViews) {
                        itemView.downloadFile(dirInput.text)
                    }
                    runAsync {
                        while (true) {
                            if (itemViews.filter { it.flag }.size == itemViews.size) {
                                ui {
                                    flagText.text = "下载完毕"
                                    flagText.fill = c("green")
                                }
                                break
                            }
                            Thread.sleep(500)
                        }
                    }
                }
            }
            flagText = text("") {
                alignment = Pos.CENTER_LEFT
            }
        }
        hbox {
            spacing = 10.0
            dirInput = textfield {
                prefWidth = 500.0
                promptText = "输入下载目录（默认下载地址在 当前jar包路径/蓝奏云下载文件夹中）"
            }
            button("选择目录") {
                action {
                    val file = chooseDirectory("选择目录")
                    if (file != null) {
                        dirInput.text = file.path
                    }
                }
            }
        }

    }


    fun setDataList(itemDatas: ArrayList<ItemData>) {
        for (itemData in itemDatas) {
            val itemView = ItemView()
            itemView.updateUi(itemData)
            itemViews.add(itemView)
            contentVbox.add(itemView)
        }
        flagText.text = "解析完毕"
    }

    fun clearData() {
        if (contentVbox.children.size > 0) {
            itemViews.clear()
            contentVbox.removeFromParent()
            contentVbox = vbox { }
            scrollpane.add(contentVbox)
        }
    }

}

