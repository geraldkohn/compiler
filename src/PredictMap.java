import java.util.ArrayList;
import java.util.HashMap;

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
        return  "{横坐标: " + terminal + "  " + "纵坐标: " + nonTerminal + "}";
    }

}
