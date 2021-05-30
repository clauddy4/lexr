import java.util.*;

public class Lexer
{
    int[] coordinates = new int[]{1,0}; // [0] is line, [1] is position
    Map<String,Type> allTypes = new HashMap<>();
    char currentChar;
    Type[] expectedTypes = null;
    String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_.+-";
    String delimiters = ".+-";
    Mode currentMode = Mode.Default;
    String currentString;
    public ArrayList<Type> outputTypes;

    public Lexer()
    {
        this.currentString = "";
        this.fillTypes();
        this.outputTypes = new ArrayList<>();
    }

    public List<String> getSpecialChars() {
        return Arrays.asList(
                "'\\''", "'\\\\'", "'\\\"'"
        );
    }

    public List<String> getKeywords() {
        return Arrays.asList(
                "public", "static", "void", "main", "import", "package", "private", "return", "true", "class", "char", "struct", "and", "or", "not",
                "false", "if", "else", "while", "for", "struct", "bool", "float", "string", "int", "binary", "print", "read", "continue"
        );
    }

    public void fillTypes()
    {

        this.allTypes.put("public",new Type("public", TypeGroup.keyword));
        this.allTypes.put("static",new Type("static", TypeGroup.keyword));
        this.allTypes.put("void",new Type("void",  TypeGroup.keyword));
        this.allTypes.put("main",new Type("main",  TypeGroup.keyword));
        this.allTypes.put("import",new Type("import",  TypeGroup.keyword));
        this.allTypes.put("package",new Type("package",  TypeGroup.keyword));
        this.allTypes.put("private",new Type("private",  TypeGroup.keyword));
        this.allTypes.put("return",new Type("return",  TypeGroup.keyword));
        this.allTypes.put("true",new Type("true", TypeGroup.keyword));
        this.allTypes.put("false",new Type("false",  TypeGroup.keyword));
        this.allTypes.put("if",new Type("if",  TypeGroup.statement));
        this.allTypes.put("else",new Type("else", TypeGroup.statement));
        this.allTypes.put("while",new Type("while", TypeGroup.statement));
        this.allTypes.put("comma",new Type(",", TypeGroup.Separator));
        this.allTypes.put("dot",new Type(".", TypeGroup.Separator));
        this.allTypes.put("semicolon",new Type(";", TypeGroup.Separator)); //;
        this.allTypes.put("comment_begin",new Type("/*", TypeGroup.Comment));
        this.allTypes.put("line_comment_begin",new Type("//", TypeGroup.Comment));
        this.allTypes.put("comment_end",new Type("*/", TypeGroup.Comment));
        this.allTypes.put("multiply",new Type("*", TypeGroup.operator));
        this.allTypes.put("divide",new Type("/", TypeGroup.operator));
        this.allTypes.put("plus",new Type("+", TypeGroup.operator));
        this.allTypes.put("minus",new Type("-", TypeGroup.operator));
        this.allTypes.put("equal",new Type("=", TypeGroup.operator));
        this.allTypes.put("is_equal",new Type("==", TypeGroup.Comparison));
        this.allTypes.put("more",new Type(">", TypeGroup.Comparison));
        this.allTypes.put("more_or_equal",new Type(">=", TypeGroup.Comparison));
        this.allTypes.put("less_or_equal",new Type("<=", TypeGroup.Comparison));
        this.allTypes.put("less",new Type("<", TypeGroup.Comparison));
        this.allTypes.put("mod",new Type("%", TypeGroup.operator));
        this.allTypes.put("not",new Type("!", TypeGroup.Comparison));
        this.allTypes.put("and",new Type("&&", TypeGroup.Comparison));
        this.allTypes.put("not_equal",new Type("!=", TypeGroup.Comparison));
        this.allTypes.put("quote_character",new Type("\"", TypeGroup.Comparison));
        this.allTypes.put("single_quote_character",new Type("\\'", TypeGroup.Comparison));
        this.allTypes.put("or",new Type("||", TypeGroup.Comparison));
        this.allTypes.put("begin",new Type("{", TypeGroup.Bracket));
        this.allTypes.put("end",new Type("}", TypeGroup.Bracket));
        this.allTypes.put("bool_class",new Type("bool", TypeGroup.keyword));
        this.allTypes.put("float_class",new Type("float", TypeGroup.keyword));
        this.allTypes.put("string_class",new Type("string", TypeGroup.keyword));
        this.allTypes.put("int_class",new Type( "int", TypeGroup.keyword));
        this.allTypes.put("binary_class",new Type("binary", TypeGroup.keyword));
        this.allTypes.put("hex_class",new Type("Hexadecimal", TypeGroup.keyword));
        this.allTypes.put("error_class",new Type("Error", TypeGroup.keyword));
        this.allTypes.put("print_func",new Type("print", TypeGroup.keyword));
        this.allTypes.put("read_func",new Type("read", TypeGroup.keyword));
        this.allTypes.put("begin_bracket",new Type("(", TypeGroup.Bracket));
        this.allTypes.put("end_bracket",new Type(")", TypeGroup.Bracket));
        this.allTypes.put("begin_squared_bracket",new Type("[", TypeGroup.Bracket));
        this.allTypes.put("end_squared_bracket",new Type("]", TypeGroup.Bracket));
    }

    public void makeIteration(Character ch, int[] coordinates) {
        this.coordinates = coordinates;
        this.makeState(ch);
    }

    public void makeState(Character ch) {
        try {
            this.currentChar = ch;
            if (this.currentMode == Mode.OneLineComment) {
                if (this.currentChar != '\n') {
                    return;
                }
            }
            if (this.currentMode == Mode.Comment) {
                if (!((this.currentChar == '*') || (this.currentChar == '/'))) {
                    return;
                }
            }
            if (this.currentMode == Mode.String) {
                this.stringCase();
                if ((this.currentChar != '"') || ((this.currentString.charAt(this.currentString.length() - 2) == '\\') && (this.currentString.charAt(this.currentString.length() - 3) != '\\'))) {
                    return;
                }
            }
            if (this.currentMode == Mode.Char) {
                this.charCase();
                return;
            }
            if (this.currentMode == Mode.NonKeywordData) {
                this.numOrIdCase();
                return;
            }
            if (this.expectedTypes == null) {
                Type t = this.getType();
                if (t==null) {
                    return;
                }
                this.outputTypes.add(t);
            }
            else {
                Type t = this.oneOrTwoCharCase();
                if (t==null) {
                    return;
                }
                this.outputTypes.add(t);
                this.expectedTypes = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Type getType() {
        switch (this.currentChar) {
            case '*':
                this.expectedTypes = new Type[]{this.allTypes.get("multiply"), this.allTypes.get("comment_end")};
                break;
            case '/':
                this.expectedTypes = new Type[]{this.allTypes.get("divide"), this.allTypes.get("comment_begin"), this.allTypes.get("line_comment_begin")};
                break;
            case '>':
                this.expectedTypes = new Type[]{this.allTypes.get("more"), this.allTypes.get("more_or_equal")};
                break;
            case '<':
                this.expectedTypes = new Type[]{this.allTypes.get("less"), this.allTypes.get("less_or_equal")};
                break;
            case '=':
                this.expectedTypes = new Type[]{this.allTypes.get("equal"), this.allTypes.get("is_equal")};
                break;
            case '!':
                this.expectedTypes = new Type[]{this.allTypes.get("not"), this.allTypes.get("not_equal")};
                break;
            case '-','+','%','^': return new Type(String.valueOf(this.currentChar), TypeGroup.operator, this.coordinates);
            case '(',')','{','}','[',']': return new Type(String.valueOf(this.currentChar), TypeGroup.Bracket, this.coordinates);
            case ';',',','.': return new Type(String.valueOf(this.currentChar), TypeGroup.Separator, this.coordinates);
            case '\n':
                if (this.currentMode == Mode.OneLineComment) {
                    this.currentMode = Mode.Default;
                    this.currentString = "";
                }
                break;
            case '"':
                if (this.currentMode == Mode.String) {
                    this.currentMode = Mode.Default;
                    return new Type(this.currentString, TypeGroup.String, this.coordinates);
                }
                this.stringCase();
                break;
            case '\'':
                this.charCase();
                break;
            default: numOrIdCase();
        }
        return null;
    }

    public Type oneOrTwoCharCase() {
        String literal1 = this.expectedTypes[0].getLiteral();
        String literal2 = this.expectedTypes[1].getLiteral();
        String literal3 = null;
        if (this.expectedTypes.length == 3) {
            literal3 = this.expectedTypes[2].getLiteral();
        }
        switch (this.currentChar) {
            case '=':
                switch (literal2) {
                    case "!=", "==", "<=", ">=", "<", ">": return new Type(literal2, TypeGroup.Comparison, this.coordinates);
                }
            case '/':
                if (literal2.equals("*/")) {
                    this.currentMode = Mode.Default;
                    this.currentString = "";
                    return new Type(literal2, TypeGroup.Comment, this.coordinates);
                }
                if (literal3 != null && literal3.equals("//")) {
                    this.currentString = "";
                    this.currentMode = Mode.OneLineComment;
                    return new Type(literal3, TypeGroup.Comment, this.coordinates);
                }
                break;
            case '*':
                if (literal2.equals("/*")) {
                    this.currentMode = Mode.Comment;
                    this.currentString = "";
                    return new Type(literal2, TypeGroup.Comment, this.coordinates);
                }
                break;
            default:
                return new Type(literal1, TypeGroup.operator, this.coordinates);
        }
        return null;
    }

    public void stringCase() {
        if (this.currentMode == Mode.Default) {
            this.currentString = "";
        }
        this.currentMode = Mode.String;
        this.currentString+=this.currentChar;
    }

    public void charCase() {
        if (this.currentMode == Mode.Default) {
            this.currentString = "";
        }
        this.currentMode = Mode.Char;
        this.currentString+=this.currentChar;
        if ((this.currentString.charAt(0) == '\'') && (this.currentString.charAt(this.currentString.length()-1) == '\'')){
            if ((this.currentString.length() == 3) && (this.currentString.charAt(this.currentString.length()-2) != '\\')) {
                this.outputTypes.add(new Type(this.currentString, TypeGroup.Character, coordinates));
                this.currentString = "";
                this.currentMode = Mode.Default;
            }
            if ((this.currentString.length() == 4) && (this.getSpecialChars().contains(this.currentString))) {
                this.outputTypes.add(new Type(this.currentString, TypeGroup.Character, coordinates));
                this.currentString = "";
                this.currentMode = Mode.Default;
            }
            else if ((this.currentString.length() > 3) || (this.currentString.length() == 2)) {
                this.outputTypes.add(new Type(this.currentString, TypeGroup.UnknownCharacter, coordinates));
                this.currentString = "";
                this.currentMode = Mode.Default;
            }
        }
    }

    public void numOrIdCase() {
        if (this.currentMode == Mode.Default) {
            this.currentString = "";
        }
        if (this.allowedChars.indexOf(this.currentChar) != -1) {
            if (this.delimiters.indexOf(this.currentChar) != -1) {
                if (!this.currentString.matches("0|[1-9][0-9]{0,9}")) {
                    this.currentMode = Mode.Default;
                    if ((this.currentString.matches("[_A-Za-z]{1,9}[_A-Za-z0-9]{0,9}"))) {
                        Type tmp = new Type(this.currentString, TypeGroup.Identifier, this.coordinates);
                        this.outputTypes.add(tmp);
                        this.currentString = "";
                        this.makeState(this.currentChar);
                        return;
                    }
                }
                else if (this.currentChar != '.') {
                    Type tmp = new Type(this.currentString, TypeGroup.Integer, this.coordinates);
                    this.outputTypes.add(tmp);
                    this.currentString = "";
                    this.currentMode = Mode.Default;
                    this.makeState(this.currentChar);
                    return;
                }
            }
            this.currentString+=this.currentChar;
            this.currentMode = Mode.NonKeywordData;
        }
        else {
            if (this.getKeywords().contains(this.currentString)) {
                Type tmp = new Type(this.currentString, TypeGroup.keyword, this.coordinates);
                this.outputTypes.add(tmp);

            }
            else if (this.currentString.matches("0|[1-9][0-9]{0,9}")) {
                try {
                    int i = Integer.parseInt(this.currentString);
                    Type tmp = new Type(this.currentString, TypeGroup.Integer, this.coordinates);
                    this.outputTypes.add(tmp);
                } catch (Exception e) {
                    Type tmp = new Type("integer number > MAX_INT", TypeGroup.Error_input, this.coordinates);
                    this.outputTypes.add(tmp);
                }
            }
            else if (this.currentString.matches("0x([0-9a-fA-F]{1,8})")) {
                Type tmp = new Type(this.currentString, TypeGroup.Hexadecimal, this.coordinates);
                this.outputTypes.add(tmp);
            }
            else if (this.currentString.matches("^0b[01]{1,32}")) {
                Type tmp = new Type(this.currentString, TypeGroup.Binary, this.coordinates);
                this.outputTypes.add(tmp);
            }
            else if ((this.currentString.matches("([0-9]+([.][0-9]*)?|[.][0-9]+)([eE][-+][0-9]{1,6})")) || (this.currentString.matches("([0-9]+([.][0-9]*)?|[.][0-9]+)$"))) {
                Type tmp = new Type(this.currentString, TypeGroup.Float, this.coordinates);
                this.outputTypes.add(tmp);
            }
            else if ((this.currentString.matches("[_A-Za-z]{1,9}[_A-Za-z0-9]{0,9}"))) {
                Type tmp = new Type(this.currentString, TypeGroup.Identifier, this.coordinates);
                this.outputTypes.add(tmp);
            }
            else if (currentMode == Mode.NonKeywordData) {
                Type tmp = new Type("identifier "+this.currentString, TypeGroup.Error_wrong, this.coordinates);
                this.outputTypes.add(tmp);
            }
            else if (currentMode == Mode.Default) {
                return;
            }
            this.currentMode = Mode.Default;
            this.currentString = "";
            this.makeState(this.currentChar);
        }
    }
    public void output(HashMap<Integer,Integer> hm) {
        if (!this.currentString.equals("")) {
            this.outputTypes.add(new Type(this.currentMode+" "+this.currentString, TypeGroup.Error_Unfinished, this.coordinates));
        }
        this.outputTypes.add(new Type("", TypeGroup.EndOfFile, this.coordinates));
        for (Type t : this.outputTypes) {
            int line = t.line;
            int pos = t.pos;
            if ((pos == 0) && (line > 1) && (t.typeGroup != TypeGroup.EndOfFile)) {
                pos = hm.get(line);
                line = line - 1;
            }
            System.out.println(t.typeGroup + " " + t.literal + " at line: " + line + ", position: " + pos);
        }
    }
    public void add(Type t) {
        this.outputTypes.add(t);
    }
}
