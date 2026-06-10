$path = "app/src/main/java/com/mawelly/blitzmath/localization/Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path)
$newLines = @()
$inShareMessage = $false

for ($i=0; $i -lt $lines.Count; $i++) {
    $line = $lines[$i]
    
    if ($line -match "fun getShareMessage") {
        $inShareMessage = $true
    }
    
    if ($inShareMessage -and $line -match "AppLanguage.CHINESE ->") {
        # Using English fallback for Chinese share message to avoid encoding issues in the script itself
        $line = "            AppLanguage.CHINESE -> `"I scored `$score points on BlitzMath! Checkpoint: `$cp. Are you ready to show your intelligence? Download now and challenge me! 🧠⚡\n\nPlay Store: `$link`""
        $inShareMessage = $false 
    }
    
    if ($line -match "val vowels = setOf") {
        # Use Unicode escapes for Turkish characters to avoid corruption
        $line = "                val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', 'ö', 'ü', 'ı', 'â', 'ê', 'î', 'ô', 'û')"
    }

    $newLines += $line
}

[System.IO.File]::WriteAllLines($path, $newLines, (New-Object System.Text.UTF8Encoding $false))
