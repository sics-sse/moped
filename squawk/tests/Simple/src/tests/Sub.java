package tests;

public class Sub extends tests.foo.Super {

    Sub() {
        super(7);

//        tests.foo.Super s = new tests.foo.Super(8);
    }

    public static void main(String[] args) {
        Sub s = new Sub();
        System.out.println("value=" + s.value);
    }
}
