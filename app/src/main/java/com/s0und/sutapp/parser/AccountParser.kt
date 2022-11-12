package com.s0und.sutapp.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.EOFException
import java.io.IOException

private const val userAgent         = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.104 (Edition Yx GX)"

private const val noLoginReferrer   = "https://lk.sut.ru/cabinet?login=no"
private const val referrer          = "https://lk.sut.ru/cabinet/"

private const val loginUrl          = "https://lk.sut.ru/cabinet/lib/autentificationok.php"
private const val profileUrl        = "https://lk.sut.ru/cabinet/project/cabinet/forms/profil.php"
private const val timetableUrl      = "https://lk.sut.ru/cabinet/project/cabinet/forms/raspisanie.php"
private const val messagesUrl       = "https://lk.sut.ru/project/cabinet/forms/message.php"
private const val filesUrl          = "https://lk.sut.ru/project/cabinet/forms/files_group_pr.php"
private const val wifiUrl           = "https://lk.sut.ru/project/cabinet/forms/wifi.php"

data class Message(
    val date: String,
    val header: String,
    val files: Map<String, String>,
    val sender: String,
    val content: String,
)

data class AccountData(
    val accountInfo: Map<String, String>,
    val messages: List<Message>,
    val groupFiles: List<Message>,
    val wifiCredentials: Pair<String, String>
)

/**
 * @return [Boolean]
 * if provided credentials are valid
 * @throws IOException
 * when there is some problem with internet connection or site
 */
suspend fun checkCredentials(username: String, password: String): Result<Boolean> {
    return try {
        val cookies = Jsoup.connect(noLoginReferrer).execute().cookies()

        val page = Jsoup.connect(loginUrl)
            .header("user-agent", userAgent)
            .data("users", username)
            .data("parole", password)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()

        Result.success(page.body() == "1")
    } catch (e: IOException) {
        Result.failure(e)
    }
}

/**
 * @return [AccountData] for provided credentials
 * @throws IOException
 * when there is some problem with internet connection or site
 */
suspend fun getAccountData(username: String, password: String): Result<AccountData> {
    return try {
        val cookies = getLoginCookies(username, password)

        val accountInfo = getAccountInfo(cookies)
//        val messageList = parseMessages(cookies)
//        val groupFileList = parseGroupFiles(cookies)
//        val wifiDataPair = parseWifiPage(cookies)
//
//        Result.success(AccountData(accountInfo, messageList, groupFileList, wifiDataPair))
        Result.success(AccountData(accountInfo, emptyList(), emptyList(), Pair("", "")))
    } catch (e: IOException) { Result.failure(e) }
}

/**
 * @return [Result.success] ([Unit])
 * when successfully checked in, or [Result.failure] ([Throwable])
 * @throws Throwable `NO_PAIR_BUTTON_AVAILABLE`
 * when there is no pairs that you can click on (to refresh or whatever)
 * @throws Throwable `CURRENT_PAIR_BUTTON_UNAVAILABLE`
 * when there is currently a pair going on and the teacher haven't started it yet
 * @throws IOException
 * when there is some problem with internet connection or site
 */
suspend fun checkInCurrentPair(username: String, password: String): Result<Unit> {
    try {
        val cookies = getLoginCookies(username, password)

        val page = Jsoup.connect(timetableUrl)
                .header("user-agent", userAgent)
                .cookies(cookies)
                .get()
                .body()

        val checkInButtons = page.select("[id^=knop]")
        val week = page.select("h3")[0].text().substring(30, 32)

        for (i in checkInButtons) {
            val id = i.attr("id").replace("knop", "")
            val buttonMessage = i.child(0)

            if (buttonMessage.text().lowercase() == "начать занятие")
                return checkInPair(id, week, cookies)
            else
            if (buttonMessage.attr("style") == "color: gray;")
                return Result.failure(Throwable("CURRENT_PAIR_BUTTON_UNAVAILABLE"))
        }
        return Result.failure(Throwable("NO_PAIR_BUTTON_AVAILABLE"))
    } catch (e: IOException) { return Result.failure(e) }
}

/**
 * @return logged in cookies as [Map]
 */
private fun getLoginCookies(username: String, password: String): Map<String, String> {
    val cookies = Jsoup.connect(noLoginReferrer).execute().cookies()

    Jsoup.connect(loginUrl)
        .header("user-agent", userAgent)
        .data("users", username)
        .data("parole", password)
        .cookies(cookies)
        .method(Connection.Method.POST)
        .execute()

    Jsoup.connect(referrer)
        .header("user-agent", userAgent)
        .cookies(cookies)
        .get()
    /*
    this loads home page of personal account.
    it adds "LINK_URL" property to site's php code,
    or else parsing messages or files will throw internal error
     */

    return cookies
}

private fun checkInPair(id: String, week: String, cookies: Map<String, String>): Result<Unit> {
    val response = try {
        Jsoup.connect(timetableUrl)
            .header("user-agent", userAgent)
            .data("open", "1")
            .data("rasp", id)
            .data("week", week)
            .cookies(cookies)
            .method(Connection.Method.POST)
            .execute()
            .body()
    } catch (e: EOFException) { return Result.failure(e) }

    val json = Json.parseToJsonElement(response)
    val state = json.jsonObject.getOrDefault("data", "").toString()

    return if (state != "\"\"")
        Result.success(Unit)
    else
        Result.failure(Throwable("CURRENT_PAIR_BUTTON_UNAVAILABLE"))
}

/**
 * @return [List]<[Message]>
 */
private fun parseMessages(cookies: Map<String, String>): List<Message> {
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

/**
 * @return [List]<[Message]>
 */
private fun parseGroupFiles(cookies: Map<String, String>): List<Message> {
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

/**
 * @return[Pair] (Name, Password)
 */
private fun parseWifiPage(cookies: Map<String, String>): Pair<String, String> {
    val page = Jsoup.connect(wifiUrl)
        .header("user-agent", userAgent)
        .cookies(cookies)
        .get().body()

    val wifiLogin = page.select("p")[4].child(0).text()
    val wifiPassword = page.select("p")[3].child(0).text()

    return Pair(wifiLogin, wifiPassword)
}

private fun getAccountInfo(cookies: Map<String, String>): Map<String, String> {
    val page = Jsoup.connect(profileUrl)
        .header("user-agent", userAgent)
        .cookies(cookies)
        .get().body()

    val tableOne = page.select(".profil")[0]
    val name = tableOne.select("tr")[1].child(1).text()
    val tableTwo = page.select(".profil")[1]
    val groupName = tableTwo.select("tr")[6].child(1).text()

    return mapOf(Pair("name", name), Pair("group", groupName))
}
