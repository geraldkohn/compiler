import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainLexicon {
    private static ArrayList<String> lex_result_stack;
    // 词法分析的入口类
    public static void DoLex() {
        String content = TextLexiconInput.input(); // 得到文本内容
        TextLexicon textLexicon = new TextLexicon(content); // 将文本内容给词法编译器
        textLexicon.scannerAll();   // 入口函数
        ArrayList<HashMap<String, String>> lex_error_stack = textLexicon.get_Lex_Error();
        if (lex_error_stack.size() != 0) {  // 错误信息不为空
            System.out.println("词法分析阶段出现错误！");
            for (HashMap<String, String> stringStringHashMap : lex_error_stack) {   // 输出错误信息
                System.out.println(stringStringHashMap);
            }
            return; // 词法分析中断
        }
        ArrayList<ArrayList<String>> threeElements = textLexicon.getThreeElements(); // 得到要输出的三元组
        // 打印
        System.out.println("开始输出词法分析结果: ---------------------");
        for (ArrayList<String> threeElement : threeElements) {
            System.out.println(threeElement.get(0) + " <"
                    + threeElement.get(1) + ","
                    + threeElement.get(2) + ">"
            );
        }
        // 输出到文件中
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.lexiconResultPath));
            for (ArrayList<String> threeElement : threeElements) {
                out.write(threeElement.get(0) + "\t" + "<"
                        + threeElement.get(1) + ","
                        + threeElement.get(2) + ">"
                        + "\n"
                );
            }
            out.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        lex_result_stack = textLexicon.get_Lex_Result();
    }

    public static ArrayList<String> getLex_result_stack() {
        return lex_result_stack;
    }
}
