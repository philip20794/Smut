package io.github.philip20794.smut

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookBridgeProtocolTest {
    private fun book(): JSONObject = JSONObject(
        """{"schemaVersion":1,"id":"book1","title":"Äpfel","createdAt":"2026-11-14T10:00:00.000Z","updatedAt":"2026-11-14T10:00:00.000Z","layout":{"pageWidthMm":148,"pageHeightMm":210,"marginsMm":{"top":18,"bottom":18,"inside":20,"outside":16},"fontFamily":"serif","fontSizePt":11,"lineSpacing":1.3,"pageNumbers":true},"chapters":[{"id":"chap1","title":"Kapitel 1","blocks":[{"id":"block1","type":"paragraph","runs":[{"text":"Äpfel 👩‍🚀"}]}]}]}"""
    )
    private fun save(document: JSONObject, position: JSONObject = position()) = JSONObject()
        .put("version", 1).put("type", "book-save").put("requestId", "save1")
        .put("expectedRevision", 0).put("document", document).put("position", position).toString()
    private fun position() = JSONObject()
        .put("chapterId", "chap1").put("blockId", "block1").put("offset", 0).put("scrollY", 3)

    @Test fun acceptsStrictBookSaveAndLoad() {
        val request = BookBridgeProtocol.parseRequest(save(book())) as BookBridgeRequest.Save
        assertEquals(0, request.expectedRevision)
        assertEquals("book1", request.document.getString("id"))
        assertEquals("chap1", request.position.chapterId)
        assertEquals(BookBridgeRequest.Load("load", "book1"),
            BookBridgeProtocol.parseRequest("""{"version":1,"type":"book-load","requestId":"load","bookId":"book1"}"""))
        val response = JSONObject(BookBridgeProtocol.saveResult("save1", BookSave.Saved(1)))
        assertEquals("saved", response.getString("state"))
        assertEquals(1L, response.getLong("revision"))
    }

    @Test fun rejectsInvalidDocumentsAndPositions() {
        val invalidSchema = book().put("schemaVersion", 2)
        assertNull(BookBridgeProtocol.parseRequest(save(invalidSchema)))
        val extra = book().put("unexpected", true)
        assertNull(BookBridgeProtocol.parseRequest(save(extra)))
        val invalidPosition = position().put("offset", 999)
        assertNull(BookBridgeProtocol.parseRequest(save(book(), invalidPosition)))
        // Splitting a surrogate pair is illegal.
        val surrogatePosition = position().put("offset", "Äpfel ".length + 1)
        assertNull(BookBridgeProtocol.parseRequest(save(book(), surrogatePosition)))
        val valid = JSONObject(save(book())).put("extra", "not-allowed")
        assertNull(BookBridgeProtocol.parseRequest(valid.toString()))
        val traversal = """{"version":1,"type":"book-load","requestId":"a","bookId":"../private"}"""
        assertNull(BookBridgeProtocol.parseRequest(traversal))
        assertTrue(BookBridgeProtocol.parseRequest("x".repeat(10_500_001)) == null)
    }

    @Test fun checksumsAreNeverAcceptedAsUserSuppliedBookFields() {
        assertNull(BookBridgeProtocol.parseRequest(save(book().put("sha256", "forged"))))
        val found = BookLoad.Found(book(), 7, BookPosition("chap1", "block1", 1, 42), true)
        val load = JSONObject(BookBridgeProtocol.loadResult("load", found))
        assertEquals("ok", load.getString("state"))
        assertEquals(7L, load.getLong("revision"))
        assertTrue(load.getBoolean("recovered"))
    }
}
