package io.github.philip20794.smut

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ChapterLibraryUiInstrumentedTest {
    @Test fun createRenameNavigateReorderAndConfirmDeleteChapter() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val title = "B02-UI-" + UUID.randomUUID().toString().take(8)
        try {
            ActivityScenario.launch(LibraryActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Neues Buch")), 12_000))
                device.findObject(By.text("Neues Buch")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buchtitel")), 5_000))
                device.findObject(By.clazz("android.widget.EditText")).text = title
                device.findObject(By.text("Anlegen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Neues Kapitel")), 12_000))

                device.findObject(By.text("Neues Kapitel")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapiteltitel")), 5_000))
                device.findObject(By.clazz("android.widget.EditText")).text = "Zweite Szene"
                device.findObject(By.text("Anlegen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapitel angelegt.")), 12_000))
                val store = BookStore(context)
                val id = store.listEntries().single { it.title == title }.id
                assertEquals(2, (store.load(id) as BookLoad.Found).document
                    .getJSONArray("chapters").length())

                device.findObject(By.desc("Kapitel Kapitel 1 nach unten")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Reihenfolge gespeichert.")), 12_000))
                val reordered = store.load(id) as BookLoad.Found
                assertEquals("Zweite Szene", reordered.document.getJSONArray("chapters")
                    .getJSONObject(0).getString("title"))

                device.findObject(By.desc("Kapitel Zweite Szene umbenennen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapiteltitel")), 5_000))
                device.findObject(By.clazz("android.widget.EditText")).text = "Neue Szene"
                device.findObject(By.text("Speichern")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapitel umbenannt.")), 12_000))
                device.findObject(By.desc("Kapitel Kapitel 1 öffnen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Aktuelles Kapitel: Kapitel 1")), 12_000))

                device.findObject(By.desc("Kapitel Kapitel 1 löschen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapitel endgültig löschen")), 5_000))
                device.findObject(By.text("Abbrechen")).click()
                assertEquals(2, (store.load(id) as BookLoad.Found).document
                    .getJSONArray("chapters").length())
                device.findObject(By.desc("Kapitel Kapitel 1 löschen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapitel endgültig löschen")), 5_000))
                device.findObject(By.text("Kapitel endgültig löschen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Kapitel gelöscht.")), 12_000))
                assertTrue(device.wait(Until.hasObject(By.text("Aktuelles Kapitel: Neue Szene")), 12_000))

                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.text("Zur Bücherliste")), 12_000))
                val finalBook = store.load(id) as BookLoad.Found
                assertEquals(1, finalBook.document.getJSONArray("chapters").length())
                assertEquals("Neue Szene", finalBook.document.getJSONArray("chapters")
                    .getJSONObject(0).getString("title"))
            }
        } finally {
            val store = BookStore(context)
            store.listEntries().filter { it.title == title }.forEach { store.deleteBook(it.id) }
        }
    }
}
