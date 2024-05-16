package jeremie.lohyer.soupify.actionParseur;

import java.util.Map;

public class ClassValue extends Value {
    private Map<String, Value> values;

    public ClassValue() {

    }

    public ClassValue(Map<String, Value> values) {
        this.values = values;
    }

    public ClassValue addValue(String key, Value value) {
        values.put(key, value);
        return this;
    }

    public Value getValue(String key) {
        return values.getOrDefault(key, new StringValue("null"));
    }

    @Override
    public String construct(Integer tabulation) {
        StringBuilder ret = new StringBuilder("{\n");
        for (String key :
                values.keySet()) {
            ret.append(tabulation(tabulation));
            ret.append("\"").append(key).append("\" : ").append(getValue(key).construct(tabulation + 1)).append(",");
        }
        return ret + "}\n";
    }

}
