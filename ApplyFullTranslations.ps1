$path = "c:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings_fixed.kt"
$text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

$jsonText = [System.IO.File]::ReadAllText("c:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations_all.json", [System.Text.Encoding]::UTF8)
$translations = $jsonText | ConvertFrom-Json

# We iterate over every block inside Strings_fixed.kt
$pattern = '(?s)(?:val|fun)\s+([a-zA-Z0-9_]+)[\s\S]*?(?=(?:val|fun)\s+[a-zA-Z0-9_]+|\z)'
$matches = [regex]::Matches($text, $pattern)

foreach ($m in $matches) {
    $block = $m.Value
    
    # Extract the exact English string
    $engPattern = 'AppLanguage\.ENGLISH\s*->\s*"([^"]*)"'
    $engMatch = [regex]::Match($block, $engPattern)
    
    if ($engMatch.Success) {
        $engStr = $engMatch.Groups[1].Value
        
        # Look up in JSON
        # Since JSON keys have spaces, we can access via $translations."$engStr"
        $lookupKey = $engStr
        if ($engStr -eq "Score") { $lookupKey = "NORMAL_SCORE" }
        if ($engStr -eq "SCORE") { $lookupKey = "STAT_SCORE" }
        
        $transObj = $translations."$lookupKey"
        
        if ($null -ne $transObj) {
            
            # Replace Russian
            if ($null -ne $transObj.RUSSIAN) {
                $rus = $transObj.RUSSIAN
                $block = [regex]::Replace($block, 'AppLanguage\.RUSSIAN\s*->\s*"[^"]*"', "AppLanguage.RUSSIAN -> `"$rus`"")
            }
            
            # Replace Chinese
            if ($null -ne $transObj.CHINESE) {
                $chi = $transObj.CHINESE
                $block = [regex]::Replace($block, 'AppLanguage\.CHINESE\s*->\s*"[^"]*"', "AppLanguage.CHINESE -> `"$chi`"")
            }
            
            # Replace Hindi
            if ($null -ne $transObj.HINDI) {
                $hin = $transObj.HINDI
                $block = [regex]::Replace($block, 'AppLanguage\.HINDI\s*->\s*"[^"]*"', "AppLanguage.HINDI -> `"$hin`"")
            }
            
            $text = $text.Replace($m.Value, $block)
        }
    }
}

[System.IO.File]::WriteAllText("c:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt", $text, [System.Text.Encoding]::UTF8)
Write-Output "Done applying translations!"
