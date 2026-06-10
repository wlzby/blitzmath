$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path, [System.Text.Encoding]::UTF8)

# Read extracted English strings
$engDict = @{}
$extractLines = [System.IO.File]::ReadAllLines("C:\Users\ozbayv\AndroidStudioProjects\blitzmath\extract.txt", [System.Text.Encoding]::UTF8)
foreach ($line in $extractLines) {
    if ($line -match "^(.+?) === `"(.*)`"$") {
        $engDict[$matches[1]] = $matches[2]
    } elseif ($line -match "^(.+?) === (.*)$") {
        $engDict[$matches[1]] = $matches[2]
    }
}

# Read translation JSON
$json = Get-Content "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations.json" -Encoding UTF8 -Raw
$replacements = $json | ConvertFrom-Json -AsHashtable

$outLines = @()
$currentProp = ""

foreach ($line in $lines) {
    if ($line -match "(?:val|fun)\s+([a-zA-Z0-9_]+)") {
        $currentProp = $matches[1]
    }

    if ($line -match 'AppLanguage\.([A-Z]+)\s*->\s*"(.*)"') {
        $lang = $matches[1]
        
        if ($lang -ne "ENGLISH") {
            if ($replacements.ContainsKey($currentProp) -and $replacements[$currentProp].ContainsKey($lang)) {
                $translated = $replacements[$currentProp][$lang]
                $line = $line -replace '->\s*".*"', "-> `"$translated`""
            } else {
                # Fallback to English if translation is missing to avoid mojibake
                if ($engDict.ContainsKey($currentProp)) {
                    $engText = $engDict[$currentProp]
                    $line = $line -replace '->\s*".*"', "-> `"$engText`""
                }
            }
        }
    }
    $outLines += $line
}

# Fix "Zihnin sınırlarını zorla! 🧠⚡" subtitle manually
for ($i=0; $i -lt $outLines.Count; $i++) {
    if ($outLines[$i] -match "Zihnin s.*zorla") {
        $outLines[$i] = $outLines[$i] -replace '".*"', "`"Zihnin sınırlarını zorla! 🧠⚡`""
    }
    # Fix the missing English property continuation from regex matches if any
    if ($outLines[$i] -match 'AppLanguage\.TURKISH\s*->\s*"CONTINUE"') {
        $outLines[$i] = $outLines[$i] -replace '"CONTINUE"', "`"DEVAM ET`""
    }
}

[System.IO.File]::WriteAllLines($path, $outLines, [System.Text.Encoding]::UTF8)
Write-Host "Translations fixed and applied successfully."
