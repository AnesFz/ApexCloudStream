package com.exampleprovider

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class ApexFlix : MainAPI() {
    override var name = "ApexFlix"
    override var mainUrl = "https://sflix.to"
    override val lang = "en"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document
        val items = doc.select("div.item, div.film-poster").map {
            SearchResult(
                title = it.select("img").attr("alt") ?: it.attr("title") ?: "Unknown",
                posterUrl = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") },
                href = fixUrl(it.select("a").attr("href"))
            )
        }
        return HomePageResponse(listOf(HomePageList("Trending", items)))
    }

    override suspend fun search(query: String): List<SearchResult> {
        val doc = app.get("$mainUrl/search.html?keyword=$query").document
        return doc.select("div.item, div.film-poster").map {
            SearchResult(
                title = it.select("img").attr("alt") ?: it.attr("title") ?: "Unknown",
                posterUrl = it.select("img").attr("data-src").ifEmpty { it.select("img").attr("src") },
                href = fixUrl(it.select("a").attr("href"))
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.select("h1").text()
        val poster = doc.select("img").attr("src")
        return MovieLoadResponse(title, url, this.name, Type.MOVIE, url, poster)
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val doc = app.get(data).document
        doc.select("a[data-src], iframe[src]").forEach {
            val link = it.attr("data-src").ifEmpty { it.attr("src") }
            if (link.isNotBlank()) loadExtractor(link, data, subtitleCallback, callback)
        }
    }
}

class ApexAnime : MainAPI() {
    override var name = "ApexAnime"
    override var mainUrl = "https://aniwatch.to"
    override val lang = "en"
    override val supportedTypes = setOf(TvType.Anime)
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get("$mainUrl/trending").document
        val items = doc.select("div.anime-card, div.item").map {
            SearchResult(
                title = it.select("h3, .title").text(),
                posterUrl = it.select("img").attr("src"),
                href = fixUrl(it.select("a").attr("href"))
            )
        }
        return HomePageResponse(listOf(HomePageList("Trending Anime", items)))
    }

    override suspend fun search(query: String): List<SearchResult> {
        val doc = app.get("$mainUrl/search?keyword=$query").document
        return doc.select("div.anime-card, div.item").map {
            SearchResult(
                title = it.select("h3, .title").text(),
                posterUrl = it.select("img").attr("src"),
                href = fixUrl(it.select("a").attr("href"))
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.select("h1").text()
        return TvSeriesLoadResponse(title, url, this.name, Type.ANIME, emptyList())
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        loadExtractor(data, data, subtitleCallback, callback)
    }
}
