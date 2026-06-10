$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

$jsonText = [System.IO.File]::ReadAllText("C:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations.json", [System.Text.Encoding]::UTF8)
$replacements = $jsonText | ConvertFrom-Json

# Find all properties and their blocks
$pattern = '(?s)(?:val|fun)\s+([a-zA-Z0-9_]+)[\s\S]*?(?=(?:val|fun)\s+[a-zA-Z0-9_]+|\z)'
$matches = [regex]::Matches($text, $pattern)

foreach ($m in $matches) {
    $prop = $m.Groups[1].Value
    if ($null -ne $replacements.$prop) {
        $block = $m.Value
        
        # Iterate languages in the JSON object
        foreach ($lang in $replacements.$prop.psobject.properties.name) {
            $translated = $replacements.$prop.$lang
            $langPattern = "AppLanguage\.$lang\s*->\s*`"([^`"]*)`""
            $replacement = "AppLanguage.$lang -> `"$translated`""
            $block = [regex]::Replace($block, $langPattern, $replacement)
        }
        
        $text = $text.Replace($m.Value, $block)
    }
}

# Fix subtitle manually
$text = [regex]::Replace($text, 'Zihnin s.*?zorla.*?(?:gï¿½ï¿½ï¿½ï¿½|🧠⚡)?', "Zihnin sınırlarını zorla! 🧠⚡")
$text = [regex]::Replace($text, 'AppLanguage\.TURKISH\s*->\s*"CONTINUE"', 'AppLanguage.TURKISH -> "DEVAM ET"')

[System.IO.File]::WriteAllText($path, $text, [System.Text.Encoding]::UTF8)
Write-Host "Fixed reliably!"
