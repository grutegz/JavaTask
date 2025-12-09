package logic;

public final class EvaluationResult {

    private final boolean isCorrect;
    private final String message;

    public EvaluationResult(boolean isCorrect, String message) {
        this.isCorrect = isCorrect;
        this.message = message;
    }

    public boolean isCorrect() {
        return this.isCorrect;
    }

    public String getMessage() {
        return this.message;
    }
}