package com.s0und.sutapp.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jsoup.Connection
import org.jsoup.Jsoup

private const val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.104 (Edition Yx GX)"

data class Message(
    val date: String,
    val header: String,
    val files: Map<String, String>,
    val sender: String,
    val content: String,
)

suspend fun getAccountData(username: String, password: String) {
    val cookies = getLoginCookies(username, password)

    val wifiDataList = parseWifiPage(cookies)
    val messageList = parseMessages(cookies)
    val groupFileList = parseGroupFiles(cookies)
}

private fun getLoginCookies(username: String, password: String): Map<String, String> {

    val referrer = "https://lk.sut.ru/cabinet/"
    val loginUrl = "https://lk.sut.ru/cabinet/lib/autentificationok.php"

    val retrievedSiteCookies = Jsoup.connect("$referrer?login=no").execute().cookies()

    Jsoup.connect(loginUrl)
        .header("user-agent", userAgent)
        .data("users", username)
        .data("parole", password)
        .cookies(retrievedSiteCookies)
        .method(Connection.Method.POST)
        .execute()

    Jsoup.connect(referrer)
        .header("user-agent", userAgent)
        .cookies(retrievedSiteCookies)
        .get()
    /*
    Just loading home page of personal account.
    This adds "LINK_URL" property to site's php code,
    or else parsing messages or files will throw internal error
     */

    return retrievedSiteCookies
}

private fun parseMessages(cookies: Map<String, String>): List<Message> {

    val messagesUrl = "https://lk.sut.ru/project/cabinet/forms/message.php"

    val page = Jsoup.connect(messagesUrl)
        .header("user-agent", userAgent)
        .cookies(cookies)
        .get()
        .body()

    val table = page.select(".simple-little-table").select("tbody").first()!!
    val trs = table.select("tr[id]").not("[id*='history']")

    val messageList = mutableListOf<Message>()

    var date = ""
    var header = ""
    var sender = ""
    var content = ""
    val files = mutableMapOf<String, String>()

    for (i in trs.indices) {
        val tr = trs[i].select("td")

        if (i % 2 == 0) {

            for (j in tr.indices) {
                val td = tr.select("td")[j]

                when (j) {
                    0 -> date = td.text()
                    1 -> header = td.text()
                    2 -> {
                        for (k in td.allElements.select("a")) {
                            files[k.text()] = k.attr("href")
                        }
                    }
                    3 -> sender = td.text()
                }

            }
        } else {

            val td = tr.select("td")[1].select("td")[2]
            val id = td.select("span").attr("id").substring(11)

            expandMessage(id, cookies) {
                content = it
            }

            val msg = Message(date, header, files.toMap(), sender, content)
            messageList.add(msg)
            files.clear()
        }
    }
    return messageList
}

private fun expandMessage(ID: String, cookies: Map<String, String>, callback: (String) -> Unit) {

    val messagesUrl = "https://lk.sut.ru/cabinet/project/cabinet/forms/sendto2.php"
    val response = Jsoup.connect(messagesUrl)
        .cookies(cookies)
        .data("id", ID)
        .data("prosmotr", "")
        .method(Connection.Method.POST)
        .execute()
        .body()

    val json = Json.parseToJsonElement(response)
    val text = json.jsonObject.getOrDefault("annotation", "no message").toString()
    val readyText = Jsoup.parse(text).text()

    callback.invoke(readyText)
}

private fun parseWifiPage(cookies: Map<String, String>): List<String> {

    val wifiUrl = "https://lk.sut.ru/project/cabinet/forms/wifi.php"

    val page = Jsoup.connect(wifiUrl)
        .header("user-agent", userAgent)
        .cookies(cookies)
        .get().body()

    val wifiLogin = page.select("p")[4].child(0).text()
    val wifiPassword = page.select("p")[3].child(0).text()

    return listOf(wifiLogin, wifiPassword)
}

private fun parseGroupFiles(cookies: Map<String, String>): List<Message> {

    val filesUrl = "https://lk.sut.ru/project/cabinet/forms/files_group_pr.php"

    val page = Jsoup.connect(filesUrl)
        .header("user-agent", userAgent)
        .cookies(cookies)
        .get().body()

    val table = page.select(".simple-little-table").select("tbody")
    val trs = table.select("tr[id]").not("[id*=showtr]")

    val filesList = mutableListOf<Message>()

    var sender = ""
    var date = ""
    var header = ""
    var content = ""
    val files = mutableMapOf<String, String>()

    for (i in trs.indices) {
        val tr = trs[i].select("td")

        if (i % 2 == 0) {

            for (j in tr.indices) {
                val td = tr.select("td")[j]

                when (j) {
                    1 -> sender = td.text()
                    2 -> date = td.text()
                    3 -> header = td.text()
                    4 -> content = td.text()
                    5 -> {
                        for (k in td.allElements.select("a")) {
                            files[k.text()] = k.attr("href")
                        }
                    }
                }
            }
            val file = Message(date, header, files.toMap(), sender, content)
            filesList.add(file)
            files.clear()
        }
    }
    return filesList
}

