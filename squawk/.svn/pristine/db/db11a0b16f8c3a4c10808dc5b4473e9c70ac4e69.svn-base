/*
 * TranslatorRegressions.java
 *
 * Created on January 29, 2007, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

/**
 *
 * @author dw29446
 */
public class TranslatorRegressions {
 
    public static void main(String[] args) {
    /*    Test1 t1 = new Test1();
        t1.testFor();
        t1.testIf();*/
        Test5.test();
    }

    private TranslatorRegressions() {
    }
    
/*    static class Test1  {
        boolean flag;
        public void testFor() {
            Object flashMem = null;
            for (int i=0; i < 8; i++) {
                Address startAddress = Address.zero();
            }
            
            Object usbPowerDaemon = new Object();
            Object masterIsolateVersionOfDriverRegistry = new Object();
        }
        
        public void testIf() {
            if (flag) {
                Address startAddress = Address.zero();
            } else {
                Object usbPowerDaemon = new Object();
            }
        }   
    }*/
    
}


interface I1 {
}
interface I2 {
}

class C1 implements I1 {
}
class C12 implements I2 , I1 {    
}

class TestTR4 {
    private I1 test(int pidx) {
        I1 pin = new C1();
        if ( pidx > 0 )
            pin = null;
        
        return pin;
    }
}

class TestTR2  {
    private I1 test(int pidx) {
        I1 pin = null;
        if ( pidx > 0 )
            pin = new C12();
        
        return pin;
    }
}

/*
 *Translatotion fails with "invalid return for non-void method"
 class TestTR3a  {
    private I1 test(int pidx) {
        I1 pin = new C1();
        if ( pidx > 0 )
            pin = new C12();
        else
            pin = new C1();
        
        return pin;
    }
}

class TestTR3  {
    private I1 test(int pidx) {
        I1 pin = null;
        if ( pidx > 0 )
            pin = new C12();
        else
            pin = new C1();
        
        return pin;
    }
}
 **/


class Test5 {
    static int testi(int x) {
        x = x + 0;
        x = x * 1;
        x = x & -1;
        x = x | 0;
        x = x ^ 0;
        return x;
    }
    
      static long testl(long x) {
        x = x + 0;
        x = x * 1;
        x = x & -1;
        x = x | 0;
        x = x ^ 0;
        return x;
    }
    static void test() {
        testi(7);
        testl(7);
        
    }
}


