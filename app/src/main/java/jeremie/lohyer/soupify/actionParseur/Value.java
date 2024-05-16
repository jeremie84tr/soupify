package jeremie.lohyer.soupify.actionParseur;

public abstract class Value {
    abstract String construct(Integer tabulation);

    static Value parse(int commence, char[] texte) {
        ClassValue ret = new ClassValue();

        boolean inJson = false;
        boolean inString = false;
        boolean inParameterName = true;
        boolean ignoreNext = false;

        StringBuilder value = new StringBuilder();
        String parameterName = "";

        for (int index = commence; index < texte.length; index++) {
            char character = texte[index];
            if (character == '\\') {
                ignoreNext = true;
            }
            else if (inString) {
                if (character == '"' && !ignoreNext) {
                    inString = false;
                    if (inParameterName) {
                        parameterName = value.toString();
                    } else { //fin de valeur d'un parametre -> action a executer
                        inParameterName = true;
                    }
                } else {
                    value.append(character);
                }
            } else {
                if (ignoreNext) {
                    ignoreNext = false;
                } else if (character == '}') {
                    if (inJson) {
                        return ret;
                    } else {
                        throw new Error("no JSON in text");
                    }
                } else if (character == '{') {
                    if (inJson) {
                        ret.addValue(parameterName, ClassValue.parse(index, texte));
                    } else {
                        ret = new ClassValue();
                    }
                } else if (character == '"') {
                    if (inParameterName) {
                        inString = true;
                        value = new StringBuilder();
                    } else {
                        ret.addValue(parameterName, StringValue.parse(index, texte));
                    }
                } else if (character == ':') {
                    inParameterName = false;
                } else if (character == '[') {
                    ret.addValue(parameterName, ListValue.parse(index, texte));
                }
            }
        }
        return ret;
    }
    String tabulation(Integer tabulation) {
        StringBuilder ret = new StringBuilder();
        if(tabulation != null) {
            for (int i = 0; i < tabulation; i++) {
                ret.append("\t");
            }
        }
        return ret.toString();
    }
}

