$path = "app/src/main/java/com/mawelly/blitzmath/localization/Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path)

# Repair getSelectLanguage
$newLines = @()
$skip = $false
for ($i=0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match "fun getSelectLanguage") {
        $newLines += $lines[$i]
        $newLines += "        return when (lang ?: currentLanguage) {"
        $newLines += "            AppLanguage.TURKISH -> `"Dilini Seç`""
        $newLines += "            AppLanguage.ENGLISH -> `"Select Language`""
        $newLines += "            AppLanguage.SPANISH -> `"Seleccionar İdioma`""
        $newLines += "            AppLanguage.GERMAN -> `"Sprache wählen`""
        $newLines += "            AppLanguage.FRENCH -> `"Choisir la langue`""
        $newLines += "            AppLanguage.ITALIAN -> `"Seleziona lingua`""
        $newLines += "            AppLanguage.PORTUGUESE -> `"Selecionar İdioma`""
        $newLines += "            AppLanguage.HINDI -> `"भाषा चुनें`""
        $newLines += "            AppLanguage.CHINESE -> `"选择语言`""
        $newLines += "            AppLanguage.RUSSIAN -> `"Выбрать язык`""
        $newLines += "        }"
        # Skip the corrupted lines that were there
        $skip = $true
        continue
    }
    
    if ($skip) {
        if ($lines[$i] -match "fun ") { # Stop skipping when next function starts
            $skip = $false
        } elseif ($lines[$i] -match "val ") {
            $skip = $false
        }
    }
    
    if (-not $skip) {
        # Fix the $cpã line while we are at it
        $line = $lines[$i]
        if ($line -match "\$cpã€‚") {
            $line = $line -replace "\$cpã€‚", "`$cp. "
        }
        $newLines += $line
    }
}

[System.IO.File]::WriteAllLines($path, $newLines, (New-Object System.Text.UTF8Encoding $false))
