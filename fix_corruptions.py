path = r"C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
with open(path, "rb") as f:
    content = f.read()

# Try to find common corrupted patterns
corruptions = {
    b'de\xef\xbf\xbdYi\xef\xbf\xbdYimini': 'değişimini'.encode('utf-8'),
    b'i\xef\xbf\xbdin': 'için'.encode('utf-8'),
    b'K\xc7\xacf\xc7\xacr': 'Küfür'.encode('utf-8'),
    b'YEN\xef\xbf\xbd': 'YENİ'.encode('utf-8'),
    b'Yasakl\xef\xbf\xbd': 'Yasaklı'.encode('utf-8'),
}

new_content = content
for c, r in corruptions.items():
    new_content = new_content.replace(c, r)

# Write back as UTF-8
with open(path, "wb") as f:
    f.write(new_content)

print("Attempted to fix common corruptions.")
