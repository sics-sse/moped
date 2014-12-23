package tests;

public class TestInstantiationException {
    
    public void test() {
        try {
            TopLevelClass.class.newInstance(); // fails with
            // IllegalAccessException
            System.out.println("PASSED: Created TopLevelClass");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            StaticClass.class.newInstance();
            System.out.println("PASSED: Created StaticClass");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            NestedClass.class.newInstance();
            System.out.println("FAILED: Created NestedClass");
        } catch (InstantiationException e) {
            System.out.println("PASSED: got InstantiationException creating NestedClass:" + e);
        } catch (Exception e) {
            System.out.println("FAILED: got wrong exception creating NestedClass:");
            e.printStackTrace();
        }
        
        try {
            TopLevelClass2.class.newInstance();
            System.out.println("FAILED: Created TopLevelClass2");
        } catch (InstantiationException e) {
            System.out.println("PASSED: got InstantiationException creating TopLevelClass2:" + e);
        } catch (Exception e) {
            System.out.println("FAILED: got wrong exception creating TopLevelClass2:");
            e.printStackTrace();
        }
        
        try {
            TopLevelClass3.class.newInstance();
            System.out.println("FAILED: Created TopLevelClass3");
        } catch (IllegalAccessException e) {
            System.out.println("PASSED: got IllegalAccessException creating TopLevelClass3:" + e);
        } catch (Exception e) {
            System.out.println("FAILED: got wrong exception creating TopLevelClass3:");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new TestInstantiationException().test();
    }
    
    static class StaticClass {
    }
    
    class NestedClass {
    }
    
}

class TopLevelClass {
}

class TopLevelClass2 {
    //no default constructor
    public TopLevelClass2(int a) {
    }
}

class TopLevelClass3 {
    // caller can't access default constructor
    private TopLevelClass3() {
    }
}