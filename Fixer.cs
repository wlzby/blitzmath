using System;
using System.IO;
using System.Text;
using System.Text.RegularExpressions;
using System.Collections.Generic;
using Newtonsoft.Json;

public class Fixer {
    public static void Run() {
        string path = @"C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt";
        string text = File.ReadAllText(path, Encoding.UTF8);
        
        string json = File.ReadAllText(@"C:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations.json", Encoding.UTF8);
        var dict = JsonConvert.DeserializeObject<Dictionary<string, Dictionary<string, string>>>(json);
        
        // Find all properties
        string pattern = @"(?:val|fun)\s+([a-zA-Z0-9_]+)[\s\S]*?(?=(?:val|fun)\s+[a-zA-Z0-9_]+|\z)";
        var matches = Regex.Matches(text, pattern);
        
        foreach (Match m in matches) {
            string prop = m.Groups[1].Value;
            if (dict.ContainsKey(prop)) {
                string block = m.Value;
                foreach (var kvp in dict[prop]) {
                    string lang = kvp.Key;
                    string translated = kvp.Value;
                    
                    // Replace the string for this language inside this block
                    string langPattern = $@"AppLanguage\.{lang}\s*->\s*""([^""]*)""";
                    block = Regex.Replace(block, langPattern, $"AppLanguage.{lang} -> \"{translated}\"");
                }
                text = text.Replace(m.Value, block);
            }
        }
        
        // Fix subtitle manually
        text = Regex.Replace(text, @"Zihnin s.*?zorla.*?gï¿½ï¿½ï¿½ï¿½", "Zihnin sınırlarını zorla! 🧠⚡");
        text = Regex.Replace(text, @"AppLanguage\.TURKISH\s*->\s*""CONTINUE""", "AppLanguage.TURKISH -> \"DEVAM ET\"");
        
        File.WriteAllText(path, text, Encoding.UTF8);
        Console.WriteLine("Fixed!");
    }
}
