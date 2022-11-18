package com.smartsparrow.courseware.pathway;

/**
 * Calculation helper for Bayesian Knowledge Tracing.
 *
 * See:
 *  1. https://en.wikipedia.org/wiki/Bayesian_Knowledge_Tracing
 *  2. Ryan Baker - http://www.upenn.edu/learninganalytics/MOOT/bigdataeducation.html
 *      a. Video - https://www.youtube.com/watch?v=DXanfWNiaic
 *      b. Slides - http://www.upenn.edu/learninganalytics/MOOT/slides/W004V002.pdf
 *
 *  source from https://github.com/SmartSparrow/aelp-core/commit/24820326fe5d2674a9e921e6970637c4f1886fa0
 *
 */
public class BKT {
    /**
     * Perform a BKT calculation.
     *
     * @param actual the actual value; correct = true, incorrect = false.
     * @param prevLn the previous Ln value, or L0 for the first
     * @param S the probability of Slip (mistake)
     * @param G the probability of Guess (random guess)
     * @param T the probabilty of Transit (learned)
     *
     * @return the BKT calculation result
     */
    public static BKTResult calculate(boolean actual, double prevLn, double S, double G, double T) {
        // calculate both probabilities for the conditional probability.
        double pKnewBeforehandGivenCorrect = (prevLn * (1.0d - S)) / (prevLn * (1.0d - S) + (1.0d - prevLn) * G);
        double pKnewBeforehandGivenIncorrect = (prevLn * S) / (prevLn * S + (1.0d - prevLn) * (1.0d - G));

        // this is the "conditional probability" (it changes based on the actual)
        double pKnown;
        if (actual) {
            pKnown = pKnewBeforehandGivenCorrect + ((1.0d - pKnewBeforehandGivenCorrect) * T);
        } else {
            pKnown = pKnewBeforehandGivenIncorrect + ((1.0d - pKnewBeforehandGivenIncorrect) * T);
        }

        // assemble the result
        BKTResult bktResult = new BKTResult();
        bktResult.pCorrect = (prevLn * (1.0 - S)) + ((1.0 - prevLn) * G);
        bktResult.pLnMinus1GivenActual = (actual ? pKnewBeforehandGivenCorrect : pKnewBeforehandGivenIncorrect);
        bktResult.pLn = pKnown;
        return bktResult;
    }

    public static class BKTResult {
        // the P(Ln-1|Actual) -- the probability that they knew it before they submitted their answer
        public double pLnMinus1GivenActual;
        // P(Ln) -- latent knowledge (how probable is the student to know the skill they are currently working on)
        public double pLn;
        // P(CORR) -- The probability P(CORR) that the learner will get the item correct
        public double pCorrect;
    }
}
