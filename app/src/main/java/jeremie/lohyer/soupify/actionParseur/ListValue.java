package jeremie.lohyer.soupify.actionParseur;

import java.util.ArrayList;
import java.util.List;

public class ListValue extends Value {
    public List<Value> values;

    public ListValue() {
        this.values = new ArrayList<>();
    }
    public ListValue(List<Value> values) {
        this.values = values;
    }

    public List<Value> getValues() {
        return values;
    }

    public ListValue addValue(Value value) {
        values.add(value);
        return this;
    }

    @Override
    String construct(Integer tabulation) {
        StringBuilder ret = new StringBuilder("[\n");
        for (Value value :
                values) {
            ret.append(tabulation(tabulation));
            ret.append(value.construct(tabulation + 1)).append(",\n");
        }
        return ret + "]\n";
    }

    static ListValue parse(int commence, char[] texte) {
        ListValue ret = new ListValue();

        boolean ignoreNext = false;

        StringBuilder value = new StringBuilder();
        String parameterName = "";

        for (int index = commence; index < texte.length; index++) {
            char character = texte[index];
            if (character == '\\') {
                ignoreNext = true;
            }
            else {
                if (ignoreNext) {
                    ignoreNext = false;
                } else if (character == '{') {
                    ret.addValue(ClassValue.parse(index, texte));
                } else if (character == '"') {
                    ret.addValue(StringValue.parse(index, texte));
                } else if (character == '[') {
                    ret.addValue(ListValue.parse(index, texte));
                } else if (character == ']') {
                    return ret;
                }
            }
        }
        return ret;
    }
}
