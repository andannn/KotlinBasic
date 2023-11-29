package serialization.chapter_4

import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class PolymorphismTest {
    @Serializable
    sealed class Project {
        abstract val name: String
        var status = "open"
    }

    @Serializable
    @SerialName("owned")
    class OwnedProject(override val name: String, val owner: String) : Project()

    @Test
    fun serializable_hierarchy_test() {
        val data = OwnedProject("kotlinx.coroutines", "kotlin")
        println(Json.encodeToString(data)) //
    }
}
