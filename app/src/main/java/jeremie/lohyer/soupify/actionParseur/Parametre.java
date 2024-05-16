package jeremie.lohyer.soupify.actionParseur;

public class Parametre {
    private String name;
    private String value;

    public Parametre(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
