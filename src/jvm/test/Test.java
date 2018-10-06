package jvm.test;

public class Test {
	public static void main(String[] args) {
		try {
			test1();
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
		}
		System.out.println("程序正常执行了");
	}
	public static void test1() {
		try {
			int x ;
			System.out.println("即将发生异常");
			x = 1/0;
		} catch (Exception e) {
			throw new ArithmeticException("操作数不能为0");
		}
		System.out.println("执行了吗？");
	}
}
