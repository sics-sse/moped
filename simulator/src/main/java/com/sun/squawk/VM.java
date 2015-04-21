package com.sun.squawk;

public class VM {

	public static void print(String out) {
		System.out.print(out);
	}
	
	public static void print(double out) {
		System.out.print(out);
	}
	
	public static void println(String out) {
		System.out.println(out);
	}
	
	public static void println() {
		System.out.println("");
	}
	
    public native static void jnaSetSelect(int selector);

	// jna methods
}
