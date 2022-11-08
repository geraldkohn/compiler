public class Main {
    public static void main(String[] args) {
        MainLexicon.DoLex();    // 词法分析
        TextParseInput.setLex_result_stack(MainLexicon.getLex_result_stack());  // 设置语法分析的输入
        MainParse.DoParse();    // 语法分析
    }
}
