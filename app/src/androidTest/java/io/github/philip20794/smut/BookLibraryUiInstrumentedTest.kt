package io.github.philip20794.smut

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BookLibraryUiInstrumentedTest {
    @Test fun nativeLibraryCreateOpenRenameDuplicateAndConfirmDelete() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val prefix = "B01-UI-" + UUID.randomUUID().toString().take(8)
        val oldName = prefix + "-Entwurf"
        val newName = prefix + "-Roman"
        try {
            ActivityScenario.launch(LibraryActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Neues Buch")), 12_000))
                device.findObject(By.text("Neues Buch")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buchtitel")), 5_000))
                val input = device.findObject(By.clazz("android.widget.EditText"))
                assertTrue("Buchtitel muss editierbar sein", input != null)
                input.text = oldName
                device.findObject(By.text("Anlegen")).click()

                assertTrue(device.wait(Until.hasObject(By.text("Buch geöffnet · Leseansicht")), 12_000))
                assertTrue(device.hasObject(By.text(oldName)))
                device.findObject(By.text("Zur Bücherliste")).click()
                assertTrue(device.wait(Until.hasObject(By.text(oldName)), 8_000))

                // Actual DB reload and an accessible, intentional open action.
                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.text(oldName)), 12_000))
                device.findObject(By.text("Umbenennen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buch umbenennen")), 5_000))
                device.findObject(By.clazz("android.widget.EditText")).text = newName
                device.findObject(By.text("Speichern")).click()
                assertTrue(device.wait(Until.hasObject(By.text(newName)), 12_000))

                device.findObject(By.text("Löschen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Endgültig löschen")), 5_000))
                device.findObject(By.text("Abbrechen")).click()
                assertTrue(device.hasObject(By.text(newName)))

                device.findObject(By.text("Duplizieren")).click()
                assertTrue(device.wait(Until.hasObject(By.text(newName + " (Kopie)")), 12_000))
                assertTrue(device.hasObject(By.text(newName)))

                // The first card after duplication is the copy; delete explicitly.
                device.findObject(By.text("Löschen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Endgültig löschen")), 5_000))
                device.findObject(By.text("Endgültig löschen")).click()
                assertTrue(device.wait(Until.hasObject(By.text("Buch gelöscht.")), 12_000))
                scenario.recreate()
                assertTrue(device.wait(Until.hasObject(By.text("Neues Buch")), 12_000))
                val matches = BookStore(context).listEntries().filter { it.title.startsWith(prefix) }
                assertTrue("One of original/copy should remain after confirmed delete", matches.size == 1)
            }
        } finally {
            val store = BookStore(context)
            store.listEntries().filter { it.title.startsWith(prefix) }.forEach { store.deleteBook(it.id) }
        }
    }
}
