$path = 'c:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt'
$content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

# Specific String Fixes
$content = $content.Replace('KARIÅžIK MOD', 'KARIŞIK MOD')
$content = $content.Replace('DÃœNYA SIRALAMASI', 'DÜNYA SIRALAMASI')
$content = $content.Replace('Ã‡IKIÅž', 'ÇIKIŞ')
$content = $content.Replace('Beyninizi ÅŸekillendirin!', 'Beyninizi Şekillendirin!')
$content = $content.Replace('ğŸ§ âš¡', '🧠⚡')
$content = $content.Replace('ğŸ ‹ï¸ ', '🏋️‍♂️')

# Generic Character Fixes
$content = $content.Replace('ÅŸ', 'ş')
$content = $content.Replace('ÄŸ', 'ğ')
$content = $content.Replace('Ä±', 'ı')
$content = $content.Replace('Ã§', 'ç')
$content = $content.Replace('Ã¶', 'ö')
$content = $content.Replace('Ã¼', 'ü')
$content = $content.Replace('Ãœ', 'Ü')
$content = $content.Replace('Ä°', 'İ')
$content = $content.Replace('Ã‡', 'Ç')
$content = $content.Replace('Åž', 'Ş')

[System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding($false)))
