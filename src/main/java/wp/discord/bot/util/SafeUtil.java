package wp.discord.bot.util;

public class SafeUtil {

	@FunctionalInterface
	public static interface SafeSupplier<T> {
		public T get() throws Exception;
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
}
