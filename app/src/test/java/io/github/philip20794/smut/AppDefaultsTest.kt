package io.github.philip20794.smut

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDefaultsTest {
    @Test
    fun a5PaperUsesMillimetresAndPortraitOrientation() {
        assertEquals(148, AppDefaults.PAGE_WIDTH_MM)
        assertEquals(210, AppDefaults.PAGE_HEIGHT_MM)
        assertTrue(AppDefaults.PAGE_HEIGHT_MM > AppDefaults.PAGE_WIDTH_MM)
    }
}
