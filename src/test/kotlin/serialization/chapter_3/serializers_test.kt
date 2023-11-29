package serialization.chapter_3

import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.math.PI

class SerializersTest {
    @Test
    fun default_serializers_test() {
        @Serializable
        class Color(val rgb: Int)

        val green = Color(0x00ff00)
        println(Json.encodeToString(green))

        val colorSerializer: KSerializer<Color> = Color.serializer()
        println(colorSerializer.descriptor)

        @Serializable
        @SerialName("Box")
        class Box<T>(val contents: T)

        val boxedColorSerializer = Box.serializer(Color.serializer())
        println(boxedColorSerializer.descriptor)

        val stringToColorMapSerializer: KSerializer<Map<String, Color>> = serializer()
        println(stringToColorMapSerializer.descriptor)
    }

    @Serializable(with = ColorAsStringSerializer::class)
    @SerialName("Color")
    class Color(val rgb: Int)

    object ColorAsStringSerializer : KSerializer<Color> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Color) {
            val string = value.rgb.toString(16).padStart(6, '0')
            encoder.encodeString(string)
        }

        override fun deserialize(decoder: Decoder): Color {
            val string = decoder.decodeString()
            return Color(string.toInt(16))
        }
    }
    @Test
    fun custom_serializers_test() {
        val green = Color(0x00ff00)
        println(Json.encodeToString(green))

        val color = Json.decodeFromString<Color>("\"00ff00\"")
        println(color.rgb)
    }
}
