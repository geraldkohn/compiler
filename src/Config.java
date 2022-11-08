public class Config {
    static String grammarPath = ".\\input\\grammar.txt";
//    static String grammarPath = ".\\input\\grammar_test.txt";

//    static String lexInputPath = ".\\input\\1.txt";
//    static String lexInputPath = ".\\input\\2.txt";
//    static String lexInputPath = ".\\input\\3.txt";
//    static String lexInputPath = ".\\input\\4.txt";
    static String lexInputPath = ".\\input\\5.txt";

    static String formulaPath = ".\\output\\文法.txt";
    static String firstTablePath = ".\\output\\First集合.txt";
    static String followTablePath = ".\\output\\Follow集合.txt";
    static String predictMapPath = ".\\output\\分析表.txt";
    static String terminalPath = ".\\output\\终结符.txt";
    static String nonTerminalPath = ".\\output\\非终结符.txt";
    static String lexiconMiddleResult = ".\\output\\词法分析产生的中间结果.txt";
    static String parseResultPath = ".\\result\\语法分析结果.txt";
    static String lexiconResultPath = ".\\result\\词法分析结果.txt";

    static String initSymbol = "program"; // 入口文法！
}
