import java.nio.file.*;
import java.nio.charset.*;

public class FixEncoding {
    public static void main(String[] args) throws Exception {
        String path = "app/src/main/java/com/mawelly/blitzmath/localization/Strings.kt";
        byte[] corruptedUtf8Bytes = Files.readAllBytes(Paths.get(path));
        
        // The file is currently valid UTF-8, but the characters inside it are the result 
        // of reading original UTF-8 bytes as Windows-1252.
        String corruptedString = new String(corruptedUtf8Bytes, StandardCharsets.UTF_8);
        
        // Convert the string back to bytes using Windows-1252
        byte[] originalBytes = corruptedString.getBytes("windows-1252");
        
        // Now originalBytes should be the valid UTF-8 bytes
        String fixedString = new String(originalBytes, StandardCharsets.UTF_8);
        
        Files.write(Paths.get(path), fixedString.getBytes(StandardCharsets.UTF_8));
        System.out.println("Encoding fixed successfully.");
    }
}
