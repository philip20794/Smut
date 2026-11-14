package io.github.philip20794.smut

import org.json.JSONObject

internal sealed interface BookBridgeRequest {
    val requestId: String
    data class Load(override val requestId: String, val bookId: String) : BookBridgeRequest
    data class Save(
        override val requestId: String,
        val expectedRevision: Long,
        val document: JSONObject,
        val position: BookPosition
    ) : BookBridgeRequest
}

/** Separate bounded v1 messages; does not change the E-02 prototype protocol. */
internal object BookBridgeProtocol {
    private const val MAX_MESSAGE_LENGTH = 10_500_000
    private val requestId = Regex("[A-Za-z0-9_-]{1,40}")

    fun parseRequest(raw: String): BookBridgeRequest? {
        if (raw.length > MAX_MESSAGE_LENGTH) return null
        return runCatching {
            val json = JSONObject(raw)
            if (json.opt("version") !is Int || json.getInt("version") != 1 ||
                json.opt("requestId") !is String || !requestId.matches(json.getString("requestId"))) return null
            val id = json.getString("requestId")
            when (json.opt("type")) {
                "book-load" -> {
                    BookDocumentValidator.exactKeys(json, setOf("version", "type", "requestId", "bookId"))
                    BookBridgeRequest.Load(id, BookDocumentValidator.safeId(json.opt("bookId")))
                }
                "book-save" -> {
                    BookDocumentValidator.exactKeys(
                        json, setOf("version", "type", "requestId", "expectedRevision", "document", "position")
                    )
                    val revisionRaw = json.opt("expectedRevision")
                    require(revisionRaw is Number && revisionRaw.toDouble().isFinite() &&
                        revisionRaw.toDouble() % 1.0 == 0.0 &&
                        revisionRaw.toLong() in 0..9_007_199_254_740_991L)
                    val book = json.optJSONObject("document") ?: return null
                    BookDocumentValidator.inspect(book)
                    val position = BookPosition.parse(json.optJSONObject("position") ?: return null)
                    BookDocumentValidator.validatePosition(book, position)
                    BookBridgeRequest.Save(id, revisionRaw.toLong(), book, position)
                }
                else -> null
            }
        }.getOrNull()
    }

    fun loadResult(id: String, result: BookLoad): String {
        require(requestId.matches(id))
        val response = JSONObject().put("version", 1).put("type", "book-load-result").put("requestId", id)
        return when (result) {
            is BookLoad.Found -> response.put("state", "ok").put("revision", result.revision)
                .put("document", result.document).put("position", result.position.toJson())
                .put("recovered", result.recovered).toString()
            BookLoad.Missing -> response.put("state", "missing").toString()
            BookLoad.Corrupt -> response.put("state", "corrupt").toString()
            BookLoad.Error -> response.put("state", "error").toString()
        }
    }
    fun saveResult(id: String, result: BookSave): String {
        require(requestId.matches(id))
        val response = JSONObject().put("version", 1).put("type", "book-save-result").put("requestId", id)
        return when (result) {
            is BookSave.Saved -> response.put("state", "saved").put("revision", result.revision).toString()
            is BookSave.Conflict -> response.put("state", "conflict").put("revision", result.revision).toString()
            BookSave.Error -> response.put("state", "error").toString()
        }
    }
}
