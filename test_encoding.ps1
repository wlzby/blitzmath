$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"

# Read the file correctly as UTF-8 into a string
$text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

# Try to reverse it using Windows-1254 (Turkish ANSI)
try {
    $enc1254 = [System.Text.Encoding]::GetEncoding(1254)
    # The string characters are actually bytes of the original UTF-8 file
    $bytes = $enc1254.GetBytes($text)
    # Now decode those bytes as UTF-8
    $restored = [System.Text.Encoding]::UTF8.GetString($bytes)
    
    Write-Host "Success decoding with 1254!"
    $idx = $restored.IndexOf("KLAS")
    if ($idx -ge 0) {
        Write-Host $restored.Substring($idx, 50)
    }
    $idx2 = $restored.IndexOf("KARI")
    if ($idx2 -ge 0) {
        Write-Host $restored.Substring($idx2, 50)
    }
} catch {
    Write-Host "Failed 1254: $_"
}

# Try to reverse it using Windows-1252 (Western European ANSI)
try {
    $enc1252 = [System.Text.Encoding]::GetEncoding(1252)
    $bytes = $enc1252.GetBytes($text)
    $restored = [System.Text.Encoding]::UTF8.GetString($bytes)
    
    Write-Host "Success decoding with 1252!"
    $idx = $restored.IndexOf("KLAS")
    if ($idx -ge 0) {
        Write-Host $restored.Substring($idx, 50)
    }
} catch {
    Write-Host "Failed 1252: $_"
}
