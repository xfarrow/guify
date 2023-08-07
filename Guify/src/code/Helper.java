package code;

import java.nio.file.Path;

public class Helper {

	/**
	 * Combine directories with POSIX-style forward slash
	 */
	public static String combinePath(String s1, String s2) {
		StringBuilder result = new StringBuilder(s1);
		if (!s1.endsWith("/")) {
			result.append('/');
		}
		result.append(s2);
		return result.toString();
	}

	public static String getParentPath(String path) {
		if (path.equals("/")) {
			return "/";
		} else if (path.equals("~")) {
			return Path.of(SshEngine.executeCommand("pwd")).getParent()
					.toString().replace('\\', '/');
		} else {
			return Path.of(path).getParent().toString().replace('\\', '/');
		}
	}
}
