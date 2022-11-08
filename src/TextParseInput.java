import java.util.ArrayList;

public class TextParseInput {
    private static ArrayList<String> lex_result_stack;

    public static void setLex_result_stack(ArrayList<String> lex) {
        lex_result_stack = lex;
    }

    public static ArrayList<String> getLex_result_stack() {
        return lex_result_stack;
    }
}
