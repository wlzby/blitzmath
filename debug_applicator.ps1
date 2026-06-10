$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path, [System.Text.Encoding]::UTF8)

$jsonText = [System.IO.File]::ReadAllText("C:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations.json", [System.Text.Encoding]::UTF8)
$replacements = $jsonText | ConvertFrom-Json

$currentProp = ""
foreach ($line in $lines) {
    if ($line -match "(?:val|fun)\s+([a-zA-Z0-9_]+)") {
        $currentProp = $matches[1]
    }

    if ($line -match 'AppLanguage\.([A-Z]+)\s*->\s*"(.*)"') {
        $lang = $matches[1]
        
        if ($currentProp -eq "menuMixed" -and $lang -eq "TURKISH") {
            Write-Host "FOUND line: $line"
            Write-Host "CurrentProp is: $currentProp"
            Write-Host "Lang is: $lang"
            if ($null -ne $replacements.$currentProp -and $null -ne $replacements.$currentProp.$lang) {
                Write-Host "Translation available!"
                $translated = $replacements.$currentProp.$lang
                $newLine = $line -replace '->\s*".*"', "-> `"$translated`""
                Write-Host "New line would be: $newLine"
            } else {
                Write-Host "Translation NOT available!"
            }
        }
    }
}
