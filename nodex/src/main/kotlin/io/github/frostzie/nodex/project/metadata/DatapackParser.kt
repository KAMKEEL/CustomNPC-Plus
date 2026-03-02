package io.github.frostzie.nodex.project.metadata

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.frostzie.nodex.utils.LoggerProvider
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

data class DatapackMetadata(
    val packFormat: Int?,
    val description: String,
    val supportedFormats: FormatRange?,
)

data class FormatRange(val min: Int, val max: Int)

object DatapackParser {
    private val logger = LoggerProvider.getLogger("DatapackParser")
    private val COLOR_CODE_REGEX = Regex("(?i)§[0-9a-fk-or]")

    fun parse(path: Path): DatapackMetadata? {
        val mcmetaPath = path.resolve("pack.mcmeta")
        if (!mcmetaPath.exists()) return null

        return try {
            val content = mcmetaPath.readText()
            val json = JsonParser.parseString(content).asJsonObject
            val pack = json.getAsJsonObject("pack") ?: return null

            val packFormat = pack.get("pack_format")?.let { if (it.isJsonPrimitive) it.asInt else null }
            
            var description = parseDescription(pack.get("description"))
            description = description.replace(COLOR_CODE_REGEX, "")

            val supportedFormats = resolveSupportedFormats(pack)

            DatapackMetadata(packFormat, description, supportedFormats)
        } catch (e: Exception) {
            logger.error("Failed to parse pack.mcmeta at $path", e)
            null
        }
    }

    private fun resolveSupportedFormats(pack: JsonObject): FormatRange? {
        // 1. Try min_format / max_format
        val min = parseMajorVersion(pack.get("min_format"))
        val max = parseMajorVersion(pack.get("max_format"))
        
        if (min != null || max != null) {
            return FormatRange(min ?: 0, max ?: Int.MAX_VALUE)
        }

        // 2. Try supported_formats field (Legacy)
        val supported = parseSupportedFormats(pack.get("supported_formats"))
        if (supported != null) return supported

        // 3. Fallback to pack_format
        val fmt = pack.get("pack_format")?.let { if (it.isJsonPrimitive) it.asInt else null }
        if (fmt != null) {
            return FormatRange(fmt, fmt)
        }

        return null
    }

    private fun parseMajorVersion(element: JsonElement?): Int? {
        if (element == null) return null
        if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) return element.asInt
        if (element.isJsonArray && element.asJsonArray.size() > 0) {
            return element.asJsonArray.get(0).asInt
        }
        return null
    }

    private fun parseSupportedFormats(element: JsonElement?): FormatRange? {
        if (element == null) return null
        
        if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            val fmt = element.asInt
            return FormatRange(fmt, fmt)
        }
        
        if (element.isJsonArray) {
            val array = element.asJsonArray
            if (array.size() == 1) {
                val f = array.get(0).asInt
                return FormatRange(f, f)
            } else if (array.size() >= 2) {
                return FormatRange(array.get(0).asInt, array.get(1).asInt)
            }
        }
        
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val min = obj.get("min_inclusive")?.asInt ?: 0
            val max = obj.get("max_inclusive")?.asInt ?: Int.MAX_VALUE
            return FormatRange(min, max)
        }
        
        return null
    }

    private fun parseDescription(element: JsonElement?): String {
        if (element == null) return ""
        if (element.isJsonPrimitive) return element.asString

        // Handle JSON text component (list or object)
        return try {
             if (element.isJsonArray) {
                 element.asJsonArray.joinToString("") { parseDescription(it) }
             } else if (element.isJsonObject) {
                 val obj = element.asJsonObject
                 val text = obj.get("text")?.asString ?: ""
                 val extra = obj.get("extra")
                 val extraText = parseDescription(extra)
                 if (text.isEmpty() && extraText.isEmpty()) {
                     obj.toString()
                 } else {
                     text + extraText
                 }
             } else {
                 element.toString()
             }
        } catch (e: Exception) {
            element.toString()
        }
    }
}