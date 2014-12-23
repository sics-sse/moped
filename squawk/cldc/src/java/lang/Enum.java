//if[JAVA5SYNTAX]*/
package java.lang;

import java.util.Hashtable;

import com.sun.squawk.Java5Marker;
import com.sun.squawk.Klass;

/**
 * A version of the 1.5 java.lang.Enum class for the 1.4 VM.
 */
@Java5Marker
public class Enum<E extends Enum<E>> {

	public final transient int ordinal_;

	private final String name_;

	private static final Hashtable enumValues = new Hashtable();

	protected Enum(final String name, final int ordinal) {
		this.name_ = name;
		this.ordinal_ = ordinal;
	}

	// Call weaved in by retroweaver
    protected static final void setEnumValues(final Object[] values, final Class<?> c) {
		synchronized(enumValues) {
			enumValues.put(c, values);
		}
	}

	protected static final Enum<?>[] getEnumValues(final Class<?> class_) {
		synchronized(enumValues) {
			return (Enum []) enumValues.get(class_);
		}
	}

	public static <T extends Enum<T>> T valueOf(final Class<T> enumType, final String name) {

		if (enumType == null) {
			throw new NullPointerException("enumType is null"); // NOPMD by xlv
		}

		if (name == null) {
			throw new NullPointerException("name_ is null"); // NOPMD by xlv
		}

		final Enum<?>[] enums = getEnumValues(enumType);

		if (enums != null) {
			for (Enum<?> enum_ : enums) {
				if (enum_.name_.equals(name)) {
                    return (T) enum_;
				}
			}
		}
		throw new IllegalArgumentException("No enum const " + enumType + "." + name);
	}

	public final boolean equals(final Object other) {
		return other == this;
	}

	public final int hashCode() {
		return System.identityHashCode(this);
	}

	public String toString() {
		return name_;
	}

	public final int compareTo(final E e) {
		final Class<?> c1 = getDeclaringClass();
		final Class<?> c2 = e.getDeclaringClass();

		if (c1 == c2) { // NOPMD by xlv
			return ordinal_ - e.ordinal();
		}

		throw new ClassCastException();
	}

	public final String name() {
		return name_;
	}

	public final int ordinal() {
		return ordinal_;
	}

	public final Class<?> getDeclaringClass() {
		final Class<?> clazz = getClass();
		final Class<?> superClass = Klass.asClass(Klass.asKlass(clazz).getSuperclass());
		if (superClass == Enum.class) {
			return clazz;
		} else {
			return superClass;
		}
	}

}
