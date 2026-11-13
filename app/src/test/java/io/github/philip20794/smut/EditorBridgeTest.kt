package io.github.philip20794.smut

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class EditorBridgeTest {
    @Test
    fun validHandshakeIsAcceptedAndAcknowledged() {
        val input = """{"version":1,"type":"ready","requestId":"startup"}"""
        assertEquals("startup", BridgeProtocol.parseReady(input))
        val ack = JSONObject(BridgeProtocol.ack("startup"))
        assertEquals(1, ack.getInt("version"))
        assertEquals("ready-ack", ack.getString("type"))
        assertEquals("startup", ack.getString("requestId"))
    }

    @Test
    fun malformedOrUnexpectedMessagesAreRejected() {
        val invalid = listOf(
            "not-json",
            "[]",
            """{"version":2,"type":"ready","requestId":"x"}""",
            """{"version":1,"type":"save","requestId":"x"}""",
            """{"version":1,"type":"ready","requestId":"bad id"}""",
            """{"version":1,"type":"ready","requestId":"x","extra":1}""",
            "x".repeat(513)
        )
        invalid.forEach { assertNull(BridgeProtocol.parseReady(it)) }
    }

    @Test
    fun onlyApkEditorAssetsAreTrusted() {
        assertTrue(isTrustedEditorAssetUrl(EDITOR_URL))
        assertTrue(isTrustedEditorAssetUrl("https://appassets.androidplatform.net/assets/editor/main.js"))
        listOf(
            "http://appassets.androidplatform.net/assets/editor/index.html",
            "https://example.org/assets/editor/index.html",
            "https://appassets.androidplatform.net/assets/other/index.html",
            "https://appassets.androidplatform.net/assets/editor/../secret",
            "https://appassets.androidplatform.net/assets/editor/%2e%2e/secret",
            "https://appassets.androidplatform.net/assets/editor/index.html?x=1",
            "https://user@appassets.androidplatform.net/assets/editor/index.html"
        ).forEach { assertFalse(isTrustedEditorAssetUrl(it)) }
    }
}
