package serialization.chapter_1

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class BasicTests {

    @Test
    fun encoding_test() {
        @Serializable
        class Project(val name: String, val language: String)

        val data = Project("kotlinx.serialization", "Kotlin")
        println(Json.encodeToString(data))
    }

    @Test
    fun decoding_test() {
        @Serializable
        data class Project(val name: String, val language: String)

        val data = Json.decodeFromString<Project>(
            """
        {"name":"kotlinx.serialization","language":"Kotlin"}
        """
        )
        println(data)
    }

    @Test
    fun only_backing_filed_serialized_test() {
        @Serializable
        class Project(
            // name is a property with backing field -- serialized
            var name: String
        ) {
            var stars: Int = 0 // property with a backing field -- serialized

            val path: String // getter only, no backing field -- not serialized
                get() = "kotlin/$name"

            var id by ::name // delegated property -- not serialized
        }
        val data = Project("kotlinx.serialization").apply { stars = 9000 }
        println(Json.encodeToString(data))
    }

    @Test
    fun serialization_work_with_primary_test() {
        @Serializable
        class Project private constructor(val owner: String, val name: String) {
            constructor(path: String) : this(
                owner = path.substringBefore('/'),
                name = path.substringAfter('/')
            )

            val path: String
                get() = "$owner/$name"
        }

        val data = Project("kotlin/kotlinx.serialization")
        println(Json.encodeToString(data))
    }


    @Test
    fun validation_decoded_data_test() {
        @Serializable
        class Project(val name: String) {
            init {
                require(name.isNotEmpty()) { "name cannot be empty" }
            }
        }

        val data = Json.decodeFromString<Project>("""
        {"name":""}
    """)
        println(data)
    }

    @Test
    fun optional_property_test() {
        @Serializable
        data class Project(val name: String, val language: String = "Kotlin")

        val data = Json.decodeFromString<Project>("""
        {"name":"kotlinx.serialization"}
    """)
        println(data)
    }

    @Test
    fun optional_property_with_initializer_test() {
        fun computeLanguage(): String {
            println("Computing")  // will not be print
            return "Kotlin"
        }

        @Serializable
        data class Project(val name: String, val language: String = computeLanguage())

        val data = Json.decodeFromString<Project>("""
        {"name":"kotlinx.serialization","language":"Kotlin"}
    """)
        println(data)
    }

    @Test
    fun required_property_test() {
        @Serializable
        data class Project(val name: String, @Required val language: String = "Kotlin")

        val data = Json.decodeFromString<Project>("""
        {"name":"kotlinx.serialization"}
    """)
        println(data)
    }

    @Test
    fun transient_property_test() {
        @Serializable
        data class Project(val name: String, @Transient val language: String = "Kotlin")

        val data = Json.decodeFromString<Project>("""
        {"name":"kotlinx.serialization","language":"Kotlin"}
    """)
        println(data)
    }

    @Test
    fun default_property_will_not_be_encoded_by_default() {
        @Serializable
        data class Project(val name: String, val language: String = "Kotlin")

        val data = Project("kotlinx.serialization")
        println(Json.encodeToString(data))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun encode_default_annotation_test() {
        @Serializable
        data class Project(
            val name: String,
            @EncodeDefault val language: String = "Kotlin"
        )
        @Serializable
        data class User(
            val name: String,
            @EncodeDefault(EncodeDefault.Mode.NEVER) val projects: List<Project> = emptyList()
        )

        val userA = User("Alice", listOf(Project("kotlinx.serialization")))
        val userB = User("Bob")
        println(Json.encodeToString(userA))
        println(Json.encodeToString(userB))
    }

    @Test
    fun nullable_property_test() {
        @Serializable
        class Project(val name: String, val renamedTo: String?)

        val data = Project("kotlinx.serialization", null)
        println(Json.encodeToString(data))
    }

    @Test
    fun type_safety_test() {
        @Serializable
        data class Project(val name: String, val language: String = "Kotlin")

        val data = Json.decodeFromString<Project>("""
        {"name":"kotlinx.serialization","language":null}
    """)
        println(data)
    }

    @Test
    fun generic_test() {
        @Serializable
        data class Project(val name: String, val language: String = "Kotlin")

        @Serializable
        class Box<T>(val contents: T)

        @Serializable
        class Data(
            val a: Box<Int>,
            val b: Box<Project>
        )

        val data = Data(Box(42), Box(Project("kotlinx.serialization", "Kotlin")))
        println(Json.encodeToString(data))
    }

    @Test
    fun serial_field_name_test() {
        @Serializable
        class Project(val name: String, @SerialName("lang") val language: String)

        val data = Project("kotlinx.serialization", "Kotlin")
        println(Json.encodeToString(data))
    }
}