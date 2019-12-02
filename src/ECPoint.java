import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * A set of operations on points on elliptic curves. <br>
 * A point on a elliptic curve is represented by an array of BigInteger, which contains exactly two elements each,
 * namely, its x-coordinate and its y-coordinate.
 */
public class ECPoint {

    /** Returns a point on a elliptic curve specified by x and y. This method improves the code's readability.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the elliptic curve point
     */
    public static BigInteger[] point(String x, String y) {
        return new BigInteger[]{new BigInteger(x), new BigInteger(y)};
    }


    /** Returns the product of two elliptic curve points a1 and a2, where the elliptic curve is specified by d and p.
     * @param a1 a point on a elliptic curve
     * @param a2 another point on the same elliptic curve
     * @param d the constants in the Edwards curve x^2 + y^2 = 1 + d * x^2 * y^2
     * @param p the modulus, which is a large prime
     * @return the product of a1 and a2, which is also a point on this elliptic curve
     */
    public static BigInteger[] mul(BigInteger[] a1, BigInteger[] a2, BigInteger d, BigInteger p) {
        BigInteger x1 = a1[0];
        BigInteger y1 = a1[1];
        BigInteger x2 = a2[0];
        BigInteger y2 = a2[1];

        BigInteger dx1x2y1y2 = d.multiply(x1).multiply(x2).multiply(y1).multiply(y2);
        BigInteger x3 = x1.multiply(y2).add(y1.multiply(x2));
        BigInteger x3_ = (BigInteger.valueOf(1).add(dx1x2y1y2));
        x3 = x3.multiply(x3_.modInverse(p)).mod(p);
        BigInteger y3 = y1.multiply(y2).subtract(x1.multiply(x2));
        BigInteger y3_ = BigInteger.valueOf(1).subtract(dx1x2y1y2);
        y3 = y3.multiply(y3_.modInverse(p)).mod(p);

        return new BigInteger[]{x3, y3};
    }


    /** Returns the exponentiation of a elliptic curve point a, where the elliptic curve is specified by d and p, and
     * the exponent is m.
     * @param a a point on a elliptic curve, which is the base
     * @param m the exponent
     * @param d the constants in the Edwards curve x^2 + y^2 = 1 + d * x^2 * y^2
     * @param p the modulus, which is a large prime
     * @return the exponentiation, which is also a point on this elliptic curve
     */
    public static BigInteger[] exp(BigInteger[] a, BigInteger m, BigInteger d, BigInteger p) {
        BigInteger[] b = ECPoint.point("0", "1");
        for (int i = m.bitLength() - 1; i >= 0; i--) {
            b = ECPoint.mul(b, b, d, p);
            if (m.testBit(i)) {
                b = ECPoint.mul(b, a, d, p);
            }
        }
        return b;
    }

    /** The discrete logarithm operation. Given a and b such that b = a^m, this method recovers the exponent m.
     * If alpha_k - alpha_2k = 0, SextupleInitializationException is thrown to report the method's failure for the
     * simple initialization of the sextuple.
     * @param a a elliptic curve point, which is the base
     * @param b a elliptic curve point, which is the exponentiation
     * @param d the constants in the Edwards curve x^2 + y^2 = 1 + d * x^2 * y^2
     * @param p the modulus, which is a large prime
     * @param n the order of the elliptic curve
     * @return a BigInteger array, where the first element is the discrete logarithm (exponent) m, and the second
     * element is the number of steps k
     * @throws SextupleInitializationException if the method fails for the simple initialization of the sextuple
     */
    public static BigInteger[] rho(BigInteger[] a, BigInteger[] b, BigInteger d, BigInteger p, BigInteger n)
            throws SextupleInitializationException {
        BigInteger[] alphaBeta_k = new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(0)};
        BigInteger[] z_k = ECPoint.point("0", "1");
        BigInteger[] alphaBeta_2k = new BigInteger[]{BigInteger.valueOf(0), BigInteger.valueOf(0)};
        BigInteger[] z_2k = ECPoint.point("0", "1");

        BigInteger m;
        BigInteger k;

        k = BigInteger.valueOf(0);
        while (!Arrays.equals(z_k, z_2k) || k.equals(BigInteger.valueOf(0))) {
            k = k.add(BigInteger.valueOf(1));

            updateAlphaBetaZ(alphaBeta_k, z_k, a, b, d, p);
            updateAlphaBetaZ(alphaBeta_2k, z_2k, a, b, d, p);
            updateAlphaBetaZ(alphaBeta_2k, z_2k, a, b, d, p);
        }

        if (alphaBeta_k[0].subtract(alphaBeta_2k[0]).mod(n).equals(BigInteger.valueOf(0))) {
            throw new SextupleInitializationException();
        }

        m = (alphaBeta_2k[1].subtract(alphaBeta_k[1])).multiply((alphaBeta_k[0].subtract(alphaBeta_2k[0])).modInverse(n)).mod(n);

        return new BigInteger[]{m, k};
    }

    private static void updateAlphaBetaZ(BigInteger[] alphaBeta, BigInteger[] z, BigInteger[] a, BigInteger[] b, BigInteger d, BigInteger p) {
        BigInteger[] _z;
        if (z[0].mod(BigInteger.valueOf(3)).equals(BigInteger.valueOf(0))) {
            _z = ECPoint.mul(b, z, d, p);
            alphaBeta[0] = alphaBeta[0].add(BigInteger.valueOf(1));
            // alphaBeta[1] = alphaBeta[1];
        } else if (z[0].mod(BigInteger.valueOf(3)).equals(BigInteger.valueOf(1))) {
            _z = ECPoint.mul(z, z, d, p);
            alphaBeta[0] = alphaBeta[0].multiply(BigInteger.valueOf(2));
            alphaBeta[1] = alphaBeta[1].multiply(BigInteger.valueOf(2));
        } else {
            _z = ECPoint.mul(a, z, d, p);
            // alphaBeta[0] = alphaBeta[0];
            alphaBeta[1] = alphaBeta[1].add(BigInteger.valueOf(1));
        }
        z[0] = _z[0];
        z[1] = _z[1];
    }


    /** Generates a random m and calculates b = a^m, then calculates m' = log_a{b} and check if m = m'. If not, throws a
     * RuntimeException.
     * @param a a elliptic curve point, which is the base
     * @param d the constants in the Edwards curve x^2 + y^2 = 1 + d * x^2 * y^2
     * @param p the modulus, which is a large prime
     * @param n the order of the elliptic curve
     * @return the number of steps k that method rho() needs to compute m
     * @throws SextupleInitializationException if the rho() method fails for the simple initialization of the sextuple
     */
    public static long check(BigInteger[] a, BigInteger d, BigInteger p, BigInteger n) throws SextupleInitializationException {
        BigInteger m = new BigInteger(64, new Random());
        m = m.mod(n);
        BigInteger[] b = ECPoint.exp(a, m, d, p);
        BigInteger[] mk = ECPoint.rho(a, b, d, p, n);
        if (!mk[0].equals(m)) {
            throw new RuntimeException();
        }
        return mk[1].longValue();
    }


    /* Tests the methods */
    public static void main(String[] args) {
        BigInteger[] a = ECPoint.point("12", "61833");
        BigInteger d = BigInteger.valueOf(154);
        BigInteger p = BigInteger.valueOf(2).pow(16).subtract(BigInteger.valueOf(17));  // 2**16 - 1
        BigInteger n = BigInteger.valueOf(16339);

        try {
            long result = ECPoint.check(a, d, p, n);
        } catch (SextupleInitializationException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}