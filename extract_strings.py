import re

path = r"C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
with open(path, "r", encoding="utf-8", errors="replace") as f:
    content = f.read()

# Extract properties and their ENGLISH translations
matches = re.finditer(r'(?:val|fun)\s+([a-zA-Z0-9_]+).*?\{.*?AppLanguage\.ENGLISH\s*->\s*(?:listOf\()?((?:".*?"(?:,\s*)?)+).*?\}', content, re.DOTALL)

with open("extract.txt", "w", encoding="utf-8") as out:
    for m in matches:
        prop_name = m.group(1)
        eng_string = m.group(2).strip()
        out.write(f"{prop_name} === {eng_string}\n")
