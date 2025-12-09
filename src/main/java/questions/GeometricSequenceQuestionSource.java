package questions;

import java.util.Random;

public final class GeometricSequenceQuestionSource implements QuestionSource {
    private final Random rng = new Random();

    // Диапазоны (можешь настроить)
    private final int minA1, maxA1;   // первый член
    private final int minR,  maxR;    // знаменатель прогрессии r
    private final int minN,  maxN;    // номер члена
    private final long limitAbs;      // ограничение по модулю для ответа

    public GeometricSequenceQuestionSource() {
        this(-10, 10, -5, 5, 2, 8, 1_000_000_000L);
    }

    public GeometricSequenceQuestionSource(int minA1, int maxA1, int minR, int maxR,
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
            int r  = rndNonZero(minR, maxR); // r != 0
            int n  = rnd(minN, maxN);

            // an = a1 * r^(n-1)
            int exp = n - 1;
            long absR = Math.abs((long) r);
            long absPow = 1L;
            for (int i = 0; i < exp; i++) {
                if (absPow > limitAbs / Math.max(1L, absR)) { absPow = -1; break; }
                absPow *= absR;
            }
            if (absPow < 0) continue;

            boolean powNegative = (r < 0) && ((exp & 1) == 1);
            long pow = powNegative ? -absPow : absPow;

            long absA1 = Math.abs((long) a1);
            if (absA1 > 0 && absPow > limitAbs / absA1) continue;

            long an = (long) a1 * pow;
            if (Math.abs(an) > limitAbs) continue;

            String text = String.format("Геометрическая прогрессия: a1=%d, r=%d, n=%d. Найдите a_n.", a1, r, n);
            return new Question(text, Long.toString(an));
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