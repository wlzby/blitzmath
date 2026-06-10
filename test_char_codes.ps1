$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

$enc1254 = [System.Text.Encoding]::GetEncoding(1254)
$bytes = $enc1254.GetBytes($text)
$restored = [System.Text.Encoding]::UTF8.GetString($bytes)

$idx = $restored.IndexOf("KLAS")
if ($idx -ge 0) {
    $sub = $restored.Substring($idx, 10)
    Write-Host "String: $sub"
    $charCodes = ""
    foreach ($c in $sub.ToCharArray()) {
        $charCodes += [int]$c + " "
    }
    Write-Host "Codes: $charCodes"
}
