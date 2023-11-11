# 天津大学编译原理大作业

* 完成词法分析
* 完成语法分析

## 代码结构

```bash
compiler
    |- input
        |- grammer.txt
        |- 测试文件
        |- ...
    |- output
        |- First集合.txt
        |- Follow集合.txt
        |- 分析表.txt
        |- 文法.txt
        |- 终结符.txt
        |- 词法分析产生的中间结果.txt
        |- 非终结符.txt
    |- result
        |- gra.tsv
        |- lex.tsv
    |- src
        |- Config.java
        |- TextLexicon.java
        |- TextLexiconInput.java
        |- MainLexicon.java
        |- FirstTable.java
        |- FollowTable.java
        |- PredictMap.java
        |- TextParse.java
        |- TextParseInput.java
        |- MainParse.java
        |- Main.java
    |- README.md
```

Config: 一些配置

TextLexicon: 词法分析的主类

MainLexicon: 词法分析的入口类

TextLexiconInput: 词法分析的输入类

TextParseInput: 语法分析的输入类

TextParse: 语法分析中负责解析文法，生成First表，Follow表，预测表的类

MainParse: 利用语法分析的输入和预测表进行语法分析的类

FirstTable: 生成First表的类

FollowTable: 生成Follow表的类

PredictMap: 生成预测表的类

Formula: 表示文法的类

## 源程序编译步骤

Main.java 为入口文件，从 Main.java 进行编译。

## 一、词法分析器设计

### 1. 实现路径

#### 1.1 实现思路

读取c--文件，存放为字符串类型。遍历整个字符串，将结果存放于三元组数组中，将中间结果也存放于数组中，输出后作为语法分析的输入

#### 1.2 需要实现的单词符号

1. 关键字（KW，不区分大小写）包括： (1) int (2) void (3) return (4) const (5) main
2. 运算符（OP）包括：(6) + (7) - (8) * (9) / (10) % (11) = (12) > (13) < (14) == (15) <= (16) >= (17) != (18) && (19) ||
3. 界符（SE）包括：(20)（ (21) ） (22) { (23) } (24)； (25) ,
4. 标识符（IDN）定义与 C 语言保持相同，为字母、数字和下划线（_）组成的不以数字开头的串
5. 整数（INT）的定义与 C 语言类似，整数由数字串表示

### 2. 算法描述

运用面向对象的编程思想，创建TextLexicon类和MainLexicon类。
由简到繁，先将类的定义规划好，再逐渐扩充完善细节。

#### 2.1 TextLexicon类定义与实现

设置threeElements，lex\_result\_stack，lex\_error\_stack，text\_length，row\_numberKey 六个属性，分别代表要输出的三元组，得出的文本的值，可能出现的错误，输入文本的长度，输入文本行号，关键字。使用面向对象的编程方式来进行词法分析器的编写。

**类中的方法：**

- isAlpha方法

用于判断当前字符是否为字母或下划线。

```java
public int isAlpha(char c){
    if(((c<='z')&&(c>='a')) || ((c<='Z')&&(c>='A')) || (c=='_'))
        return 1;
    else
        return 0;
}
```

- isNumber方法

用于判断当前字符是否为数字。。

```java
public int isNumber(char c) {
    if ((c >= '0') && (c <= '9'))
        return 1;
    else
        return 0;
}
```

- isKey方法

用于判断当前字符串是否为关键字。

```java
public int isKey(String t) {
    for (int i = 0; i < Key.length; i++) {
        if (t.equals(Key[i])) {
            return 1;
        }
    }
    return 0;
}
```

- scannerAll方法

用于遍历读取的整个c--文本字符串。

```java
public void scannerAll() {
    int i = 0;
    char c;
    text = text + '\0';
    while (i < text_length) {
        c = text.charAt(i);
        if (c == ' ' || c == '\t')
            i++;
        else if (c == '\r' || c == '\n') {
            row_number++;
            i++;
        } else
            i = scannerPart(i);
    }
}
```

- scannerPart方法

用于扫描字符串单元。

```java
public int scannerPart(int arg0) {
    int i = arg0;
    char ch = text.charAt(i);
    String s = "";
    // 第一个输入的字符是字母
    if (isAlpha(ch) == 1) {
        s = "" + ch;
        return handleFirstAlpha(i, s);
    }
    // 第一个是数字的话
    else if (isNumber(ch) == 1) {
        s = "" + ch;
        return handleFirstNum(i, s);

    }
    // 既不是既不是数字也不是字母
    else {
        s = "" + ch;
        switch (ch) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
            return ++i;
        case '[':
        case ']':
        case '(':
        case ')':
        case '{':
        case '}':
            printResult(s, "双界符");
            return ++i;
        case ':':
            if (text.charAt(i + 1) == '=') {
                s = s + "=";
                printResult(s, "界符");
                return i + 2;
            } else {
                printError(row_number, s, "不能识别");
                return i + 1;
            }
        case ',':
        case '.':
        case ';':
            printResult(s, "单界符");
            return ++i;
        case '\\':
            if (text.charAt(i + 1) == 'n' || text.charAt(i + 1) == 't' || text.charAt(i + 1) == 'r') {
                printResult(s + text.charAt(i + 1), "转义");
                return i + 2;
            }
        case '\'':
            // 判断是否为单字符，否则报错
            return handleChar(i, s);
        case '\"':
            // 判定字符串
            return handleString(i, s);
        case '+':
            return handlePlus(i, s);
        case '-':
            return handleMinus(i, s);
        case '*':
        case '/':
            if (text.charAt(i + 1) == '*') {
                return handleNote(i, s);
            } else if (text.charAt(i + 1) == '/') {
                return handleSingleLineNote(i, s);
            }
        case '!':
        case '=':
            ch = text.charAt(++i);
            if (ch == '=') {
                // 输出运算符
                s = s + ch;
                printResult(s, "运算符");
                return ++i;
            } else {
                // 输出运算符
                printResult(s, "运算符");
                return i;
            }
        case '>':
            return handleMore(i, s);
        case '<':
            return handleLess(i, s);
        case '%':
            ch = text.charAt(++i);
            if (ch == '=') {
                // 输出运算符
                s = s + ch;
                printResult(s, "运算符");
                return ++i;
            } else if (ch == 's' || ch == 'c' || ch == 'd' || ch == 'f' || ch == 'l') {
                // 输出类型标识符
                s = s + ch;
                printResult(s, "输出类型标识符");
                return ++i;
            } else {
                // 输出求余标识符
                printResult(s, "求余标识符");
                return i;
            }
        default:
            // 输出暂时无法识别的字符,制表符也被当成了有问题的字符
            printError(row_number, s, "暂时无法识别的标识符");
            return ++i;
        }
    }
}
```

- handleFirstAlpha方法

用于处理字符串单元的第一个输入为字母或下划线的情况

```java
public int handleFirstAlpha(int arg, String arg0) {
    int i = arg;
    String s = arg0;
    char ch = text.charAt(++i);
    while (isAlpha(ch) == 1 || isNumber(ch) == 1) {
        s = s + ch;
        ch = text.charAt(++i);
    }
    // if(s.length()==1){
    // printResult(s, "字符常数");
    // return i;
    // }
    // 到了结尾
    if (isKey(s) == 1) {
        // 输出key
        printResult(s, "关键字");
        return i;

    } else {
        // 输出普通的标识符
        printResult(s, "标识符");
        return i;
    }
}
```

- handleFirstNum方法

用于处理字符串单元的第一个输入为数字的情况

```java
public int handleFirstNum(int arg, String arg0) {
    int i = arg;
    char ch = text.charAt(++i);
    String s = arg0;
    while (isNumber(ch) == 1) {
        s = s + ch;
        ch = text.charAt(++i);
    }
    if ((text.charAt(i) == ' ') || (text.charAt(i) == '\t') || (text.charAt(i) == '\n') || (text.charAt(i) == '\r')
            || (text.charAt(i) == '\0') || ch == ';' || ch == ',' || ch == ')' || ch == ']' || ch == '['
            || ch == '(') {
        // 到了结尾，输出数字
        printResult(s, "整数");
        return i;
    } else if (ch == 'E') {
        if (text.charAt(i + 1) == '+') {
            s = s + ch;
            ch = text.charAt(++i);
            s = s + ch;
            ch = text.charAt(++i);
            while (isNumber(ch) == 1) {
                s = s + ch;
                ch = text.charAt(++i);
            }
            if (ch == '\r' || ch == '\n' || ch == ';' || ch == '\t') {
                printResult(s, "科学计数");
                return ++i;
            } else {
                printError(i, s, "浮点数错误");
                return i;
            }
        } else if (isNumber(text.charAt(i + 1)) == 1) {
            s = s + ch;
            ch = text.charAt(++i);
            while (isNumber(ch) == 1) {
                s = s + ch;
                ch = text.charAt(++i);
            }
            if (ch == '\r' || ch == '\n' || ch == ';' || ch == '\t') {
                printResult(s, "科学计数");
                return ++i;
            } else {
                printError(row_number, s, "浮点数错误");
                return i;
            }
        } else {
            printError(row_number, s, "科学计数法错误");
            return ++i;
        }
    }

    // 浮点数判断
    else if (text.charAt(i) == '.' && (isNumber(text.charAt(i + 1)) == 1)) {
        s = s + '.';
        ch = text.charAt(++i);
        while (isNumber(ch) == 1) {
            s = s + ch;
            ch = text.charAt(++i);
        }
        if (ch == 'E') {
            if (text.charAt(i + 1) == '+') {
                s = s + ch;
                ch = text.charAt(++i);
                s = s + ch;
                ch = text.charAt(++i);
                while (isNumber(ch) == 1) {
                    s = s + ch;
                    ch = text.charAt(++i);
                }
                if (ch == '\r' || ch == '\n' || ch == ';' || ch == '\t') {
                    printResult(s, "科学计数");
                    return ++i;
                } else {
                    printError(i, s, "浮点数错误");
                    return i;
                }
            } else if (isNumber(text.charAt(i + 1)) == 1) {
                s = s + ch;
                ch = text.charAt(++i);
                while (isNumber(ch) == 1) {
                    s = s + ch;
                    ch = text.charAt(++i);
                }
                if (ch == '\r' || ch == '\n' || ch == ';' || ch == '\t') {
                    printResult(s, "科学计数");
                    return ++i;
                } else {
                    printError(row_number, s, "浮点数错误");
                    return i;
                }
            } else {
                printError(row_number, s, "科学计数法错误");
                return ++i;
            }
        } else if (ch == '\n' || ch == '\r' || ch == '\t' || ch == ' ' || ch == '\0' || ch != ',' || ch != ';') {
            printResult(s, "浮点数");
            return i;
        } else if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '\0') {
            printResult(s, "浮点数");
            return i;
        } else {
            while (ch != '\n' && ch != '\t' && ch != ' ' && ch != '\r' && ch != '\0' && ch != ';' && ch != '.'
                    && ch != ',') {
                s = s + ch;
                ch = text.charAt(++i);
            }
            printError(row_number, s, "不合法的字符");
            return i;
        }
    } else if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '\0') {
        printResult(s, "整数");
        return i;
    } else {
        do {
            ch = text.charAt(i++);
            s = s + ch;
        } while ((text.charAt(i) != ' ') && (text.charAt(i) != '\t') && (text.charAt(i) != '\n')
                && (text.charAt(i) != '\r') && (text.charAt(i) != '\0'));
        printError(row_number, s, "错误的标识符");
        return i;
    }
}
```

- printResult方法

用于将中间结果和最终结果添加到lex_result_stack数组和threeElements数组中。

```java
public void printResult(String rs_value, String rs_name) {
    if (rs_name.equals("标识符")) {
        lex_result_stack.add("Ident");
        threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "IDN", rs_value)));
    } else if (rs_name.equals("整数")) {
        lex_result_stack.add("INT");
        threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "INT", rs_value)));
    } else if (rs_name.equals("科学计数") || rs_name.equals("浮点数")) {
        lex_result_stack.add("float");
        threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "FLOAT", rs_value)));
    } else if (rs_name.equals("单字符")) {
        lex_result_stack.add("char");
        threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "CHAR", rs_value)));
    } else if (rs_name.equals("字符串")) {
        lex_result_stack.add("str");
        threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "STR", rs_value)));
    } else if (rs_name.equals("运算符")) {
        lex_result_stack.add(rs_value);
        if (rs_value.equals("+")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "6")));
        } else if (rs_value.equals("-")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "7")));
        } else if (rs_value.equals("*")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "8")));
        } else if (rs_value.equals("/")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "9")));
        } else if (rs_value.equals("%")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "10")));
        } else if (rs_value.equals("=")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "11")));
        } else if (rs_value.equals(">")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "12")));
        } else if (rs_value.equals("<")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "13")));
        } else if (rs_value.equals("==")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "14")));
        } else if (rs_value.equals("<=")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "15")));
        } else if (rs_value.equals(">=")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "16")));
        } else if (rs_value.equals("!=")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "17")));
        } else if (rs_value.equals("&&")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "18")));
        } else if (rs_value.equals("||")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "OP", "19")));
        }
    } else if (rs_name.equals("单界符") || rs_name.equals("双界符")) {
        lex_result_stack.add(rs_value);
        if (rs_value.equals("(")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "SE", "20")));
        } else if (rs_value.equals(")")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "SE", "21")));
        } else if (rs_value.equals("{")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "SE", "22")));
        } else if (rs_value.equals("}")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "SE", "23")));
        } else if (rs_value.equals(";")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "SE", "24")));
        } else if (rs_value.equals(",")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "SE", "25")));
        }
    } else if (rs_name.equals("关键字")) {
        lex_result_stack.add(rs_value);
        if (rs_value.equals("int")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "KW", "1")));
        } else if (rs_value.equals("void")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "KW", "2")));
        } else if (rs_value.equals("return")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "KW", "3")));
        } else if (rs_value.equals("const")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "KW", "4")));
        } else if (rs_value.equals("main")) {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "KW", "5")));
        } else {
            threeElements.add(new ArrayList<>(Arrays.asList(rs_value, "KW", rs_value)));
        }
    }
}
```

- printError方法

用于打印错误信息。

```java
public void printError(int row_num, String rs_value, String rs_name) {
    HashMap<String, String> hashMap = new HashMap<String, String>();
    hashMap.put("行号：", row_num + "");
    hashMap.put("输入：", rs_value);
    hashMap.put("错误类型: ", rs_name);
    lex_error_stack.add(hashMap);
    // tbModel_lex_result.addRow(new String[]{"ERROR，"+rs_name, rs_value});
}
```

#### 2.2MainLexicon类的定义与实现

此类主要定义了词法分析的入口函数，用于打印词法分析结果及将结果输出到文件中。

```java
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
            out.write(threeElement.get(0) + " <"
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
```

## 二、语法分析器设计

1. 解析文法，找到终结符和非终结符

2. 构造First集合

3. 构造Follow集合

4. 根据First集合和Follow集合构造预测表

5. 根据预测表和词法分析的结果进行语法分析


### 1. 解析文法

Formula 是表示文法的类，其中的left字段表示文法的左值，right表示文法的右值。有一个初始化方法，和两个gettr方法。

```java
class Formula {
    String left;
    String[] right;
    public Formula(String left, String[] right){
        this.left = left;
        this.right = right;
    }

    public String[] returnRights(){
        return right;
    }

    public String returnLeft(){
        return left;
    }
}
```

TextParse 类是语法分析类，主要的工作就是根据语法规则来解析出语法分析需要的各种资源，包括文法，终结符，非终结符，First表，Follow表，预测表。

setFormulas() 这个方法用于从文法文件中解析出文法规则。解析的规则如下 left -> []right。具体代码如下：

```java
public static void setFormulas() {
        try {
            File file = new File(Config.grammarPath);
            RandomAccessFile randomfile = new RandomAccessFile(file, "r");
            String line;
            String left;
            String right;
            Formula formula;
            while ((line=randomfile.readLine())!=null) {
//                System.out.println(line);
//                System.out.println("split: " + Arrays.toString(line.split("->")));
                left = line.split("->")[0].trim();
                right = line.split("->")[1].trim();    // 将右侧所有的值都算进去
                formula = new Formula(left, right.split(" ")); // 根据空格分离右侧的值
                formulas.add(formula);
            }
            randomfile.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
```

解析文法中的非终结符，并将其存储到 ArrayList< String > 中。因为文法中的非终结符就是文法左侧的全部符号，只需要统计左侧就可以了。

```java
    // 解析文法中的非终结符，并设置
    // 非终结符在文法中就是所有的left
    static void setNonTerminals() {
        for (Formula formula : formulas) {
            if (nonTerminals.contains(formula.left)) {
                continue;
            } else {
                nonTerminals.add(formula.left);
            }
        }
    }
```

解析文法中的终结符，并将其存储到 ArrayList < String > 中。文法中的终结符是文法中全部的符号去掉终结符。

```java
// 生成终结符
    static void setTerminals() {
        for (Formula formula : formulas) {
            String[] rights = formula.returnRights();
            // 从右侧去掉非终结符，剩下的就是终结符
            for (String s : rights) {
                // 去掉非终结符和空
                if (nonTerminals.contains(s) || s.equals("$")) {
                    continue;
                } else { // 剩下的就是终结符
                    terminals.add(s);
                }
            }
        }
    }
```

### 2. 构造 First 集合

根据解析出的文法，终结符，非终结符来推导出 First 集合。根据以下算法来构造 First 集合。

```java
// 生成 First 集合
static void setFirsts() {
    FirstTable.setFirst(formulas,terminals,nonTerminals,firsts);
}
```

- 使用 HashMap 来存储 First 集合，Key 值是符号，Value 值是ArrayList< String> ，存储着 Key 的 First 集合中所有元素。

- 全部终结符号的 First 集合就是终结符本身。

- 将全部非终结符都注册一个 Map，方便后序代码。

- 遍历文法右侧的每一个符号的First集合，然后将该符号的First集合去掉空加入到左侧文法的First集合中。因为 Java 是按照引用来传递的，这个过程就可以看作一个递归过程。


```java
public class FirstTable {
    static ArrayList<Formula> formulas;
    static ArrayList<String> terminals;
    static ArrayList<String> nonTerminals;
    static HashMap<String, ArrayList<String>> firsts;

    static void setFirst(ArrayList<Formula> _formulas, ArrayList<String> _terminals,
                         ArrayList<String> _nonTerminals, HashMap<String, ArrayList<String>> _firsts) {
        formulas = _formulas;
        terminals = _terminals;
        nonTerminals = _nonTerminals;
        firsts = _firsts;

        // 终结符全部求出first集
        ArrayList<String> first;
        for (String terminal : terminals) {
            first = new ArrayList<String>();
            first.add(terminal);
            firsts.put(terminal, first);
        }
        // 给所有非终结符注册一下
        for (String nonterminal : nonTerminals) {
            first = new ArrayList<String>();
            firsts.put(nonterminal, first);
        }

        boolean flag;
        while (true) {
            flag = true;
            String left;
            String right;
            String[] rights;
            // 遍历所有文法
            for (Formula formula : formulas) {
                left = formula.returnLeft();
                rights = formula.returnRights();
                // 每个文法的右侧
                for (String s : rights) {
                    right = s;
                    // oneOfRight是否存在，遇到空怎么办
                    if (!right.equals("$")) {   // 右侧的字符串不为空（任意一个都不为空）
                        // 遍历每一个右侧字符串的First集合，即 First(右侧字符串)
                        // 这个就类似于一个递归
                        for (int l = 0; l < firsts.get(right).size(); l++) {
                            // First(left) 包括了 First(oneOfRights)
                            if (firsts.get(left).contains(firsts.get(right).get(l))) {
                                continue;
                            } else {
                                // 不包括, 就加入
                                firsts.get(left).add(firsts.get(right).get(l));
                                flag = false;
                            }
                        }
                    }
                    // OneOfRights -> $
                    if (isCanBeNull(formulas, right)) {
                        continue;
                    } else {
                        break;
                    }
                }
            }
            if (flag) {
                break;
            }
        }

    // 判断是否产生$
    static boolean isCanBeNull(ArrayList<Formula> formulas, String symbol){
        String[] rights;
        // 遍历每一个文法
        for (Formula formula : formulas) {
            // 找到产生式
            if (formula.returnLeft().equals(symbol)) {
                // symbol -> [rights]
                rights = formula.returnRights();
                // symbol -> $
                if (rights[0].equals("$")) {    // 第一个就是$, 即类似于 E -> $
                    return true;
                }
            }
        }
        return false;
    }
}
```

### 3. 构造 Follow 集合

根据解析出的文法，终结符，非终结符。通过以下算法来推导出 Follow 集合。

```java
   // 生成 Follow 集合
   static void setFollows() {
        FollowTable.setFollow(formulas,terminals,nonTerminals,firsts,follows);
   }
```

- 将文法开始符号 program 置于 Follow(program)。

- 将最后一个元素的 First 集合去掉空之后加入到文法右侧前一个元素的 Follow 集合中。

- 将文法左侧的 Follow 集合加入到文法右侧最后一个 First 集合中没有空的符号的 Follow 集合中。


```java
public class FollowTable {
    static void setFollow(ArrayList<Formula> formulas, ArrayList<String> terminals,
                          ArrayList<String> nonTerminals, HashMap<String, ArrayList<String>> firsts,
                          HashMap<String, ArrayList<String>> follows) {
        // 所有非终结符的follow集初始化一下
        ArrayList<String> follow;
        for (String nonTerminal : nonTerminals) {
            follow = new ArrayList<String>();
            follows.put(nonTerminal, follow);
        }
        // 将#加入到follow(S)中
//        follows.get("S").add("#");  // 开始文法的入口必须是S
        follows.get(Config.initSymbol).add("#");

        boolean flag;
        boolean fab;
        while (true) {
            flag = true;
            // 循环
            for (Formula formula : formulas) {
                String left;
                String right;
                String[] rights;
                rights = formula.returnRights();
                for (int j = 0; j < rights.length; j++) {
                    right = rights[j];

                    // 非终结符
                    if (nonTerminals.contains(right)) {
                        fab = true;
                        for (int k = j + 1; k < rights.length; k++) {

                            // 查找first集
                            for (int v = 0; v < firsts.get(rights[k]).size(); v++) {
                                // 将后一个元素的first集加入到前一个元素的follow集中
                                if (follows.get(right).contains(firsts.get(rights[k]).get(v))) {
                                    continue;
                                } else {
                                    follows.get(right).add(firsts.get(rights[k]).get(v));
                                    flag = false;
                                }
                            }
                            if (isCanBeNull(formulas, rights[k])) {
                                continue;
                            } else {
                                fab = false;
                                break;
                            }
                        }
                        if (fab) {
                            left = formula.returnLeft();
                            for (int p = 0; p < follows.get(left).size(); p++) {
                                if (follows.get(right).contains(follows.get(left).get(p))) {
                                    continue;
                                } else {
                                    follows.get(right).add(follows.get(left).get(p));
                                    flag = false;
                                }
                            }
                        }
                    }

                }
            }
            if(flag){
                break;
            }
        }

        // 清除follow集中的#
        String left;
        for (String nonterminal : nonTerminals) {
            left = nonterminal;
            for (int v = 0; v < follows.get(left).size(); v++) {
                if (follows.get(left).get(v).equals("#"))
                    follows.get(left).remove(v);
            }
        }

        // -------------------
        // 为Follow加上#
        for (String notTerminal : nonTerminals) {
            follows.get(notTerminal).add("#");
        }
        // -------------------
    }

    // 判断是否产生$
    static boolean isCanBeNull(ArrayList<Formula> formulas, String symbol){
        String[] rights;
        for (Formula formula : formulas) {
            // 找到产生式
            if (formula.returnLeft().equals(symbol)) {
                rights = formula.returnRights();
                if (rights[0].equals("$")) {
                    return true;
                }
            }
        }
        return false;
    }
}
```

### 4. 构造预测表

```java
    static void setPrediction() {
        PredictMap.setPrediction(formulas,terminals,nonTerminals,firsts,follows,predictions);
    }
```

- 遍历每一个文法

- 将文法左侧符号的 First 集合中的每一个终结符作为横坐标，左侧符号作为纵坐标，填上这个文法。

- 如果左侧文法符号的 First 集合中包含空，则将文法左侧的 Follow 集合的每一个终结符作为横坐标，左侧符号作为纵坐标，填上这个文法。


```java
public class PredictMap {
    static void setPrediction(ArrayList<Formula> formulas, ArrayList<String> terminals,
                              ArrayList<String> nonTerminals, HashMap<String, ArrayList<String>> firsts,
                              HashMap<String, ArrayList<String>> follows,
                              HashMap<String, Formula> predictions) {
        // (2)
        for (Formula formula : formulas) {
            // First(formula.right[0])
            try {
                if (formula.right[0].equals("$")) { // 类似于 value' -> $ 这种文法
                    // First集合中First($)是不存在的
                    // 遇到这种文法，直接跳过就行
                    continue;
                }
                for (String terminalInFirsts : firsts.get(formula.right[0])) {
                    // 空
                    if (terminalInFirsts.equals("$")) {
                        // Follow(formula.left)
                        for (String terminalInFollows : follows.get(formula.left)) {
                            predictions.put(getMapKey(terminalInFollows, formula.left),
                                    new Formula(formula.left, new String[]{"$"}));
                        }
                    }
                    // 不空
                    // [Terminal, notTerminal] : formula
                    predictions.put(getMapKey(terminalInFirsts, formula.left), formula);
                }
            } catch (Exception e) {
                System.out.println("first结合中没有 key: " + formula.right[0]);
                e.printStackTrace();
            }
        }

        // (3)
        // E -> $
        for (Formula formula : formulas) {
            if (formula.returnRights()[0].equals("$")) {    // E -> $
                for (String followElement : follows.get(formula.returnLeft())) { // Follow(E)
                    // [FollowElement(E), E] : E - > $
                    predictions.put(getMapKey(followElement, formula.returnLeft()), formula);
                }
            }
        }
    }

    // 以固定的格式产生分析表的 Key
    static String getMapKey(String terminal, String nonTerminal) {
        // i 为终结符，横坐标
        // j 为非终结符，纵坐标
        return  "{横坐标: " + terminal + " , " + "纵坐标: " + nonTerminal + "}";
    }
}
```

### 5. 语法分析

- 根据上图来进行语法分析，不断的来进行移进规约。

- 首先要在压栈一个#，然后在输入串的末尾压入一个#

- 然后严格依照图中的遍历规则，来进行移进规约，直到 # 遇到 # 就结束。


```java
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
            out.write("词法分析的中间结果结果如下: --------------------\n");
            out.write("总共有 " + input_str.size() + " 条数据\n");
            out.write("\n");
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
                parse_result_stack.add(parse_result_counter + " "
                        + "EOF" + "#"
                        + "EOF" + " " + "accept");
                break;
            }
            // 输入缓冲区与推导符号串第一个字符相等的话，删掉
            try {
                if(input_str.get(0).equals(symbol_stack.get(symbol_stack.size()-1))){
                    // 语法分析的结果写入栈中
                    parse_result_stack.add(parse_result_counter + " "
                            + symbol_stack.get(symbol_stack.size()-1) + "#"
                            + input_str.get(0) + " " + "move");
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
                parse_result_stack.add(parse_result_counter + " "
                        + symbol_stack.get(symbol_stack.size()-1) + "#"
                        + input_str.get(0) + " " + "reduction");
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
                parse_result_stack.add(parse_result_counter + " "
                        + symbol_stack.get(symbol_stack.size()-1) + "#"
                        + input_str.get(0) + " " + "error");
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
```

## 三、输出的一些表

### 1. 文法

```
文法解析结果如下: --------------------
总共有 77 条数据

文法左侧: program 文法右侧: [compUnit]
文法左侧: compUnit 文法右侧: [decl, compUnit]
文法左侧: compUnit 文法右侧: [funcDef, compUnit]
文法左侧: compUnit 文法右侧: [$]
文法左侧: decl 文法右侧: [constDecl]
文法左侧: decl 文法右侧: [varDecl]
文法左侧: constDecl 文法右侧: [const, bType, constDef, argConst, ;]
文法左侧: argConst 文法右侧: [,, constDef, argConst]
文法左侧: argConst 文法右侧: [$]
文法左侧: constDef 文法右侧: [Ident, =, constInitVal]
文法左侧: constInitVal 文法右侧: [constExp]
文法左侧: varDecl 文法右侧: [bType, varDef, argVarDecl, ;]
文法左侧: argVarDecl 文法右侧: [,, varDef, argVarDecl]
文法左侧: argVarDecl 文法右侧: [$]
文法左侧: varDef 文法右侧: [Ident, argVarDef]
文法左侧: argVarDef 文法右侧: [=, initVal]
文法左侧: argVarDef 文法右侧: [$]
文法左侧: initVal 文法右侧: [exp]
文法左侧: bType 文法右侧: [int]
文法左侧: funcDef 文法右侧: [funcType, Ident, (, funcFParams, ), block]
文法左侧: funcType 文法右侧: [void]
文法左侧: funcFParams 文法右侧: [funcFParam, argFunctionF]
文法左侧: funcFParams 文法右侧: [$]
文法左侧: argFunctionF 文法右侧: [,, funcFParam, argFunctionF]
文法左侧: argFunctionF 文法右侧: [$]
文法左侧: funcFParam 文法右侧: [bType, Ident]
文法左侧: block 文法右侧: [{, blockItem, }]
文法左侧: blockItem 文法右侧: [decl, blockItem]
文法左侧: blockItem 文法右侧: [stmt, blockItem]
文法左侧: blockItem 文法右侧: [$]
文法左侧: stmt 文法右侧: [exp, ;]
文法左侧: stmt 文法右侧: [;]
文法左侧: stmt 文法右侧: [block]
文法左侧: stmt 文法右侧: [return, argExp, ;]
文法左侧: argExp 文法右侧: [$]
文法左侧: argExp 文法右侧: [exp]
文法左侧: exp 文法右侧: [assignExp]
文法左侧: lVal 文法右侧: [Ident]
文法左侧: primaryExp 文法右侧: [(, exp, )]
文法左侧: primaryExp 文法右侧: [number]
文法左侧: number 文法右侧: [INT]
文法左侧: unaryOp 文法右侧: [+]
文法左侧: unaryOp 文法右侧: [-]
文法左侧: unaryOp 文法右侧: [!]
文法左侧: unaryExp 文法右侧: [unaryOp, unaryExp]
文法左侧: unaryExp 文法右侧: [Ident, callFunc]
文法左侧: callFunc 文法右侧: [(, funcRParams, )]
文法左侧: callFunc 文法右侧: [$]
文法左侧: unaryExp 文法右侧: [primaryExp]
文法左侧: funcRParams 文法右侧: [funcRParam, argFunctionR]
文法左侧: funcRParams 文法右侧: [$]
文法左侧: argFunctionR 文法右侧: [,, funcRParam, argFunctionR]
文法左侧: argFunctionR 文法右侧: [$]
文法左侧: funcRParam 文法右侧: [exp]
文法左侧: mulExp 文法右侧: [unaryExp, mulExpAtom]
文法左侧: mulExpAtom 文法右侧: [*, unaryExp, mulExpAtom]
文法左侧: mulExpAtom 文法右侧: [/, unaryExp, mulExpAtom]
文法左侧: mulExpAtom 文法右侧: [%, unaryExp, mulExpAtom]
文法左侧: mulExpAtom 文法右侧: [$]
文法左侧: addExp 文法右侧: [mulExp, addExpAtom]
文法左侧: addExpAtom 文法右侧: [+, mulExp, addExpAtom]
文法左侧: addExpAtom 文法右侧: [-, mulExp, addExpAtom]
文法左侧: addExpAtom 文法右侧: [$]
文法左侧: relExp 文法右侧: [addExp, relExpAtom]
文法左侧: relExpAtom 文法右侧: [<, addExp, relExpAtom]
文法左侧: relExpAtom 文法右侧: [>, addExp, relExpAtom]
文法左侧: relExpAtom 文法右侧: [<=, addExp, relExpAtom]
文法左侧: relExpAtom 文法右侧: [>=, addExp, relExpAtom]
文法左侧: relExpAtom 文法右侧: [$]
文法左侧: eqExp 文法右侧: [relExp, eqExpAtom]
文法左侧: eqExpAtom 文法右侧: [==, relExp, eqExpAtom]
文法左侧: eqExpAtom 文法右侧: [!=, relExp, eqExpAtom]
文法左侧: eqExpAtom 文法右侧: [$]
文法左侧: assignExp 文法右侧: [eqExp, assignExpAtom]
文法左侧: assignExpAtom 文法右侧: [=, eqExp, assignExpAtom]
文法左侧: assignExpAtom 文法右侧: [$]
文法左侧: constExp 文法右侧: [assignExp]
```

### 2. 终结符

```
从文法中解析的终结符结果如下: --------------------
总共有 45 条数据

const
;
,
Ident
=
;
,
Ident
=
int
Ident
(
)
void
,
Ident
{
}
;
;
return
;
Ident
(
)
INT
+
-
!
Ident
(
)
,
*
/
%
+
-
<
>
<=
>=
==
!=
=
```

### 3. 非终结符

```
从文法中解析的非终结符结果如下: --------------------
总共有 43 条数据

program
compUnit
decl
constDecl
argConst
constDef
constInitVal
varDecl
argVarDecl
varDef
argVarDef
initVal
bType
funcDef
funcType
funcFParams
argFunctionF
funcFParam
block
blockItem
stmt
argExp
exp
lVal
primaryExp
number
unaryOp
unaryExp
callFunc
funcRParams
argFunctionR
funcRParam
mulExp
mulExpAtom
addExp
addExpAtom
relExp
relExpAtom
eqExp
eqExpAtom
assignExp
assignExpAtom
constExp
```

### 4. First 集合

```
First列表结果如下: --------------------
总共有 68 条数据

argFunctionR   [,]
<=   [<=]
decl   [const, int]
constInitVal   [+, -, !, Ident, (, INT]
constDef   [Ident]
compUnit   [const, void, int]
addExp   [+, -, !, Ident, (, INT]
unaryOp   [+, -, !]
program   [const, void, int]
addExpAtom   [+, -]
mulExpAtom   [*, /, %]
relExp   [+, -, !, Ident, (, INT]
argVarDecl   [,]
number   [INT]
eqExp   [+, -, !, Ident, (, INT]
funcFParams   [int]
block   [{]
mulExp   [+, -, !, Ident, (, INT]
argExp   [+, -, !, Ident, (, INT]
exp   [+, -, !, Ident, (, INT]
constExp   [+, -, !, Ident, (, INT]
==   [==]
!   [!]
void   [void]
assignExp   [+, -, !, Ident, (, INT]
%   [%]
lVal   [Ident]
(   [(]
)   [)]
*   [*]
assignExpAtom   [=]
+   [+]
,   [,]
-   [-]
/   [/]
bType   [int]
unaryExp   [+, -, !, Ident, (, INT]
varDef   [Ident]
primaryExp   [(, INT]
;   [;]
blockItem   [const, ;, {, return, int, +, -, !, Ident, (, INT]
<   [<]
!=   [!=]
=   [=]
>   [>]
>=   [>=]
funcDef   [void]
eqExpAtom   [==, !=]
const   [const]
funcRParam   [+, -, !, Ident, (, INT]
INT   [INT]
funcRParams   [+, -, !, Ident, (, INT]
initVal   [+, -, !, Ident, (, INT]
argConst   [,]
funcType   [void]
Ident   [Ident]
relExpAtom   [<, >, <=, >=]
constDecl   [const]
callFunc   [(]
int   [int]
argVarDef   [=]
funcFParam   [int]
{   [{]
argFunctionF   [,]
}   [}]
varDecl   [int]
return   [return]
stmt   [;, {, return, +, -, !, Ident, (, INT]
```

### 5. Follow 集合

```
Follow列表结果如下: --------------------
总共有 43 条数据

argFunctionR   [), #]
eqExpAtom   [=, ,, ;, ), #]
decl   [const, void, int, ;, {, return, +, -, !, Ident, (, INT, }, #]
constInitVal   [,, ;, #]
constDef   [,, ;, #]
compUnit   [#]
funcRParam   [,, ), #]
addExp   [<, >, <=, >=, ==, !=, =, ,, ;, ), #]
unaryOp   [+, -, !, Ident, (, INT, #]
program   [#]
addExpAtom   [<, >, <=, >=, ==, !=, =, ,, ;, ), #]
mulExpAtom   [+, -, <, >, <=, >=, ==, !=, =, ,, ;, ), #]
relExp   [==, !=, =, ,, ;, ), #]
funcRParams   [), #]
argVarDecl   [;, #]
initVal   [,, ;, #]
number   [*, /, %, +, -, <, >, <=, >=, ==, !=, =, ,, ;, ), #]
argConst   [;, #]
eqExp   [=, ,, ;, ), #]
funcFParams   [), #]
block   [const, void, int, ;, {, return, +, -, !, Ident, (, INT, }, #]
mulExp   [+, -, <, >, <=, >=, ==, !=, =, ,, ;, ), #]
argExp   [;, #]
exp   [,, ;, ), #]
constExp   [,, ;, #]
funcType   [Ident, #]
assignExp   [,, ;, ), #]
relExpAtom   [==, !=, =, ,, ;, ), #]
lVal   [#]
constDecl   [const, void, int, ;, {, return, +, -, !, Ident, (, INT, }, #]
assignExpAtom   [,, ;, ), #]
callFunc   [*, /, %, +, -, <, >, <=, >=, ==, !=, =, ,, ;, ), #]
bType   [Ident, #]
unaryExp   [*, /, %, +, -, <, >, <=, >=, ==, !=, =, ,, ;, ), #]
argVarDef   [,, ;, #]
funcFParam   [,, ), #]
varDef   [,, ;, #]
primaryExp   [*, /, %, +, -, <, >, <=, >=, ==, !=, =, ,, ;, ), #]
blockItem   [}, #]
argFunctionF   [), #]
varDecl   [const, void, int, ;, {, return, +, -, !, Ident, (, INT, }, #]
stmt   [const, ;, {, return, int, +, -, !, Ident, (, INT, }, #]
funcDef   [const, void, int, #]
```

### 6. 分析表

```
预测表结果如下: --------------------
总共有 217 条数据

{横坐标: Ident   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: !   纵坐标: relExp}    文法: relExp->[addExp, relExpAtom]
{横坐标: #   纵坐标: funcFParams}    文法: funcFParams->[$]
{横坐标: void   纵坐标: funcType}    文法: funcType->[void]
{横坐标: const   纵坐标: constDecl}    文法: constDecl->[const, bType, constDef, argConst, ;]
{横坐标: <   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: const   纵坐标: compUnit}    文法: compUnit->[decl, compUnit]
{横坐标: +   纵坐标: unaryExp}    文法: unaryExp->[unaryOp, unaryExp]
{横坐标: )   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: )   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: >=   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: -   纵坐标: stmt}    文法: stmt->[exp, ;]
{横坐标: (   纵坐标: constInitVal}    文法: constInitVal->[constExp]
{横坐标: +   纵坐标: mulExp}    文法: mulExp->[unaryExp, mulExpAtom]
{横坐标: !   纵坐标: funcRParams}    文法: funcRParams->[funcRParam, argFunctionR]
{横坐标: ,   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: Ident   纵坐标: addExp}    文法: addExp->[mulExp, addExpAtom]
{横坐标: +   纵坐标: constExp}    文法: constExp->[assignExp]
{横坐标: Ident   纵坐标: lVal}    文法: lVal->[Ident]
{横坐标: ;   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: ,   纵坐标: argFunctionF}    文法: argFunctionF->[,, funcFParam, argFunctionF]
{横坐标: !=   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: #   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: +   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: >   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: +   纵坐标: unaryOp}    文法: unaryOp->[+]
{横坐标: }   纵坐标: blockItem}    文法: blockItem->[$]
{横坐标: !=   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: !   纵坐标: eqExp}    文法: eqExp->[relExp, eqExpAtom]
{横坐标: Ident   纵坐标: varDef}    文法: varDef->[Ident, argVarDef]
{横坐标: -   纵坐标: argExp}    文法: argExp->[exp]
{横坐标: (   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: (   纵坐标: primaryExp}    文法: primaryExp->[(, exp, )]
{横坐标: INT   纵坐标: addExp}    文法: addExp->[mulExp, addExpAtom]
{横坐标: )   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: const   纵坐标: program}    文法: program->[compUnit]
{横坐标: !   纵坐标: assignExp}    文法: assignExp->[eqExp, assignExpAtom]
{横坐标: int   纵坐标: decl}    文法: decl->[varDecl]
{横坐标: void   纵坐标: compUnit}    文法: compUnit->[funcDef, compUnit]
{横坐标: #   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: %   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: ,   纵坐标: argConst}    文法: argConst->[,, constDef, argConst]
{横坐标: INT   纵坐标: assignExp}    文法: assignExp->[eqExp, assignExpAtom]
{横坐标: (   纵坐标: funcRParam}    文法: funcRParam->[exp]
{横坐标: (   纵坐标: relExp}    文法: relExp->[addExp, relExpAtom]
{横坐标: INT   纵坐标: unaryExp}    文法: unaryExp->[primaryExp]
{横坐标: ;   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: #   纵坐标: eqExpAtom}    文法: eqExpAtom->[$]
{横坐标: ;   纵坐标: eqExpAtom}    文法: eqExpAtom->[$]
{横坐标: <   纵坐标: relExpAtom}    文法: relExpAtom->[<, addExp, relExpAtom]
{横坐标: ;   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: -   纵坐标: funcRParam}    文法: funcRParam->[exp]
{横坐标: (   纵坐标: funcRParams}    文法: funcRParams->[funcRParam, argFunctionR]
{横坐标: +   纵坐标: funcRParam}    文法: funcRParam->[exp]
{横坐标: >   纵坐标: relExpAtom}    文法: relExpAtom->[>, addExp, relExpAtom]
{横坐标: #   纵坐标: blockItem}    文法: blockItem->[$]
{横坐标: >=   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: +   纵坐标: addExp}    文法: addExp->[mulExp, addExpAtom]
{横坐标: Ident   纵坐标: initVal}    文法: initVal->[exp]
{横坐标: )   纵坐标: funcFParams}    文法: funcFParams->[$]
{横坐标: #   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: *   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: ,   纵坐标: assignExpAtom}    文法: assignExpAtom->[$]
{横坐标: Ident   纵坐标: mulExp}    文法: mulExp->[unaryExp, mulExpAtom]
{横坐标: >   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: (   纵坐标: unaryExp}    文法: unaryExp->[primaryExp]
{横坐标: {   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: INT   纵坐标: argExp}    文法: argExp->[exp]
{横坐标: #   纵坐标: argVarDef}    文法: argVarDef->[$]
{横坐标: {   纵坐标: block}    文法: block->[{, blockItem, }]
{横坐标: !   纵坐标: constExp}    文法: constExp->[assignExp]
{横坐标: >   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: -   纵坐标: constInitVal}    文法: constInitVal->[constExp]
{横坐标: ;   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: -   纵坐标: assignExp}    文法: assignExp->[eqExp, assignExpAtom]
{横坐标: %   纵坐标: mulExpAtom}    文法: mulExpAtom->[%, unaryExp, mulExpAtom]
{横坐标: <   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: +   纵坐标: addExpAtom}    文法: addExpAtom->[+, mulExp, addExpAtom]
{横坐标: +   纵坐标: argExp}    文法: argExp->[exp]
{横坐标: ;   纵坐标: argVarDef}    文法: argVarDef->[$]
{横坐标: INT   纵坐标: mulExp}    文法: mulExp->[unaryExp, mulExpAtom]
{横坐标: -   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: const   纵坐标: decl}    文法: decl->[constDecl]
{横坐标: +   纵坐标: eqExp}    文法: eqExp->[relExp, eqExpAtom]
{横坐标: int   纵坐标: varDecl}    文法: varDecl->[bType, varDef, argVarDecl, ;]
{横坐标: ,   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: )   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: int   纵坐标: blockItem}    文法: blockItem->[decl, blockItem]
{横坐标: ,   纵坐标: argFunctionR}    文法: argFunctionR->[,, funcRParam, argFunctionR]
{横坐标: Ident   纵坐标: funcRParam}    文法: funcRParam->[exp]
{横坐标: INT   纵坐标: eqExp}    文法: eqExp->[relExp, eqExpAtom]
{横坐标: -   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: {   纵坐标: stmt}    文法: stmt->[block]
{横坐标: )   纵坐标: assignExpAtom}    文法: assignExpAtom->[$]
{横坐标: (   纵坐标: exp}    文法: exp->[assignExp]
{横坐标: -   纵坐标: constExp}    文法: constExp->[assignExp]
{横坐标: -   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: Ident   纵坐标: assignExp}    文法: assignExp->[eqExp, assignExpAtom]
{横坐标: INT   纵坐标: initVal}    文法: initVal->[exp]
{横坐标: =   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: ;   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: ;   纵坐标: assignExpAtom}    文法: assignExpAtom->[$]
{横坐标: #   纵坐标: argExp}    文法: argExp->[$]
{横坐标: INT   纵坐标: stmt}    文法: stmt->[exp, ;]
{横坐标: ;   纵坐标: argConst}    文法: argConst->[$]
{横坐标: INT   纵坐标: funcRParams}    文法: funcRParams->[funcRParam, argFunctionR]
{横坐标: +   纵坐标: constInitVal}    文法: constInitVal->[constExp]
{横坐标: (   纵坐标: addExp}    文法: addExp->[mulExp, addExpAtom]
{横坐标: +   纵坐标: initVal}    文法: initVal->[exp]
{横坐标: (   纵坐标: initVal}    文法: initVal->[exp]
{横坐标: !   纵坐标: stmt}    文法: stmt->[exp, ;]
{横坐标: #   纵坐标: funcRParams}    文法: funcRParams->[$]
{横坐标: #   纵坐标: argFunctionR}    文法: argFunctionR->[$]
{横坐标: !=   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: Ident   纵坐标: exp}    文法: exp->[assignExp]
{横坐标: !   纵坐标: addExp}    文法: addExp->[mulExp, addExpAtom]
{横坐标: -   纵坐标: eqExp}    文法: eqExp->[relExp, eqExpAtom]
{横坐标: int   纵坐标: program}    文法: program->[compUnit]
{横坐标: -   纵坐标: funcRParams}    文法: funcRParams->[funcRParam, argFunctionR]
{横坐标: #   纵坐标: compUnit}    文法: compUnit->[$]
{横坐标: =   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: )   纵坐标: argFunctionR}    文法: argFunctionR->[$]
{横坐标: (   纵坐标: mulExp}    文法: mulExp->[unaryExp, mulExpAtom]
{横坐标: +   纵坐标: relExp}    文法: relExp->[addExp, relExpAtom]
{横坐标: !=   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: int   纵坐标: compUnit}    文法: compUnit->[decl, compUnit]
{横坐标: -   纵坐标: unaryOp}    文法: unaryOp->[-]
{横坐标: INT   纵坐标: number}    文法: number->[INT]
{横坐标: Ident   纵坐标: constExp}    文法: constExp->[assignExp]
{横坐标: -   纵坐标: addExpAtom}    文法: addExpAtom->[-, mulExp, addExpAtom]
{横坐标: ==   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: !   纵坐标: mulExp}    文法: mulExp->[unaryExp, mulExpAtom]
{横坐标: =   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: -   纵坐标: unaryExp}    文法: unaryExp->[unaryOp, unaryExp]
{横坐标: INT   纵坐标: relExp}    文法: relExp->[addExp, relExpAtom]
{横坐标: +   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: Ident   纵坐标: eqExp}    文法: eqExp->[relExp, eqExpAtom]
{横坐标: +   纵坐标: funcRParams}    文法: funcRParams->[funcRParam, argFunctionR]
{横坐标: *   纵坐标: mulExpAtom}    文法: mulExpAtom->[*, unaryExp, mulExpAtom]
{横坐标: -   纵坐标: addExp}    文法: addExp->[mulExp, addExpAtom]
{横坐标: INT   纵坐标: funcRParam}    文法: funcRParam->[exp]
{横坐标: ,   纵坐标: eqExpAtom}    文法: eqExpAtom->[$]
{横坐标: /   纵坐标: mulExpAtom}    文法: mulExpAtom->[/, unaryExp, mulExpAtom]
{横坐标: (   纵坐标: argExp}    文法: argExp->[exp]
{横坐标: /   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: INT   纵坐标: constInitVal}    文法: constInitVal->[constExp]
{横坐标: !   纵坐标: constInitVal}    文法: constInitVal->[constExp]
{横坐标: Ident   纵坐标: relExp}    文法: relExp->[addExp, relExpAtom]
{横坐标: return   纵坐标: stmt}    文法: stmt->[return, argExp, ;]
{横坐标: ,   纵坐标: argVarDef}    文法: argVarDef->[$]
{横坐标: <=   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: !   纵坐标: unaryOp}    文法: unaryOp->[!]
{横坐标: !   纵坐标: initVal}    文法: initVal->[exp]
{横坐标: void   纵坐标: funcDef}    文法: funcDef->[funcType, Ident, (, funcFParams, ), block]
{横坐标: )   纵坐标: funcRParams}    文法: funcRParams->[$]
{横坐标: ,   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: #   纵坐标: assignExpAtom}    文法: assignExpAtom->[$]
{横坐标: Ident   纵坐标: constInitVal}    文法: constInitVal->[constExp]
{横坐标: (   纵坐标: stmt}    文法: stmt->[exp, ;]
{横坐标: (   纵坐标: assignExp}    文法: assignExp->[eqExp, assignExpAtom]
{横坐标: Ident   纵坐标: argExp}    文法: argExp->[exp]
{横坐标: <   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: =   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: =   纵坐标: argVarDef}    文法: argVarDef->[=, initVal]
{横坐标: INT   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: #   纵坐标: argFunctionF}    文法: argFunctionF->[$]
{横坐标: Ident   纵坐标: unaryExp}    文法: unaryExp->[Ident, callFunc]
{横坐标: !   纵坐标: unaryExp}    文法: unaryExp->[unaryOp, unaryExp]
{横坐标: !   纵坐标: funcRParam}    文法: funcRParam->[exp]
{横坐标: const   纵坐标: blockItem}    文法: blockItem->[decl, blockItem]
{横坐标: Ident   纵坐标: funcRParams}    文法: funcRParams->[funcRParam, argFunctionR]
{横坐标: -   纵坐标: mulExp}    文法: mulExp->[unaryExp, mulExpAtom]
{横坐标: +   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: ==   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: INT   纵坐标: exp}    文法: exp->[assignExp]
{横坐标: =   纵坐标: assignExpAtom}    文法: assignExpAtom->[=, eqExp, assignExpAtom]
{横坐标: (   纵坐标: eqExp}    文法: eqExp->[relExp, eqExpAtom]
{横坐标: ,   纵坐标: relExpAtom}    文法: relExpAtom->[$]
{横坐标: return   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: INT   纵坐标: primaryExp}    文法: primaryExp->[number]
{横坐标: #   纵坐标: argVarDecl}    文法: argVarDecl->[$]
{横坐标: #   纵坐标: argConst}    文法: argConst->[$]
{横坐标: )   纵坐标: argFunctionF}    文法: argFunctionF->[$]
{横坐标: ==   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: =   纵坐标: eqExpAtom}    文法: eqExpAtom->[$]
{横坐标: !   纵坐标: argExp}    文法: argExp->[exp]
{横坐标: -   纵坐标: initVal}    文法: initVal->[exp]
{横坐标: -   纵坐标: relExp}    文法: relExp->[addExp, relExpAtom]
{横坐标: int   纵坐标: funcFParam}    文法: funcFParam->[bType, Ident]
{横坐标: void   纵坐标: program}    文法: program->[compUnit]
{横坐标: !   纵坐标: blockItem}    文法: blockItem->[stmt, blockItem]
{横坐标: (   纵坐标: callFunc}    文法: callFunc->[(, funcRParams, )]
{横坐标: (   纵坐标: constExp}    文法: constExp->[assignExp]
{横坐标: ;   纵坐标: stmt}    文法: stmt->[;]
{横坐标: ;   纵坐标: argExp}    文法: argExp->[$]
{横坐标: int   纵坐标: funcFParams}    文法: funcFParams->[funcFParam, argFunctionF]
{横坐标: ;   纵坐标: argVarDecl}    文法: argVarDecl->[$]
{横坐标: #   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: >=   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: <=   纵坐标: relExpAtom}    文法: relExpAtom->[<=, addExp, relExpAtom]
{横坐标: -   纵坐标: exp}    文法: exp->[assignExp]
{横坐标: <=   纵坐标: addExpAtom}    文法: addExpAtom->[$]
{横坐标: ==   纵坐标: eqExpAtom}    文法: eqExpAtom->[==, relExp, eqExpAtom]
{横坐标: +   纵坐标: assignExp}    文法: assignExp->[eqExp, assignExpAtom]
{横坐标: ,   纵坐标: argVarDecl}    文法: argVarDecl->[,, varDef, argVarDecl]
{横坐标: Ident   纵坐标: stmt}    文法: stmt->[exp, ;]
{横坐标: +   纵坐标: exp}    文法: exp->[assignExp]
{横坐标: <=   纵坐标: mulExpAtom}    文法: mulExpAtom->[$]
{横坐标: >=   纵坐标: relExpAtom}    文法: relExpAtom->[>=, addExp, relExpAtom]
{横坐标: +   纵坐标: stmt}    文法: stmt->[exp, ;]
{横坐标: !=   纵坐标: eqExpAtom}    文法: eqExpAtom->[!=, relExp, eqExpAtom]
{横坐标: !   纵坐标: exp}    文法: exp->[assignExp]
{横坐标: INT   纵坐标: constExp}    文法: constExp->[assignExp]
{横坐标: int   纵坐标: bType}    文法: bType->[int]
{横坐标: ==   纵坐标: callFunc}    文法: callFunc->[$]
{横坐标: Ident   纵坐标: constDef}    文法: constDef->[Ident, =, constInitVal]
{横坐标: )   纵坐标: eqExpAtom}    文法: eqExpAtom->[$]
```