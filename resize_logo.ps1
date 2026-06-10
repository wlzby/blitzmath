Add-Type -AssemblyName System.Drawing
$sourceFile = "C:\Users\ozbayv\.gemini\antigravity\brain\158d210a-29d6-4086-ae03-ce8698f9ce27\media__1777586351319.jpg"
$destFile = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\huawei_icon_perfect.png"

$img = [System.Drawing.Image]::FromFile($sourceFile)
$bmp = New-Object System.Drawing.Bitmap(512,512)
$g = [System.Drawing.Graphics]::FromImage($bmp)

$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

$g.DrawImage($img, 0, 0, 512, 512)

$bmp.Save($destFile, [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bmp.Dispose()
$img.Dispose()

Write-Host "Logo successfully resized to 512x512 and saved to $destFile"
