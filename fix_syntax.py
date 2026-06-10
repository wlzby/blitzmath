import re

path = r"C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"

with open(path, "r", encoding="utf-8") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if line.strip() == "val time: String":
        # Check if previous line is just '        }'
        if lines[i-1].strip() == "}":
            lines.insert(i, "    }\n")
            break

# Also fix the vowels array on line 220
for i, line in enumerate(lines):
    if "val vowels = setOf" in line:
        lines[i] = "                val vowels = setOf('a', 'e', 'i', 'o', 'u', 'y', '\\u00F6', '\\u00FC', '\\u0131', '\\u00E2', '\\u00EA', '\\u00EE', '\\u00F4', '\\u00FB')\n"
        break

with open(path, "w", encoding="utf-8") as f:
    f.writelines(lines)
