import os
import re

def fix_global_leaderboard():
    path = r"app\src\androidMain\kotlin\com\mawelly\blitzmath\ui\screens\GlobalLeaderboardScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('initialMode: String = "classic",', 'initialMode: String,')
    content = content.replace('scrollToPlayerId: String? = null,', 'scrollToPlayerId: String?,')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed GlobalLeaderboardScreen")

def fix_vs_screen():
    path = r"app\src\androidMain\kotlin\com\mawelly\blitzmath\ui\screens\VsScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('val context = androidx.compose.ui.platform.LocalContext.current', '')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed VsScreen")

def fix_scientist_dialog():
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\dialogs\ScientistCardUnlockDialog.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('painter = org.jetbrains.compose.resources.org.jetbrains.compose.resources.painterResource', 'painter = org.jetbrains.compose.resources.painterResource')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed ScientistCardUnlockDialog")

def fix_support_dialog():
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\dialogs\SupportDialog.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    # SupportDialog uses context to open URLs. I need to replace it with platformServices.
    content = content.replace('val context = LocalContext.current', 'val platformServices = LocalPlatformServices.current')
    content = content.replace('import androidx.compose.ui.platform.LocalContext', 'import com.mawelly.blitzmath.core.LocalPlatformServices')
    content = content.replace('context.startActivity', 'platformServices.launchUrl')
    
    # Needs a regex to fix Intent(Intent.ACTION_VIEW, Uri.parse(...))
    content = re.sub(r'android\.content\.Intent\(android\.content\.Intent\.ACTION_VIEW,\s*android\.net\.Uri\.parse\((.*?)\)\)', r'\1', content)
    # The actual code might be: context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    # We want: platformServices.launchUrl(url)
    content = re.sub(r'platformServices\.launchUrl\(Intent\(Intent\.ACTION_VIEW,\s*Uri\.parse\((.*?)\)\)\)', r'platformServices.launchUrl(\1)', content)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed SupportDialog")

def fix_update_dialog():
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\dialogs\UpdateDialog.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('painter = org.jetbrains.compose.resources.org.jetbrains.compose.resources.painterResource(Res.drawable.blitzmath_logo)', 'painter = org.jetbrains.compose.resources.painterResource(blitzmath.app.generated.resources.Res.drawable.blitzmath_logo)')
    content = content.replace('val context = LocalContext.current', 'val platformServices = LocalPlatformServices.current')
    content = content.replace('import androidx.compose.ui.platform.LocalContext', 'import com.mawelly.blitzmath.core.LocalPlatformServices')
    content = re.sub(r'context\.startActivity\(Intent\(Intent\.ACTION_VIEW,\s*Uri\.parse\((.*?)\)\)\)', r'platformServices.launchUrl(\1)', content)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed UpdateDialog")

def fix_collection_screen():
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\screens\CollectionScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('painter = org.jetbrains.compose.resources.org.jetbrains.compose.resources.painterResource', 'painter = org.jetbrains.compose.resources.painterResource')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed CollectionScreen")

def fix_settings_screen():
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\screens\SettingsScreen.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('import com.mawelly.blitzmath.LanguageManager\n', '')
    content = content.replace('import com.mawelly.blitzmath.data.GameDataStore\n', '')
    content = content.replace('vibrateTick(context)', 'platformServices.hapticManager.vibrateTick()')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed SettingsScreen")

def fix_main_activity():
    path = r"app\src\androidMain\kotlin\com\mawelly\blitzmath\MainActivity.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # We need to make sure MainActivity matches GameScreen signature again.
    # GameScreen is now `fun GameScreen(mode: GameMode, startLevel: Int, soundManager: SoundManager, voiceManager: VoiceManager?, dataStore: GameDataStore)`
    # BUT wait, the errors are:
    # e: MainActivity.kt:600:25 No parameter with name 'soundManager' found. (VsScreen)
    # e: MainActivity.kt:601:25 No parameter with name 'languageManager' found. (VsScreen)
    
    # For VsScreen:
    content = content.replace('soundManager = soundManager,\n                        languageManager = languageManager,\n', '')
    
    # For LanguageSelectionScreen: No value passed for parameter 'dataStore'
    content = content.replace('LanguageSelectionScreen(\n                        onLanguageSelected = {', 'LanguageSelectionScreen(\n                        dataStore = gameDataStore,\n                        onLanguageSelected = {')
    
    # For SettingsScreen: No value passed for parameter 'dataStore'
    content = content.replace('SettingsScreen(\n                        onBackToMenu = {', 'SettingsScreen(\n                        dataStore = gameDataStore,\n                        onBackToMenu = {')
    
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed MainActivity")

if __name__ == "__main__":
    fix_global_leaderboard()
    fix_vs_screen()
    fix_scientist_dialog()
    fix_support_dialog()
    fix_update_dialog()
    fix_collection_screen()
    fix_settings_screen()
    fix_main_activity()
