package jeremie.lohyer.soupify.actionParseur;

public class StringValue extends Value {
    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    String construct(Integer tabulation) {
        return "\"" + value + "\"";
    }
}
