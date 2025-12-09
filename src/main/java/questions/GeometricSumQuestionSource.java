package questions;

import java.util.Random;

public final class GeometricSumQuestionSource implements QuestionSource {
    private final Random rng = new Random();

    private final int minA1, maxA1;
    private final int minR,  maxR;
    private final int minN,  maxN;
    private final long limitAbs;

    public GeometricSumQuestionSource() {
        this(-20, 20, -5, 5, 2, 8, 1_000_000_000L);
    }

    public GeometricSumQuestionSource(int minA1, int maxA1, int minR, int maxR,
                                      int minN, int maxN, long limitAbs) {
        if (minA1 > maxA1 || minR > maxR || minN > maxN) throw new IllegalArgumentException("Bad ranges");
        this.minA1 = minA1; this.maxA1 = maxA1;
        this.minR  = minR;  this.maxR  = maxR;
        this.minN  = minN;  this.maxN  = maxN;
        this.limitAbs = limitAbs;
    }

    @Override
    public Question next() {
        while (true) {
            int a1 = rndNonZero(minA1, maxA1);
            int r  = rnd(minR, maxR); // r может быть 1 или отрицательным
            int n  = rnd(minN, maxN);

            long sum = 0L;
            long term = a1;
            boolean ok = true;

            for (int i = 0; i < n; i++) {
                sum += term;
                if (Math.abs(sum) > limitAbs) { ok = false; break; }
                if (i < n - 1) {
                    long next = term * (long) r;
                    if (Math.abs(next) > limitAbs) { ok = false; break; }
                    term = next;
                }
            }
            if (!ok) continue;

            String text = String.format(
                    "Геометрическая прогрессия: a1=%d, r=%d, n=%d. Найдите S_n (сумму первых n членов).",
                    a1, r, n
            );
            return new Question(text, Long.toString(sum));
        }
    }

    private int rnd(int lo, int hi) {
        return lo + rng.nextInt(hi - lo + 1);
    }

    private int rndNonZero(int lo, int hi) {
        int v;
        do { v = rnd(lo, hi); } while (v == 0);
        return v;
    }
}