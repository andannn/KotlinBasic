package serialization.chapter_2

import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.math.PI

class BuildInTypesTest {
    @Test
    fun numbers_decode_test() {
        @Serializable
        class Data(
            val answer: Int,
            val pi: Double
        )

        val data = Data(42, PI)
        println(Json.encodeToString(data))
    }

    enum class Status { @SerialName("maintained")SUPPORTED }
    @Test
    fun enum_class_encode_test() {
        @Serializable
        class Project(val name: String, val status: Status)

        val data = Project("kotlinx.serialization", Status.SUPPORTED)
        println(Json.encodeToString(data))
    }

    @Test
    fun composites_class_encode_test() {
        @Serializable
        class Project(val name: String)

        val pair = 1 to Project("kotlinx.serialization")
        println(Json.encodeToString(pair))

        val list = listOf(
            Project("kotlinx.serialization"),
            Project("kotlinx.coroutines")
        )
        println(Json.encodeToString(list))

        val set = setOf(
            Project("kotlinx.serialization"),
            Project("kotlinx.coroutines")
        )
        println(Json.encodeToString(set))
    }

    @Test
    fun deserializing_collection() {
        @Serializable
        data class Data(
            val a: List<Int>,
            val b: Set<Int>
        )
        @Serializable
        class Project(val name: String)

        val data = Json.decodeFromString<Data>("""
        {
            "a": [42, 42],
            "b": [42, 42]
        }
    """)
        println(data)

        val map = mapOf(
            1 to Project("kotlinx.serialization"),
            2 to Project("kotlinx.coroutines")
        )
        println(Json.encodeToString(map))
    }
}
