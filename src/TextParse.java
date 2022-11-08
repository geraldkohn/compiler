import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TextParse {
    static ArrayList<Formula> formulas;	// 产生式
    static ArrayList<String> terminals;	// 终结符
    static ArrayList<String> nonTerminals;	// 非终结符
    static HashMap<String, ArrayList<String>> firsts;
    static HashMap<String, ArrayList<String>> follows;
    static HashMap<String, Formula> predictions;

    static void writeAllIntoFile() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.formulaPath));
            out.write("文法解析结果如下: --------------------\n");
            out.write("总共有 " + formulas.size() + " 条数据\n");
            out.write("\n");
            for (Formula formula : formulas) {
                out.write("文法左侧: " + formula.left + " 文法右侧: " + Arrays.toString(formula.right) + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.terminalPath));
            out.write("从文法中解析的终结符结果如下: --------------------\n");
            out.write("总共有 " + terminals.size() + " 条数据\n");
            out.write("\n");
            for (String s : terminals) {
                out.write(s + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.nonTerminalPath));
            out.write("从文法中解析的非终结符结果如下: --------------------\n");
            out.write("总共有 " + nonTerminals.size() + " 条数据\n");
            out.write("\n");
            for (String s : nonTerminals) {
                out.write(s + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.firstTablePath));
            out.write("First列表结果如下: --------------------\n");
            out.write("总共有 " + firsts.size() + " 条数据\n");
            out.write("\n");
            for (String s : firsts.keySet()) {
                out.write(s + "   " + firsts.get(s) + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.followTablePath));
            out.write("Follow列表结果如下: --------------------\n");
            out.write("总共有 " + follows.size() + " 条数据\n");
            out.write("\n");
            for (String s : follows.keySet()) {
                out.write(s + "   " + follows.get(s) + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Config.predictMapPath));
            out.write("预测表结果如下: --------------------\n");
            out.write("总共有 " + predictions.size() + " 条数据\n");
            out.write("\n");
            for (String s : predictions.keySet()) {
                out.write(s + "    " + "文法: " + predictions.get(s).left + "->" +
                        Arrays.toString(predictions.get(s).right) + "\n");
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void main(String[] args) {
//        Do();
//        for (Formula formula : formulas) {
//            System.out.println("left: " + formula.left + " right: " + Arrays.toString(formula.right));
//        }
//        for (String s : terminals) {
//            System.out.println("终结符: " + s);
//        }
//        for (String s : nonTerminals) {
//            System.out.println("非终结符: " + s);
//        }
//        for (String s : firsts.keySet()) {
//            System.out.println("First: " + "Key: " + s + " Value: " + firsts.get(s));
//        }
//        for (String s : follows.keySet()) {
//            System.out.println("Follow: " + "Key: " + s + " Value: " + follows.get(s));
//        }
//        for (String s : predictions.keySet()) {
//            System.out.println("Prediction: " + s + " Value: " + Arrays.toString(predictions.get(s).right));
//        }
//    }

    // 入口
    static void Do() {
        formulas = new ArrayList<>();
        terminals = new ArrayList<>();
        nonTerminals = new ArrayList<>();
        firsts = new HashMap<>();
        follows = new HashMap<>();
        predictions = new HashMap<>();
        setFormulas();
        setNonTerminals();
        setTerminals();
        setFirsts();
        setFollows();
        setPrediction();
    }

    // 这个函数会生成文法规则
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
                right = line.split("->")[1].trim();	// 将右侧所有的值都算进去
                formula = new Formula(left, right.split(" ")); // 根据空格分离右侧的值
                formulas.add(formula);
            }
            randomfile.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

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

    // 生成 First 集合
    static void setFirsts() {
        FirstTable.setFirst(formulas,terminals,nonTerminals,firsts);
    }

    // 生成 Follow 集合
    static void setFollows() {
        FollowTable.setFollow(formulas,terminals,nonTerminals,firsts,follows);
    }

    static void setPrediction() {
        PredictMap.setPrediction(formulas,terminals,nonTerminals,firsts,follows,predictions);
    }
}

// 产生式类(在这里解析文法)
class Formula {
    String left;
    String[] right;
    // 初始化select集
    ArrayList<String> select = new ArrayList<String>();
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
