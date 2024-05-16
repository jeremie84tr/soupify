package jeremie.lohyer.soupify.actionParseur;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ActionParseur {
    private Map<String,Function<String, Void>> actionDeclencheur;

    public ActionParseur() {
        actionDeclencheur = new HashMap<>();
    }

    public void ajouteAction(String declencheur, Function<String, Void> action) {
        actionDeclencheur.put(declencheur, action);
    }

    public Function<String, Void> parseAction(String action) {
        return actionDeclencheur.getOrDefault(action, parametre -> {return null;});
    }

    public void parseTexte(String text) throws Error {

        Map<String, String> parametres = new HashMap<>();

        char[] texte = text.toCharArray();

        boolean inJson = false;
        boolean inString = false;
        boolean inParameterName = true;
        boolean ignoreNext = false;

        StringBuilder value = new StringBuilder();
        String parameterName = "";

        for (int index = 0; index < texte.length; index++) {
            char character = texte[index];
            if (character == '\\') {
                ignoreNext = true;
            }
            else if (inString) {
                if (character == '"' && !ignoreNext) {
                    inString = false;
                    if (inParameterName) {
                        parameterName = value.toString();
                    } else {
                        System.out.println("parametre ajouté : " + parameterName + " -> " + value);
                        parametres.put(parameterName, value.toString());
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
                        index = texte.length;
                    } else {
                        throw new Error("no JSON in text");
                    }
                } else if (character == '{') {
                    inJson = true;
                } else if (character == '"') {
                    inString = true;
                    value = new StringBuilder();
                } else if (character == ':') {
                    inParameterName = false;
                }
            }
        }

        String parametreAction = parametres.getOrDefault("action", "");
        String parametreSong = parametres.getOrDefault("song", "null");

        actionDeclencheur.getOrDefault(parametreAction, songName -> {return null;}).apply(parametreSong);
    }
}
