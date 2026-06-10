$jsonText = [System.IO.File]::ReadAllText("C:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations.json", [System.Text.Encoding]::UTF8)
$replacements = $jsonText | ConvertFrom-Json

Write-Host "Checking menuMixed..."
if ($null -ne $replacements.menuMixed) {
    Write-Host "menuMixed exists!"
    Write-Host "TURKISH: " $replacements.menuMixed.TURKISH
} else {
    Write-Host "menuMixed DOES NOT EXIST"
}
