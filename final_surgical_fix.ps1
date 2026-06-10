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
        $line = "            AppLanguage.CHINESE -> `"我在 BlitzMath 中获得了 `$score 分！关卡：`$cp。准备好展示你的智慧了吗？立即下载并挑战我！ 🧠⚡\n\nPlay Store: `$link`""
        $inShareMessage = $false # only one chinese entry in shareMessage
    }
    
    # Also fix the vowels line just in case it got corrupted again
    if ($line -match "val vowels = setOf") {
        $line = "                val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', 'ö', 'ü', 'ı', 'â', 'ê', 'î', 'ô', 'û')"
    }

    $newLines += $line
}

[System.IO.File]::WriteAllLines($path, $newLines, (New-Object System.Text.UTF8Encoding $false))
