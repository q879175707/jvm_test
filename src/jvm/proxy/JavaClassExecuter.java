package jvm.proxy;

import java.lang.reflect.Method;

public class JavaClassExecuter {
	public static String executer(byte[] classByte) {
		HackSystem.clearBuffer();
		ClassModifier cm = new ClassModifier(classByte);
		byte[] modifyBytes = cm.modifyUTF8Constant("java/lang/System", "jvm/proxy/HackSystem");
		HotSwapClassLoader loader = new HotSwapClassLoader();
		Class<?> clazz = loader.loadByte(modifyBytes);
		try {
			Method method = clazz.getMethod("main", new Class[]{String[].class});
			method.invoke(null, new String[]{null});
		} catch (Exception e) {
			e.printStackTrace(HackSystem.out);
		}
		
		return HackSystem.getBufferString();
	}
}
