package com.supremeintelligence.app

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private val Navy = Color(0xFF07111F)
private val Panel = Color(0xFF101F33)
private val Cyan = Color(0xFF67D5FF)
private val Muted = Color(0xFF9FB2C9)

data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent { SupremeApp(onSpeak = { speak(it) }) }
    }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setPitch(0.82f)
            tts?.setSpeechRate(0.88f)
        }
    }
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "supreme_reply")
    }
    override fun onDestroy() {
        tts?.stop(); tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun SupremeApp(onSpeak: (String) -> Unit) {
    val messages = remember {
        mutableStateListOf(ChatMessage(
            "Supreme Intelligence online. I am ready to reason, research, and create. What shall we work on?", false
        ))
    }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Cyan, background = Navy, surface = Panel,
        onPrimary = Navy, onBackground = Color.White, onSurface = Color.White
    )) {
        Column(Modifier.fillMaxSize().background(Navy).padding(16.dp)) {
            Column(Modifier.padding(top = 12.dp, bottom = 18.dp)) {
                Text("SUPREME", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Text("INTELLIGENCE", color = Cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("ANDROID • CORE 0.1", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFF153B38), shape = RoundedCornerShape(50)) {
                    Text("● SYSTEM READY", color = Color(0xFF82E5BD),
                        fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("Autonomy: permission-based", color = Muted, fontSize = 11.sp)
            }
            Spacer(Modifier.height(14.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages) { message ->
                    Column(Modifier.fillMaxWidth(),
                        horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start) {
                        Surface(color = if (message.fromUser) Color(0xFF16415B) else Panel,
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.widthIn(max = 320.dp)) {
                            Column(Modifier.padding(13.dp)) {
                                Text(if (message.fromUser) "YOU" else "SUPREME INTELLIGENCE",
                                    color = if (message.fromUser) Cyan else Muted,
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(5.dp))
                                Text(message.text, color = Color.White, fontSize = 15.sp)
                                if (!message.fromUser) {
                                    TextButton(onClick = { onSpeak(message.text) },
                                        contentPadding = PaddingValues(0.dp)) {
                                        Text("▶  SPEAK", color = Cyan, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            error?.let { Text(it, color = Color(0xFFFF9A9A), fontSize = 12.sp) }
            Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(value = input, onValueChange = { input = it },
                    modifier = Modifier.weight(1f).padding(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Cyan),
                    decorationBox = { inner ->
                        if (input.isEmpty()) Text("Give Supreme Intelligence a command...",
                            color = Muted, fontSize = 14.sp)
                        inner()
                    })
                Button(enabled = input.isNotBlank() && !busy, onClick = {
                    val prompt = input.trim()
                    input = ""
                    messages.add(ChatMessage(prompt, true))
                    busy = true
                    error = null
                    scope.launch {
                        try {
                            val answer = withContext(Dispatchers.IO) { askBackend(prompt) }
                            messages.add(ChatMessage(answer, false))
                        } catch (e: Exception) {
                            error = "Connection failed: ${e.message ?: "check backend configuration"}"
                        } finally { busy = false }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Navy)) {
                    Text(if (busy) "..." else "SEND", fontWeight = FontWeight.Bold)
                }
            }
            Text("External actions require your approval. Never put API keys in this app.",
                color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

private fun askBackend(prompt: String): String {
    val connection = (URL(BuildConfig.API_BASE_URL.trimEnd('/') + "/chat")
        .openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"; connectTimeout = 15000; readTimeout = 60000
        doOutput = true; setRequestProperty("Content-Type", "application/json")
    }
    try {
        val body = JSONObject().put("message", prompt).toString()
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299)
            throw IllegalStateException("Server ${connection.responseCode}: $response")
        return JSONObject(response).optString("reply", "The server returned an empty response.")
    } finally { connection.disconnect() }
}
