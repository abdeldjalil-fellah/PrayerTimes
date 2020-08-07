package jalil.prayertimes;

/***
 * https://www.aa.quae.nl/en/reken/juliaansedag.html
 *
 * ⌊x/y⌋ = floorDiv(x,y)
 * x mod y = floorMod(x,y)
 * div(x,y) = {floorDiv(x,y), floorMod(x,y)}
 *
 *  this issue is fixed by floorDiv and floorMod:
 *  2020-01-31 => 1441-06-05 (cjdn = 2458880)
 *  2020-02-01 => 1441-06-05 (cjdn = 2458880)
 */

public class HijriQuae {

    static final int J0_1948439 = 1948439;
    static final int J0_1948440 = 1948440;

    static final int R_15 = 15;
    static final int R_14 = 14;
    static final int R_11 = 11;
    static final int R_9 = 9;

    private static int floorDiv(int x, int y) {

        int r = x / y;

        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }

        return r;
    }

    private static int floorMod(int x, int y, int floorDiv) {

        return x - floorDiv * y;
    }

    static int[] toHijri(int y, int m, int d, int adjustOutputByDays) {

        int[] h = toHijri(y, m, d, R_14, J0_1948440);

        return Utilities.adjustHijri(h[0], h[1], h[2], adjustOutputByDays);
    }

    static int[] toGregorian(int y, int m, int d, int inputAdjustedByDays) {

        int[] h = Utilities.adjustHijri(y, m, d, -inputAdjustedByDays);

        return toGregorian(h[0], h[1], h[2], R_14, J0_1948440);
    }

    static int[] toHijri(int y, int m, int d, int r, int j0) {
        return cjdn2h(g2cjdn(y, m, d), r, j0);
    }

    static int[] toGregorian(int y, int m, int d, int r, int j0) {
        return cjdn2g(h2cjdn(y, m, d, r, j0));
    }

    static int[] cjdn2h(int j, int r, int j0) {

        if (j < j0) return new int[]{-1, -1, -1};

        int y2 = j - j0;
        int k2 = 30 * y2 + 29 - r;
        int x2 = floorDiv(k2, 10631);
        int r2 = floorMod(k2, 10631, x2);
        int z2 = floorDiv(r2, 30);
        int k1 = 11 * z2 + 5;
        int x1 = floorDiv(k1, 325);
        int r1 = floorMod(k1, 325, x1);
        int z1 = floorDiv(r1, 11);

        return new int[]{x2 + 1, x1 + 1, z1 + 1};
    }

    static int h2cjdn(int y, int m, int d, int r, int j0) {

        if (toInt(y, m, d) < 10101) return -1;

        return floorDiv(10631 * y - 10631 + r, 30) + floorDiv(325 * m - 320, 11) + d - 1 + j0;
    }

    static int g2cjdn(int y, int m, int d) {

        int c0 = floorDiv(m - 3, 12);
        int x4 = y + c0;
        int x3 = floorDiv(x4, 100);
        int x2 = floorMod(x4, 100, x3);
        int x1 = m - 12 * c0 - 3;

        return floorDiv(146097 * x3, 4) + floorDiv(36525 * x2, 100) + floorDiv(153 * x1 + 2, 5) + d + 1721119;
    }

    static int[] cjdn2g(int j) {

        if (j == -1) return new int[]{-1, -1, -1};

        int k3 = 4 * j - 6884477;
        int x3 = floorDiv(k3, 146097);
        int r3 = floorMod(k3, 146097, x3);
        int k2 = 100 * floorDiv(r3, 4) + 99;
        int x2 = floorDiv(k2, 36525);
        int r2 = floorMod(k2, 36525, x2);
        int k1 = 5 * floorDiv(r2, 100) + 2;
        int x1 = floorDiv(k1, 153);
        int r1 = floorMod(k1, 153, x1);
        int c0 = floorDiv(x1 + 2, 12);

        return new int[]{100 * x3 + x2 + c0, x1 - 12 * c0 + 3, floorDiv(r1, 5) + 1};
    }

    static int toInt(int y, int m, int d) {
        return y * 10000 + m * 100 + d;
    }
}
