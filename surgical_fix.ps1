$path = "app/src/main/java/com/mawelly/blitzmath/localization/Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path)

# Line 220 fix (vowels)
$lines[219] = "                val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', 'ö', 'ü', 'ı', 'â', 'ê', 'î', 'ô', 'û')"

# Line 1007 fix (shareMessage Chinese)
$lines[1006] = "            AppLanguage.CHINESE -> `"I scored `$score points on BlitzMath! Checkpoint: `$cp. Are you ready to show your intelligence? Download now and challenge me! 🧠⚡\n\nPlay Store: `$link`""

[System.IO.File]::WriteAllLines($path, $lines, (New-Object System.Text.UTF8Encoding $false))
Write-Host "Fixed line 220 and 1007!"
