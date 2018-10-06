package jvm.test1;

import java.io.Serializable;

/**
 * 注释掉char，会执行int；
 * 注释掉int，会执行long；
 * 注释掉long，会执行Character；
 * 注释掉Character，会执行Serializable；(Serializable是character的接口)
 * 注释掉Serializable，会执行Object；
 * 注释掉Object，会执行char...；
 * @author Administrator
 *
 */
public class Overload {
	public static void sayHello(Object arg) {
		System.out.println("hello Object");
	}
	public static void sayHello(int arg) {
		System.out.println("hello int");
	}
	public static void sayHello(long arg) {
		System.out.println("hello long");
	}
	public static void sayHello(Character arg) {
		System.out.println("hello Character");
	}
	public static void sayHello(char arg) {
		System.out.println("hello char");
	}
	public static void sayHello(char... arg) {
		System.out.println("hello char...");
	}
	public static void sayHello(Serializable arg) {
		System.out.println("hello Serializable");
	}
	
	public static void main(String[] args) {
		sayHello('a');
	}
}
