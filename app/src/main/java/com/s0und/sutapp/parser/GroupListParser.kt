package com.s0und.sutapp.parser

import kotlinx.coroutines.coroutineScope
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException

data class UniGroup(
    val name: String,
    val ID: String,
)

suspend fun bgetGroups(callback: (Result<List<UniGroup>>) -> Unit) {
    coroutineScope {
        val request = requestFulltimeGroups()

        if (request.isSuccess) {
            val page = request.getOrNull()!!
            val groupList = parseGroups(page)

            callback.invoke(Result.success(groupList))
        } else {
            callback.invoke(Result.failure(request.exceptionOrNull()!!))
        } } }

fun parseGroups(page: Element): List<UniGroup> {
    val groups = mutableListOf<UniGroup>()
    val listGroups = page.select(".vt256")

    for (i in 0 until listGroups.size) {
        val groupID = listGroups[i].attr("data-i")
        val groupName = listGroups[i].text()
        val group = UniGroup(groupName, groupID)
        groups.add(group)
    }
    return groups
}

fun requestFulltimeGroups(): Result<Element> {
    val url = "https://www.sut.ru/studentu/raspisanie/raspisanie-zanyatiy-studentov-ochnoy-i-vecherney-form-obucheniya"
    return try {
        val page = Jsoup.connect(url)
            .header(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.5060.134 Safari/537.36 OPR/89.0.4447.104 (Edition Yx GX)"
            )
            .get().body()
        Result.success(page)
    } catch (e: IOException) {
        Result.failure(e)
    }
}