package questions;

import java.util.Random;

public final class ArithmeticSumQuestionSource implements QuestionSource {
    private final Random rng = new Random();

    private final int minA1, maxA1;
    private final int minD,  maxD;
    private final int minN,  maxN;
    private final long limitAbs;

    public ArithmeticSumQuestionSource() {
        this(-50, 50, -20, 20, 3, 50, 1_000_000_000L);
    }

    public ArithmeticSumQuestionSource(int minA1, int maxA1, int minD, int maxD,
                                       int minN, int maxN, long limitAbs) {
        if (minA1 > maxA1 || minD > maxD || minN > maxN) throw new IllegalArgumentException("Bad ranges");
        this.minA1 = minA1; this.maxA1 = maxA1;
        this.minD  = minD;  this.maxD  = maxD;
        this.minN  = minN;  this.maxN  = maxN;
        this.limitAbs = limitAbs;
    }

    @Override
    public Question next() {
        while (true) {
            int a1 = rnd(minA1, maxA1);
            int d  = rnd(minD, maxD);
            int n  = rnd(minN, maxN);

            // S_n = n/2 * (2a1 + (n-1)d)
            long numerator = (long) n * (2L * a1 + (long)(n - 1) * d);
            if ((numerator & 1L) != 0L) continue; // На всякий случай (теоретически должно делиться)

            long sum = numerator / 2L;
            if (Math.abs(sum) > limitAbs) continue;

            String text = String.format(
                    "Арифметическая прогрессия: a1=%d, d=%d, n=%d. Найдите S_n (сумму первых n членов).",
                    a1, d, n
            );
            return new Question(text, Long.toString(sum));
        }
    }

    private int rnd(int lo, int hi) {
        return lo + rng.nextInt(hi - lo + 1);
    }
}