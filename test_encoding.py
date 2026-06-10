import sys

def try_restore_mojibake():
    path = r"C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Try different encodings
    encodings_to_test = ["windows-1252", "windows-1254", "iso-8859-1", "iso-8859-9"]
    
    for enc in encodings_to_test:
        try:
            # Reverse double encoding:
            # The file is currently UTF-8 string containing 'Ã±'.
            # 'Ã±' in UTF-8 string means the bytes were C3 B1.
            # If we encode the string to latin-1 (or cp1252), we get bytes C3 B1.
            # Then we decode those bytes as UTF-8 to get the original character.
            restored = content.encode(enc).decode('utf-8')
            print(f"Success with {enc}! Sample:")
            # Find a known string
            idx = restored.find("KLAS")
            if idx != -1:
                print(restored[idx:idx+50])
            idx2 = restored.find("KARI")
            if idx2 != -1:
                print(restored[idx2:idx2+50])
            idx3 = restored.find("Zihnin")
            if idx3 != -1:
                print(restored[idx3:idx3+50])
        except Exception as e:
            print(f"Failed with {enc}: {e}")

if __name__ == "__main__":
    try_restore_mojibake()
