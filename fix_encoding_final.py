import os

def check_encoding(file_path):
    try:
        with open(file_path, 'rb') as f:
            content = f.read()
        
        # Try to decode as UTF-8
        try:
            content.decode('utf-8')
            print("File is valid UTF-8")
            return
        except UnicodeDecodeError as e:
            print(f"UTF-8 decode error at position {e.start}: {e.reason}")
            
            # Try to decode as Windows-1254 (Turkish) or ISO-8859-9
            encodings = ['windows-1254', 'iso-8859-9', 'latin-1']
            for enc in encodings:
                try:
                    decoded = content.decode(enc)
                    print(f"File seems to be {enc}. Converting to UTF-8...")
                    with open(file_path, 'w', encoding='utf-8') as f:
                        f.write(decoded)
                    print("Converted successfully.")
                    return
                except:
                    continue
            print("Failed to decode with common encodings.")

    except Exception as e:
        print(f"Error: {e}")

file_path = r'c:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt'
check_encoding(file_path)
