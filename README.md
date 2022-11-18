## 天津大学编译原理大作业

* 完成词法分析
* 完成语法分析

代码结构：

```
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