package jvm.test;

public class Test {
	public static void main(String[] args) {
		try {
			test1();
		} catch (Exception e) {
			System.out.println(e.getMessage().toString());
		}
		System.out.println("��������ִ����");
	}
	public static void test1() {
		try {
			int x ;
			System.out.println("���������쳣");
			x = 1/0;
		} catch (Exception e) {
			throw new ArithmeticException("����������Ϊ0");
		}
		System.out.println("ִ������");
	}
}
