import os

def fix_main_activity():
    path = r"app\src\androidMain\kotlin\com\mawelly\blitzmath\MainActivity.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Restore soundManager to GameScreen calls
    content = content.replace(
'''                          mode = GameMode.CLASSIC,
                          startLevel = currentLevel,
                          
                          voiceManager = voiceManager,''',
'''                          mode = GameMode.CLASSIC,
                          startLevel = currentLevel,
                          soundManager = soundManager,
                          voiceManager = voiceManager,'''
    )
    content = content.replace(
'''                          mode = GameMode.MIXED,
                          startLevel = currentLevel,
                          
                          voiceManager = voiceManager,''',
'''                          mode = GameMode.MIXED,
                          startLevel = currentLevel,
                          soundManager = soundManager,
                          voiceManager = voiceManager,'''
    )
    content = content.replace(
'''                          mode = GameMode.CHALLENGE,
                          startLevel = 1,
                          
                          voiceManager = voiceManager,''',
'''                          mode = GameMode.CHALLENGE,
                          startLevel = 1,
                          soundManager = soundManager,
                          voiceManager = voiceManager,'''
    )
    
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed MainActivity")

def fix_dialogs():
    # UpdateDialog.kt
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\dialogs\UpdateDialog.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('platformServices.launchUrl', 'platformServices.openUrl')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    
    # SupportDialog.kt
    path = r"app\src\commonMain\kotlin\com\mawelly\blitzmath\ui\dialogs\SupportDialog.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace('platformServices.launchUrl', 'platformServices.openUrl')
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Fixed Dialogs")

if __name__ == "__main__":
    fix_main_activity()
    fix_dialogs()
