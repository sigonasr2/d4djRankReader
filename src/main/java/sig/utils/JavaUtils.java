package sig.utils;

import java.lang.reflect.Field;

public class JavaUtils {
	public JavaUtils clone() {
		JavaUtils newpos = new JavaUtils();
		for (Field f : this.getClass().getDeclaredFields()) {
			if (ReflectUtils.isCloneable(f)) {
				try {
					f.set(newpos, f.get(this));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return newpos;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()+"(");
		boolean first=true;
		for (Field f : this.getClass().getDeclaredFields()) {
			if (!first) {
				sb.append(",");
			}
			try {
				sb.append(f.getName()+"="+f.get(this));
				first=false;
			} catch (IllegalArgumentException|IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
