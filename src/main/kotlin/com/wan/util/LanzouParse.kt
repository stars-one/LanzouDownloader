package com.wan.util

import com.google.gson.Gson
import org.jsoup.Jsoup
import sun.net.www.protocol.http.HttpURLConnection.userAgent
import java.util.*




/**
 *  解析单条蓝奏云分享地址的真事链接
 * @author StarsOne
 * @date Create in  2020/3/10 0010 17:04
 */
class LanzouParse {

    fun getDownloadLink(url: String): String {
        //先获得iframe的src连接（包含电信下载，联通下载和普通下载的一个页面）
        val iframeUrl = getFrameUrl(url)
        //获得伪直链
        return getLink(iframeUrl)
    }

    private fun getLink(iframeUrl: String): String {
        val doc = Jsoup.connect(iframeUrl)
                .userAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB5; .NET CLR 2.0.50727; CIBA)")
                .get()
        val scriptCodeText = doc.body().getElementsByTag("script").toString()
        //正则匹配，获得sign（之后post请求需要此数据）
        val signLine = Regex("var sg = '([\\w]+?)';").find(scriptCodeText)?.value as String
        val sign = signLine.substring(signLine.indexOf("'") + 1, signLine.lastIndex - 1)

        val postUrl = "https://www.lanzous.com/ajaxm.php"
        //请求头参数
        val header = HashMap<String, String>()
        header["Accept-Language"] = "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"
        header["referer"] = "https://www.lanzous.com"
        //请求参数
        val params = LinkedHashMap<String, String>()
        params["action"] = "downprocess"
        params["sign"] = sign
        params["ves"] = "1"

        //获得伪链接（此处若没有请求头，会导致下一步访问获得伪链出现400错误）
        val result = Jsoup.connect(postUrl)
                .headers(header)
                .data(params)
                .post()
                .body()
                .text()

        //json转为实体类
        val lanzouData = Gson().fromJson(result, LanzouData::class.java)
        //拼接得到伪链
        val link = lanzouData.dom +"/file/"+ lanzouData.url

        //从请求头得到Location重定向地址(即真实下载地址）
        val reqHeads = Jsoup.connect(link)
                .headers(header)
                .ignoreContentType(true)
                .userAgent(userAgent)
                .followRedirects(false)
                .execute()
                .headers()
        return reqHeads["Location"] as String
    }


    private fun getFrameUrl(url: String): String {
        val doc = Jsoup.connect(url)
                .userAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; GTB5; .NET CLR 2.0.50727; CIBA)")
                .get()
        val result = doc.getElementsByTag("iframe")[0].attr("src")
        return "https://www.lanzous.com$result"
    }

}
data class LanzouData(
        val dom: String,
        val inf: Int,
        val url: String,
        val zt: Int
)