package tools.junit;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

/**
 * simple generator to create mock objects using reflection
 * 
 * @author W353322
 * 
 */
public class MockObjectGenerator {

	// TODO:CHANGE HERE
	private static final String CLASS_TO_MOCK = "";

	public static void main(final String[] args) throws IllegalArgumentException, Exception {
		final StringBuilder result = new StringBuilder();
		final String newLine = System.getProperty("line.separator");

		final Class mockClass = getMockClass();
		final String className = mockClass.getSimpleName();
		result.append(String.format("public %s getMock%s()", className, mockClass.getSimpleName()));
		result.append(" {").append(newLine);

		final Field[] fields = mockClass.getDeclaredFields();

		final String instanceName = StringUtils.uncapitalise(className);
		result.append(String.format("\t %s %s = new %s();", className, instanceName, className));
		result.append(newLine);
		for (final Field field : fields) {
			try {
				final Object mockValue = getMockValueForType(field);
				result.append(String.format("\t %s.set%s(%s);", instanceName, StringUtils.capitalise(field.getName()), mockValue));
				// field.setAccessible(true);
			} catch (final Exception ex) {
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("\t return " + instanceName + ";" + newLine).append("}");

		System.out.println(result);
	}

	public static Object getMockValueForType(final Field field) {
		final String type = field.getType().getSimpleName();
		String val;
		if ("Integer".equals(type) || "Short".equals(type) || "Long".equals(type)) {
			val = "1";
		} else if ("Date".equals(type)) {
			val = "new Date()";
		} else {
			val = "\"test-" + field.getName() + "\"";
		}
		return val;
	}

	private static Class getMockClass() throws Exception {
		return Class.forName(CLASS_TO_MOCK);
	}

}
