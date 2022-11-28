import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainParse {
    static HashMap<String, Formula> predictMap;    // 预测表
    static ArrayList<String> input_str;   // 输入串, 词法分析的结果
    static ArrayList<String> symbol_stack;    // 符号栈
//    static ArrayList<String> parse_error_stack;    // 语法分析输出可能的错误结果
    static ArrayList<String> parse_result_stack;    // 语法分析输出展示的结果
    static int parse_result_counter; // 语法分析输出结果的计数器

    // 入口函数
    static void DoParse() {
        input_str = TextParseInput.getLex_result_stack(); // 词法分析的输入
        symbol_stack = new ArrayList<>();
        parse_result_stack = new ArrayList<>();
        parse_result_counter = 0;

        TextParse.Do(); // 生成各种表，First，Follow，预测表
        predictMap = TextParse.predictions; // 预测表

        TextParse.writeAllIntoFile(); // 将语法分析开始前生成的所有表打印出来
        writeLexiconMiddleResultIntoFile(); // 将词法分析的中间结果打印出来

        parse();    // 开始语法分析

        printParseResult();    // 打印语法分析结果
    }

    // 将词法分析传递给语法分析的中间结果打印出来
    static void writeLexiconMiddleResultIntoFile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.lexiconMiddleResult));
            for (String s : input_str) {
                out.write(s + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 利用预测表和词法分析的输入来解析
    // 语法分析
    static void parse() {
        // ------
        // 将 # 压入栈
        symbol_stack.add("#");
        input_str.add("#"); //输入串在最后也需要放 #
        // ------

        // 初始符号压入栈
//        symbol_stack.add("S");
        symbol_stack.add(Config.initSymbol);

        String predictMapKey;   // PredictMap-Key
//        String process="";


        // 符号栈和输入串如果同时为0，那么语法分析结束
//        while (symbol_stack.size()>0 && input_str.size()>0 ) {
        while (true) {
            parse_result_counter++; // 语法分析结果的计数器加一
            if (symbol_stack.get(symbol_stack.size()-1).equals("#") && input_str.get(0).equals("#")) {
                parse_result_stack.add(parse_result_counter + "\t"
                        + "EOF" + "#"
                        + "EOF" + "\t" + "accept");
                break;
            }
            // 输入缓冲区与推导符号串第一个字符相等的话，删掉
            try {
                if(input_str.get(0).equals(symbol_stack.get(symbol_stack.size()-1))){
                    // 语法分析的结果写入栈中
                    parse_result_stack.add(parse_result_counter + "\t"
                            + symbol_stack.get(symbol_stack.size()-1) + "#"
                            + input_str.get(0) + "\t" + "move");
                    input_str.remove(0);    // 输入字符移除第一个，类似于指针向后遍历
                    symbol_stack.remove(symbol_stack.size()-1); // 符号栈移除栈顶
                    continue;
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            // 匹配字符
            predictMapKey = PredictMap.getMapKey(input_str.get(0), symbol_stack.get(symbol_stack.size()-1));

            // 能够找到匹配的
            Formula formula = predictMap.get(predictMapKey);    // 找到文法
            if (formula != null) {  // 文法不为空，为空报错
                // 语法分析的结果写入栈中
                parse_result_stack.add(parse_result_counter + "\t"
                        + symbol_stack.get(symbol_stack.size()-1) + "#"
                        + input_str.get(0) + "\t" + "reduction");
                // 符号栈的最后一个元素如果是 #, 就不能删除了
                if (symbol_stack.get(symbol_stack.size()-1).equals("#")) {
                } else {
                    symbol_stack.remove(symbol_stack.size()-1); // 删除符号栈中最后一个元素
                }
                String[] rights = formula.returnRights();   // 文法的右侧
                if (rights[0].equals("$")) {    // E->$，不能压入空
                    continue;
                }
                for (int i = rights.length-1; i >= 0; i--) {
                    // 将文法右侧的非终结符反向压入栈中
                    symbol_stack.add(rights[i]);
                }
            }

            else {
                // 语法分析的结果写入栈中
                parse_result_stack.add(parse_result_counter + "\t"
                        + symbol_stack.get(symbol_stack.size()-1) + "#"
                        + input_str.get(0) + "\t" + "error");
                return; // 遇到error直接返回
            }
        }
    }

    // 输出语法分析结果
    static void printParseResult() {
        System.out.println("开始输出语法分析结果: --------------------");
        for (String s : parse_result_stack) {
            System.out.println(s);
        }
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.parseResultPath));
            for (String s : parse_result_stack) {
                out.write(s + "\n");
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
