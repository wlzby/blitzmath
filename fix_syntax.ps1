$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path, [System.Text.Encoding]::UTF8)
$newLines = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Trim() -eq "val time: String") {
        if ($i -gt 0 -and $lines[$i-1].Trim() -eq "}") {
            $newLines += "    }"
        }
    }
    
    if ($lines[$i] -match "val vowels = setOf") {
        $newLines += "                val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', '\u00F6', '\u00FC', '\u0131', '\u00E2', '\u00EA', '\u00EE', '\u00F4', '\u00FB')"
    } else {
        $newLines += $lines[$i]
    }
}

[System.IO.File]::WriteAllLines($path, $newLines, [System.Text.Encoding]::UTF8)
