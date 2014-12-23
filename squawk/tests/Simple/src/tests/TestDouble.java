/*
 * TestDouble.java
 *
 * Created on September 25, 2007, 10:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

/**
 *
 * @author dw29446
 */
public class TestDouble {
    
    /** Creates a new instance of TestDouble */
    public TestDouble() {
    }
    
    public static void main(String[] args) {
        int failed = 0;
        String s;
        double nums[] = {Double.NEGATIVE_INFINITY,
                -1.7976931348623157E308d,
                -1.0d,
                -4.9E-324d,
                -0.0d,
                0.0d,
                4.9E-324d,
                1.0d,
                1.7976931348623157E308d,
                Double.POSITIVE_INFINITY,
                Double.NaN};
        String strs[] = {"-Infinity",
                "-1.7976931348623157E308",
                "-1.0",
                "-4.9E-324",
                "-0.0",
                "0.0",
                "4.9E-324",
                "1.0",
                "1.7976931348623157E308",
                "Infinity",
                "NaN"};
        

     double da =  0.0d;
     System.err.println("0.0d = " + Double.doubleToLongBits(da));
     System.err.println("1.0d = " + Double.doubleToLongBits(1.0d));
     da = da + 1.0d;
     System.err.println(" da + 1.0d; = " + Double.doubleToLongBits(da));
     da = da - 1;
     System.err.println("da - 1 = " + Double.doubleToLongBits(da));

          System.err.println("2.0d = " + Double.doubleToLongBits(2.0d));
     da = 1.0d;
     da = da / 2;
     System.err.println("1/2 = " + Double.doubleToLongBits(da));
     
     da = 1.0d;
     da = da * 2;
     System.err.println("1*2 = " + Double.doubleToLongBits(da));
     
      da = Double.MIN_VALUE;
     da = da / 2;
     System.err.println("MinValue/2 = " + Double.doubleToLongBits(da));
     System.err.println("MinValue/2*2 = " + Double.doubleToLongBits(da * 2));
     
     
     if (da * 2 != 0.0D) {
         System.err.println("Correct underflow Failed ");
     }
       
     long num = -2251799813685248L;
     System.err.println("double val = " + Double.longBitsToDouble(num));
    
        for (int i = 0; i < nums.length; i++) {
            s = Double.toString(nums[i]);
            if (!s.equals(strs[i])) {
                failed ++;
                System.err.println("For " + nums[i] + " returned " + s
                        + " instead of " + strs[i]);
            }
        }
    }
}
