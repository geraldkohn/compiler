import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextLexiconInput {
    // 这个类是处理词法分析输入的，将文件转换成为字符串。
    static String input() {
        String path = Config.lexInputPath;
        String content = "";
        try {
            content = Files.readString(Paths.get(path));
//            System.out.println(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }
}
