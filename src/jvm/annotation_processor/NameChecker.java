package jvm.annotation_processor;

import java.util.EnumSet;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner8;
import javax.tools.Diagnostic.Kind;

public class NameChecker {
	private final Messager messager;
	NameCheckScanner nameCheckScanner = new NameCheckScanner();

	public NameChecker(ProcessingEnvironment processingEnv) {
		this.messager = processingEnv.getMessager();
	}

	public void checkNames(Element element) {
		nameCheckScanner.scan(element);
	}

	/**
	 * 名称检查器实现类,继承了ElementScanner 将会以Visitor模式访问抽象语法树中的元素
	 * 
	 * @author Administrator
	 *
	 */
	private class NameCheckScanner extends ElementScanner8<Void, Void> {

		/**
		 * 此方法用于检查Java类
		 */
		@Override
		public Void visitType(TypeElement e, Void p) {
			scan(e.getTypeParameters(), p);
			checkCamelCase(e, true);
			return super.visitType(e, p);
		}

		/**
		 * 检查方法命名是否合法
		 */
		@Override
		public Void visitExecutable(ExecutableElement e, Void p) {
			if (e.getKind() == ElementKind.METHOD) {
				Name name = e.getSimpleName();
				if (name.contentEquals(e.getEnclosingElement().getSimpleName()))
					messager.printMessage(Kind.WARNING, "一个普通方法" + name + "不应当与类名重复,避免与构造函数产生混淆", e);
				checkCamelCase(e, false);
			}
			return super.visitExecutable(e, p);
		}

		/**
		 * 检查变量命名是否合法
		 */
		@Override
		public Void visitVariable(VariableElement e, Void p) {
			// 如果这个Variable是枚举或常量,按大写命名检查
			if (e.getKind() == ElementKind.ENUM_CONSTANT || e.getConstantValue() != null || heuristicallConstant(e))
				checkAllCaps(e);
			else
				checkCamelCase(e, false);
			return super.visitVariable(e, p);
		}

		/**
		 * 判断一个变量是否是常量
		 */
		private boolean heuristicallConstant(VariableElement e) {
			if (e.getEnclosingElement().getKind() == ElementKind.INTERFACE)
				return true;
			else if (e.getKind() == ElementKind.FIELD && e.getModifiers()
					.containsAll(EnumSet.of(ModifierEnum.PUBLIC, ModifierEnum.STATIC, ModifierEnum.FINAL)))
				return true;
			else {
				return false;
			}
		}

		/**
		 * 检查传入的Elment是否符合驼式命名
		 */
		private void checkCamelCase(Element e, boolean initialCaps) {
			String name = e.getSimpleName().toString();
			boolean previousUpper = false;
			boolean conventional = true;
			int firstCodePoint = name.codePointAt(0);
			if (Character.isUpperCase(firstCodePoint)) {
				previousUpper = true;
				if (!initialCaps) {
					messager.printMessage(Kind.WARNING, "名称" + name + "应当以小写字母开头", e);
					return;
				}
			} else if (Character.isLowerCase(firstCodePoint)) {
				if (initialCaps) {
					messager.printMessage(Kind.WARNING, "名称" + name + "应当以大写字母开头", e);
					return;
				}
			} else
				conventional = false;
			if (conventional) {
				int cp = firstCodePoint;
				for (int i = Character.charCount(cp); i < name.length(); i += Character.charCount(cp)) {
					cp = name.codePointAt(i);
					if (Character.isUpperCase(cp)) {
						if (previousUpper) {
							conventional = false;
							break;
						}
						previousUpper = true;
					} else
						previousUpper = false;
				}
			}

			if (!conventional)
				messager.printMessage(Kind.WARNING, "名称\"" + name + "\"应当符合驼式命名法", e);

		}

		/**
		 * 大写命名检查,要求第一个字母必须是大写的英文字符,其余部分可以是下划线或者大写字母
		 */
		private void checkAllCaps(Element e) {
			String name = e.getSimpleName().toString();
			boolean conventional = true;
			int firstCodePoint = name.codePointAt(0);
			if (!Character.isUpperCase(firstCodePoint))
				conventional = false;
			else {
				boolean previousUnderscore = false;
				int cp = firstCodePoint;
				for (int i = Character.charCount(cp); i < name.length(); i += Character.charCount(cp)) {
					cp = name.codePointAt(i);
					if (cp == (int) '_') {
						if (previousUnderscore) {
							conventional = false;
							break;
						}
						previousUnderscore = true;
					} else {
						previousUnderscore = false;
						if (!Character.isUpperCase(cp) && !Character.isDigit(cp)) {
							conventional = false;
							break;
						}
					}
				}
			}
			if (!conventional)
				messager.printMessage(Kind.WARNING, "常量\"" + name + "\"应当全部以大写字母或下划线命名,并且以字母开头", e);
		}
	}
}
