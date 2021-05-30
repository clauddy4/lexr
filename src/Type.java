import java.util.Objects;

public class Type {

    TypeGroup typeGroup;
    int line;
    int pos;
    String literal;

    public Type(String literal, TypeGroup typeGroup, int[] ...coordinates) {
        this.literal = literal;
        this.typeGroup = typeGroup;
        try {
            this.line = coordinates[0][0];
            this.pos = coordinates[0][1];
        } catch (Exception e) {
            this.line = 0;
            this.pos = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Type)) return false;
        Type type = (Type) o;
        return
                typeGroup == type.typeGroup &&
                Objects.equals(literal, type.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeGroup, line, pos, literal);
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public TypeGroup getTypeGroup() {
        return typeGroup;
    }
}
