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
	 * ���Ƽ����ʵ����,�̳���ElementScanner ������Visitorģʽ���ʳ����﷨���е�Ԫ��
	 * 
	 * @author Administrator
	 *
	 */
	private class NameCheckScanner extends ElementScanner8<Void, Void> {

		/**
		 * �˷������ڼ��Java��
		 */
		@Override
		public Void visitType(TypeElement e, Void p) {
			scan(e.getTypeParameters(), p);
			checkCamelCase(e, true);
			return super.visitType(e, p);
		}

		/**
		 * ��鷽�������Ƿ�Ϸ�
		 */
		@Override
		public Void visitExecutable(ExecutableElement e, Void p) {
			if (e.getKind() == ElementKind.METHOD) {
				Name name = e.getSimpleName();
				if (name.contentEquals(e.getEnclosingElement().getSimpleName()))
					messager.printMessage(Kind.WARNING, "һ����ͨ����" + name + "��Ӧ���������ظ�,�����빹�캯����������", e);
				checkCamelCase(e, false);
			}
			return super.visitExecutable(e, p);
		}

		/**
		 * �����������Ƿ�Ϸ�
		 */
		@Override
		public Void visitVariable(VariableElement e, Void p) {
			// ������Variable��ö�ٻ���,����д�������
			if (e.getKind() == ElementKind.ENUM_CONSTANT || e.getConstantValue() != null || heuristicallConstant(e))
				checkAllCaps(e);
			else
				checkCamelCase(e, false);
			return super.visitVariable(e, p);
		}

		/**
		 * �ж�һ�������Ƿ��ǳ���
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
		 * ��鴫���Elment�Ƿ������ʽ����
		 */
		private void checkCamelCase(Element e, boolean initialCaps) {
			String name = e.getSimpleName().toString();
			boolean previousUpper = false;
			boolean conventional = true;
			int firstCodePoint = name.codePointAt(0);
			if (Character.isUpperCase(firstCodePoint)) {
				previousUpper = true;
				if (!initialCaps) {
					messager.printMessage(Kind.WARNING, "����" + name + "Ӧ����Сд��ĸ��ͷ", e);
					return;
				}
			} else if (Character.isLowerCase(firstCodePoint)) {
				if (initialCaps) {
					messager.printMessage(Kind.WARNING, "����" + name + "Ӧ���Դ�д��ĸ��ͷ", e);
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
				messager.printMessage(Kind.WARNING, "����\"" + name + "\"Ӧ��������ʽ������", e);

		}

		/**
		 * ��д�������,Ҫ���һ����ĸ�����Ǵ�д��Ӣ���ַ�,���ಿ�ֿ������»��߻��ߴ�д��ĸ
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
				messager.printMessage(Kind.WARNING, "����\"" + name + "\"Ӧ��ȫ���Դ�д��ĸ���»�������,��������ĸ��ͷ", e);
		}
	}
}
