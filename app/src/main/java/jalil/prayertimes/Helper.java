package jalil.prayertimes;

public class Helper {

    public static void main(String[] args) {

        // check if issue
        // 2020-01-31 => 1441-06-05 (cjdn = 2458880)
        // 2020-02-01 => 1441-06-05 (cjdn = 2458880)

        /*for (int i = 500; i <= 2500; i++) {

            int c1 = HijriQuae.g2cjdn(i, 1, 31);
            int c2 = HijriQuae.g2cjdn(i, 2, 1);

            if (c1 == c2) {
                System.out.println(i);
            }
        }*/

        for (int i = 1; i <= 1600; i++) {

            int c1 = HijriQuae.h2cjdn(i, 2, 29, HijriQuae.R_14, HijriQuae.J0_1948440);
            int c2 = HijriQuae.h2cjdn(i, 3, 1, HijriQuae.R_14, HijriQuae.J0_1948440);

            if (c1 == c2) {
                System.out.println(i);
            }
        }
    }
}
