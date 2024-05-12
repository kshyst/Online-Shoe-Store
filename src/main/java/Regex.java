public enum Regex {

    REGISTER("\\s*register:(?<id>\\S+):(?<name>\\S+):(?<money>\\S+)\\s*"),
    LOGIN("\\s*login:(?<id>\\S+)\\s*"),
    LOGOUT("\\s*logout\\s*"),
    GETPRICE("\\s*get price:(?<shoename>\\S+)\\s*"),
    GETQUANTITY("\\s*get quantity:(?<shoename>\\S+)\\s*"),
    GETMONEY("\\s*get money\\s*"),
    CHARGE("\\s*charge:(?<money>\\S+)\\s*"),
    PURCHASE("\\s*purchase:(?<shoename>\\S+):(?<quantity>\\S+)\\s*");

    private final String regex;

    Regex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
