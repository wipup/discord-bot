package wp.discord.bot.util;

public class SafeUtil {

	@FunctionalInterface
	public static interface SafeSupplier<T> {
		public T get() throws Exception;
	}

	@FunctionalInterface
	public static interface SafeSuppresser {
		public void call() throws Exception;
	}

	public static <T> T runtimeException(SafeSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void runtimeException(SafeSuppresser supplier) {
		try {
			supplier.call();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static void suppress(SafeSuppresser supplier) {
		try {
			supplier.call();
		} catch (Exception e) {
		}
	}

	public static <T> T get(SafeSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
		}
		return null;
	}

	public static <T> T get(SafeSupplier<T> supplier, T fallbackValue) {
		try {
			return supplier.get();
		} catch (Exception e) {
		}
		return fallbackValue;
	}

	public static <T> T nonNull(SafeSupplier<T> supplier, T fallbackValue) {
		T value = get(supplier, fallbackValue);
		if (value != null) {
			return value;
		}
		return fallbackValue;
	}

	@SafeVarargs
	public static final <T> T firstNonNull(SafeSupplier<T> supplier, SafeSupplier<T>... suppliers) {
		T value = nonNull(supplier, null);
		if (value != null) {
			return value;
		}
		for (SafeSupplier<T> sup : suppliers) {
			value = nonNull(sup, null);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
