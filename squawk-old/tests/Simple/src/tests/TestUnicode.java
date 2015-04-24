package tests;

/**
 * Test that basic unicode strings work. Especially test by swapping a suite endianness.
 */
public class TestUnicode {
	private final static String a = " \u03B8=";
	private final static String b = " \u03B8=";
    
    //private final static String a = " A=";
	//private final static String b = " B=";
    
	public static void main(String[] args) {
        if (a.length() != 3) {
            throw new Error("Unicode encoding error. length = " + a.length());
        }
		System.out.println(a);
		System.out.println(b);
		System.out.println(a.concat(b));

		System.out.println("a: " + a);
		System.out.println("b: " + b);
		System.out.println("concat: " + a.concat(b));

		System.out.println(a + b);
		System.out.println(a.charAt(0));
		System.out.println(a.charAt(1));

		String c = a + b;
		System.out.println(c.charAt(0));
		System.out.println(c.charAt(1));
		System.out.println(c.charAt(2));
		System.out.println(c.charAt(3));


		if (a.indexOf(b.charAt(0)) != 0) {
			throw new Error("indexOf error");
		}

		String e = c.toUpperCase();
		System.out.println("uppercase: " + e);
	}

    private TestUnicode() {
    }
}