import java.math.BigInteger;

public class Driver {

    public static void main(String[] args) {
        int N = 1000;
        int[] p_pows = {16,      18,         20,         22};
        int[] p_subs = {17,      5,          5,          17};
        String[] ds  = {"154",   "294",      "47",       "314"};
        String[] ns  = {"16339", "65717",    "262643",   "1049497"};
        String[] axs = {"12",    "5",        "3",        "4"};
        String[] ays = {"61833", "261901",   "111745",   "85081"};

        for (int i = 0; i < 4; i++) {
            BigInteger p = BigInteger.valueOf(2).pow(p_pows[i]).subtract(BigInteger.valueOf(p_subs[i]));  // 2**p_pows[i] - p_subs[i]
            BigInteger d = new BigInteger(ds[i]);
            BigInteger n = new BigInteger(ns[i]);
            BigInteger[] a = ECPoint.point(axs[i], ays[i]);
            long k = 0;

            for (int j = 0; j < N; j++) {
                try {
                    k += ECPoint.check(a, d, p, n);
                } catch (SextupleInitializationException e) {
                    j--;    // if check() fails, ignore this call of check() and check again
                }
            }

            k /= N;

            System.out.println("p = 2 ^ " + p_pows[i] + " - " + p_subs[i] + "\t\td = " + ds[i] + "\t\tn = " + ns[i]);
            System.out.println("a = (" + axs[i] + "\t, " + ays[i] + ")");
            System.out.println("N = " + N);
            System.out.println("k = " + k);
            System.out.println();
        }
    }

}
