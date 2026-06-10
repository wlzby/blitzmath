$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

# Simple regex to find properties and their English text
$regex = [regex]'(?:val|fun)\s+([a-zA-Z0-9_]+)[\s\S]*?AppLanguage\.ENGLISH\s*->\s*(?:listOf\()?((?:".*?"(?:,\s*)?)+)'
$matches = $regex.Matches($content)

$out = @()
foreach ($m in $matches) {
    $prop = $m.Groups[1].Value
    $eng = $m.Groups[2].Value
    $out += "$prop === $eng"
}

[System.IO.File]::WriteAllLines("C:\Users\ozbayv\AndroidStudioProjects\blitzmath\extract.txt", $out)
