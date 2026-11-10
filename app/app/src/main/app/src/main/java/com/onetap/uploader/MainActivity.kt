
package com.onetap.uploader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneTapApp()
        }
    }
}

@Composable
fun OneTapApp() {
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }

    var youtube by remember { mutableStateOf(true) }
    var instagram by remember { mutableStateOf(true) }
    var facebook by remember { mutableStateOf(true) }

    var message by remember {
        mutableStateOf("Choose a video to get started.")
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        videoUri = uri
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "OneTap Uploader",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text("One video. Your selected platforms.")

                Button(
                    onClick = { picker.launch("video/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (videoUri == null)
                            "Choose Video"
                        else
                            "Change Video"
                    )
                }

                Text(
                    if (videoUri == null)
                        "No video selected"
                    else
                        "Video selected"
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption and hashtags") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text(
                    "Select platforms",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    Checkbox(
                        checked = youtube,
                        onCheckedChange = { youtube = it }
                    )
                    Text("YouTube")
                }

                Row {
                    Checkbox(
                        checked = instagram,
                        onCheckedChange = { instagram = it }
                    )
                    Text("Instagram")
                }

                Row {
                    Checkbox(
                        checked = facebook,
                        onCheckedChange = { facebook = it }
                    )
                    Text("Facebook Page")
                }

                Button(
                    onClick = {
                        message = when {
                            videoUri == null ->
                                "Please choose a video first."

                            !youtube && !instagram && !facebook ->
                                "Select at least one platform."

                            else ->
                                "Prototype only: publishing is not connected yet. API login and backend setup are still required."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }

                Text(message)

                Text(
                    "Note: This is an initial UI prototype. " +
                    "Real publishing is not connected yet.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
