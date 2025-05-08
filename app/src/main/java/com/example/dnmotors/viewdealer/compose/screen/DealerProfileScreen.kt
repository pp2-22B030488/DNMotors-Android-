package com.example.dnmotors.viewdealer.compose.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.dnmotors.R
import com.example.domain.model.User
import com.example.dnmotors.view.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@Composable
fun DealerProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var profile by remember { mutableStateOf(User()) }
    var showLangDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = (LocalContext.current as Activity)

    LaunchedEffect(userId) {
        userId?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        profile = User(
                            name = document.getString("name") ?: "",
                            email = document.getString("email") ?: "",
                            avatarUrl = document.getString("avatarUrl") ?: "",
                            profileFon = document.getString("profileFon") ?: "",
                            location = document.getString("location") ?: "",
                            phoneNumber = document.getString("phoneNumber") ?: ""
                        )
                    }
                }
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        // Header
        Box(
            modifier = Modifier
                .height(260.dp)
                .fillMaxWidth()
        ) {
            if (profile.profileFon.isNotEmpty()) {
                AsyncImage(
                    model = profile.profileFon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile_fon),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (profile.avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profile.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.email,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        // Card 1
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                ProfileItem(icon = R.drawable.ic_address, text = profile.location)
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                ProfileItem(icon = R.drawable.ic_phone, text = profile.phoneNumber)
            }
        }

        // Card 2 - Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                ProfileItem(
                    icon = R.drawable.ic_edit_profile,
                    text = stringResource(id = R.string.edit_profile),
                    trailingIcon = R.drawable.ic_arrow_right,
                    onClick = { navController.navigate("edit_profile_screen") }
                )
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                NotificationItem(activity)
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                ProfileItem(
                    icon = R.drawable.ic_password,
                    text = stringResource(id = R.string.passwords),
                    trailingIcon = R.drawable.ic_arrow_right,
                    onClick = { navController.navigate("change_password") }
                )
                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                ProfileItem(
                    icon = R.drawable.ic_language,
                    text = stringResource(id = R.string.language),
                    trailingIcon = R.drawable.ic_arrow_right,
                    onClick = { showLangDialog = true } // открываем диалог
                )

                Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                ProfileItem(
                    icon = R.drawable.ic_logout,
                    text = stringResource(id = R.string.logout),
                    trailingIcon = R.drawable.ic_arrow_right,
                    iconTint = Color.Red,
                    onClick = {
                        auth.signOut()

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)

                        activity?.finish() // на всякий случай закрыть текущую activity
                    }
                )
            }
        }
    }

    if (showLangDialog) {
        val context = LocalContext.current
        val activity = LocalActivity.current

        LanguageSelectionDialog(
            onDismiss = { showLangDialog = false },
            onLanguageSelected = { langCode ->
                saveLanguageCode(context, langCode)
                setLocale(context, langCode)
                showLangDialog = false
                activity?.recreate()
            }
        )
    }

}

@Composable
fun ProfileItem(
    @DrawableRes icon: Int,
    text: String,
    @DrawableRes trailingIcon: Int? = null,
    iconTint: Color = Color(0xFF757575),
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
            color = Color(0xFF212121)
        )
        if (trailingIcon != null) {
            Icon(
                painter = painterResource(id = trailingIcon),
                contentDescription = null,
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NotificationItem(activity: Activity) {
    var checked by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(id = R.string.notifications),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            fontSize = 16.sp,
            color = Color(0xFF212121)
        )
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF4CAF50),
                checkedTrackColor = Color(0xFFA5D6A7),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}


@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("English", "Қазақша", "Русский")
    val codes = listOf("en", "kk", "ru")
    val context = LocalContext.current
    val currentLang = getSavedLanguageCode(context)
    var selectedIndex by remember { mutableStateOf(codes.indexOf(currentLang)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column {
                languages.forEachIndexed { index, language ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedIndex = index
                                onLanguageSelected(codes[index])
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                onLanguageSelected(codes[index])
                            }
                        )
                        Text(language)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

        fun saveLanguageCode(context: Context, code: String) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putString("lang", code).apply()
        }

        fun getSavedLanguageCode(context: Context): String {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            return prefs.getString("lang", "en") ?: "en"
        }

        fun setLocale(context: Context, langCode: String) {
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }


