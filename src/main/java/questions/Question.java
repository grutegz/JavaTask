package questions;

public final class Question {

    private final String text;
    private final String answer;
    public Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }
    public String text() {
        return text;
    }
    public String answer() {
        return answer;
    }
}