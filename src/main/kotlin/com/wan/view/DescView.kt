package com.wan.view

import javafx.geometry.Pos
import tornadofx.*

/**
 *  说明自定义分享地址
 * @author StarsOne
 * @date Create in  2020/2/25 0025 13:59
 * @description
 *
 */
class DescView : View("My View") {
    override val root = vbox {
        setPrefSize(600.0,650.0)
        imageview(url = "img/2.png") {
            alignment = Pos.TOP_CENTER
            isPreserveRatio = true
        }
        text("上图的图片就不是属于自定义分享外链")
        text("如果打开的蓝奏云的分享页面与上面不同，那就是属于自定义分享，由于自定义分享外链需要开通蓝奏云会员才能创建，所以，暂时不支持有提取码的自定义外链（其实是没有作为测试的链接）")
    }
}
