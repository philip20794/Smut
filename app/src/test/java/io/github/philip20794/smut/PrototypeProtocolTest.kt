package io.github.philip20794.smut

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrototypeProtocolTest {
    @Test
    fun loadAndSaveRequireExplicitVersionedShapes() {
        assertEquals(
            BridgeRequest.Load("load-1"),
            BridgeProtocol.parseRequest("""{"version":1,"type":"load","requestId":"load-1"}""")
        )
        val save = BridgeProtocol.parseRequest(
            """{"version":1,"type":"save","requestId":"save-1","document":{"main":[{"value":"Äpfel und Grüße"}]}}"""
        ) as? BridgeRequest.Save
        assertNotNull(save)
        assertEquals("Äpfel und Grüße", save!!.document.getJSONArray("main").getJSONObject(0).getString("value"))
        assertTrue(JSONObject(BridgeProtocol.saveResult("save-1", true)).getBoolean("saved"))
        assertEquals(
            "Äpfel und Grüße",
            JSONObject(BridgeProtocol.loadResult("load-2", save.document))
                .getJSONObject("document").getJSONArray("main").getJSONObject(0).getString("value")
        )
    }

    @Test
    fun rejectsUntrustedOrOversizedDocuments() {
        for (payload in listOf(
            """{"version":1,"type":"save","requestId":"save-1","document":{"main":"bad"}}""",
            """{"version":1,"type":"save","requestId":"save-1","document":{"main":[{"type":"bad"}]}}""",
            """{"version":2,"type":"save","requestId":"save-1","document":{"main":[]}}""",
            """{"version":1,"type":"save","requestId":"bad id","document":{"main":[]}}""",
            """{"version":1,"type":"load","requestId":"load-1","extra":"x"}""",
            "x".repeat(750_001)
        )) assertNull(BridgeProtocol.parseRequest(payload))
        assertNull(BridgeProtocol.parseDocument("{}"))
    }
}
