package io.github.philip20794.smut

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class BookInfo(val id: String, val title: String, val createdAt: String, val updatedAt: String)
internal data class BookPosition(val chapterId: String, val blockId: String?, val offset: Int, val scrollY: Int) {
    fun toJson(): JSONObject = JSONObject()
        .put("chapterId", chapterId).put("blockId", blockId ?: JSONObject.NULL)
        .put("offset", offset).put("scrollY", scrollY)

    companion object {
        fun parse(value: JSONObject): BookPosition {
            BookDocumentValidator.exactKeys(value, setOf("chapterId", "blockId", "offset", "scrollY"))
            val chapter = BookDocumentValidator.safeId(value.opt("chapterId"))
            val block = if (value.isNull("blockId")) null else BookDocumentValidator.safeId(value.opt("blockId"))
            val offset = BookDocumentValidator.integer(value.opt("offset"), 0, 1_000_000)
            val scroll = BookDocumentValidator.integer(value.opt("scrollY"), 0, 10_000_000)
            return BookPosition(chapter, block, offset, scroll)
        }
    }
}

/** Mirrors the strict D-01 schema at the untrusted JS->Android disk boundary. */
internal object BookDocumentValidator {
    const val MAX_BOOK_BYTES = 8_000_000
    private val idRegex = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,63}")
    private val timeFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
        .withZone(ZoneOffset.UTC)

    internal fun exactKeys(json: JSONObject, allowed: Set<String>) {
        require(json.keys().asSequence().toSet() == allowed) { "Ungültige JSON-Felder" }
    }
    private fun keys(json: JSONObject, required: Set<String>, optional: Set<String> = emptySet()) {
        val actual = json.keys().asSequence().toSet()
        require(actual.containsAll(required) && (actual - required - optional).isEmpty()) { "Ungültige Buchfelder" }
    }
    internal fun safeId(value: Any?): String {
        require(value is String && idRegex.matches(value)) { "Ungültige ID" }
        return value
    }
    private fun text(value: Any?, max: Int, empty: Boolean = false): String {
        require(value is String && value.length <= max && (empty || value.isNotEmpty()) && !value.contains('\u0000')) { "Ungültiger Text" }
        return value
    }
    internal fun integer(value: Any?, min: Int, max: Int): Int {
        require(value is Number && value.toDouble().isFinite() && value.toDouble() % 1.0 == 0.0 &&
            value.toLong() in min.toLong()..max.toLong()) { "Ungültige Ganzzahl" }
        return value.toInt()
    }
    private fun number(value: Any?, min: Double, max: Double) {
        require(value is Number && value.toDouble().isFinite() && value.toDouble() in min..max) { "Ungültige Zahl" }
    }
    private fun obj(json: JSONObject, key: String): JSONObject =
        json.optJSONObject(key) ?: throw IllegalArgumentException("Objekt fehlt: $key")
    private fun array(json: JSONObject, key: String, max: Int): JSONArray {
        val data = json.optJSONArray(key) ?: throw IllegalArgumentException("Liste fehlt: $key")
        require(data.length() <= max) { "Liste zu groß" }
        return data
    }
    private fun timestamp(value: Any?): String {
        val raw = text(value, 24)
        require(raw.length == 24 && runCatching { timeFormat.format(Instant.parse(raw)) == raw }.getOrDefault(false)) { "Ungültiger Zeitstempel" }
        return raw
    }
    private fun uniqueId(value: Any?, ids: MutableSet<String>) {
        require(ids.add(safeId(value))) { "Doppelte ID" }
    }
    private fun runs(block: JSONObject) {
        val runs = array(block, "runs", 20_000)
        for (i in 0 until runs.length()) {
            val run = runs.getJSONObject(i)
            keys(run, setOf("text"), setOf("marks"))
            val s = text(run.opt("text"), 1_000_000)
            require('\n' !in s && '\r' !in s) { "Zeilenwechsel muss strukturell sein" }
            if (run.has("marks")) {
                val marks = obj(run, "marks")
                val fields = marks.keys().asSequence().toSet()
                require(fields.isNotEmpty() && fields.subtract(setOf("bold", "italic", "fontFamily", "fontSizePt")).isEmpty())
                for (flag in listOf("bold", "italic")) {
                    if (marks.has(flag)) require(marks.opt(flag) == true) { "Ungültige Textmarkierung" }
                }
                if (marks.has("fontFamily")) text(marks.opt("fontFamily"), 80)
                if (marks.has("fontSizePt")) number(marks.opt("fontSizePt"), 6.0, 72.0)
            }
        }
    }

    fun inspect(book: JSONObject): BookInfo {
        require(book.toString().toByteArray(Charsets.UTF_8).size <= MAX_BOOK_BYTES) { "Buch zu groß" }
        keys(book, setOf("schemaVersion", "id", "title", "createdAt", "updatedAt", "layout", "chapters"), setOf("author"))
        require(integer(book.opt("schemaVersion"), 1, 1) == 1)
        val ids = mutableSetOf<String>()
        val bookId = safeId(book.opt("id"))
        uniqueId(bookId, ids)
        val title = text(book.opt("title"), 240)
        if (book.has("author")) text(book.opt("author"), 240)
        val created = timestamp(book.opt("createdAt"))
        val updated = timestamp(book.opt("updatedAt"))
        require(updated >= created) { "Änderungsdatum liegt vor Erstellungsdatum" }
        val layout = obj(book, "layout")
        exactKeys(layout, setOf("pageWidthMm", "pageHeightMm", "marginsMm", "fontFamily", "fontSizePt", "lineSpacing", "pageNumbers"))
        require(integer(layout.opt("pageWidthMm"), 148, 148) == 148 &&
            integer(layout.opt("pageHeightMm"), 210, 210) == 210)
        val margins = obj(layout, "marginsMm")
        exactKeys(margins, setOf("top", "bottom", "inside", "outside"))
        for (side in listOf("top", "bottom", "inside", "outside")) number(margins.opt(side), 0.0, 50.0)
        require(margins.getDouble("inside") + margins.getDouble("outside") < 130.0 &&
            margins.getDouble("top") + margins.getDouble("bottom") < 190.0) { "Satzfläche zu klein" }
        text(layout.opt("fontFamily"), 80)
        number(layout.opt("fontSizePt"), 6.0, 32.0)
        number(layout.opt("lineSpacing"), 1.0, 3.0)
        require(layout.opt("pageNumbers") is Boolean)
        val chapters = array(book, "chapters", 1_000)
        for (i in 0 until chapters.length()) {
            val chapter = chapters.getJSONObject(i)
            exactKeys(chapter, setOf("id", "title", "blocks"))
            uniqueId(chapter.opt("id"), ids)
            text(chapter.opt("title"), 240)
            val blocks = array(chapter, "blocks", 25_000)
            for (j in 0 until blocks.length()) {
                val block = blocks.getJSONObject(j)
                uniqueId(block.opt("id"), ids)
                when (block.opt("type")) {
                    "paragraph" -> {
                        exactKeys(block, setOf("id", "type", "runs"))
                        runs(block)
                    }
                    "heading" -> {
                        exactKeys(block, setOf("id", "type", "level", "runs"))
                        integer(block.opt("level"), 1, 2)
                        runs(block)
                        require(block.getJSONArray("runs").length() > 0)
                    }
                    "sceneBreak", "pageBreak" -> exactKeys(block, setOf("id", "type"))
                    else -> throw IllegalArgumentException("Unbekannter Blocktyp")
                }
            }
        }
        return BookInfo(bookId, title, created, updated)
    }

    /**
     * B-03: A valid, checksummed older snapshot may refer to a chapter or block
     * removed by a later edit. Repair only a structurally parsed location; a
     * broken envelope, checksum or document is never treated as a valid book.
     */
    fun restorePosition(book: JSONObject, saved: BookPosition): BookPosition {
        if (runCatching { validatePosition(book, saved) }.isSuccess) return saved
        val chapters = book.getJSONArray("chapters")
        require(chapters.length() > 0) { "Kein gültiger Positionsanker vorhanden" }
        val chapter = (0 until chapters.length())
            .map { chapters.getJSONObject(it) }
            .firstOrNull { it.getString("id") == saved.chapterId }
            ?: chapters.getJSONObject(0)
        val chapterId = chapter.getString("id")
        // An unknown chapter or non-text block must not inherit an unrelated scroll.
        if (chapterId != saved.chapterId || saved.blockId == null) {
            return BookPosition(chapterId, null, 0, 0)
        }
        val blocks = chapter.getJSONArray("blocks")
        val block = (0 until blocks.length()).map { blocks.getJSONObject(it) }
            .firstOrNull { it.getString("id") == saved.blockId }
        if (block == null || block.optString("type") !in setOf("paragraph", "heading")) {
            return BookPosition(chapterId, null, 0, 0)
        }
        val runs = block.getJSONArray("runs")
        val content = buildString {
            for (i in 0 until runs.length()) append(runs.getJSONObject(i).getString("text"))
        }
        var offset = saved.offset.coerceAtMost(content.length)
        if (offset in 1 until content.length &&
            Character.isHighSurrogate(content[offset - 1]) &&
            Character.isLowSurrogate(content[offset])) offset--
        return BookPosition(chapterId, saved.blockId, offset, saved.scrollY)
    }

    fun validatePosition(book: JSONObject, position: BookPosition) {
        val chapters = book.getJSONArray("chapters")
        val chapter = (0 until chapters.length()).map { chapters.getJSONObject(it) }
            .firstOrNull { it.optString("id") == position.chapterId }
            ?: throw IllegalArgumentException("Unbekanntes Kapitel")
        if (position.blockId == null) {
            require(position.offset == 0)
            return
        }
        val blocks = chapter.getJSONArray("blocks")
        val block = (0 until blocks.length()).map { blocks.getJSONObject(it) }
            .firstOrNull { it.optString("id") == position.blockId }
            ?: throw IllegalArgumentException("Unbekannter Cursorblock")
        require(block.optString("type") in setOf("paragraph", "heading")) { "Kein Textblock" }
        val runs = block.getJSONArray("runs")
        val content = buildString {
            for (i in 0 until runs.length()) append(runs.getJSONObject(i).getString("text"))
        }
        require(position.offset <= content.length) { "Cursor hinter Text" }
        // Never persist a cursor that slices a UTF-16 surrogate pair.
        if (position.offset in 1 until content.length) {
            require(!(Character.isHighSurrogate(content[position.offset - 1]) &&
                Character.isLowSurrogate(content[position.offset]))) { "Cursor teilt Unicode-Zeichen" }
        }
    }
}
