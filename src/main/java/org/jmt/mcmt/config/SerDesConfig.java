package org.jmt.mcmt.config;


import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 
 * So forge is being unhelpful, so I need to write my own damn config system
 * 
 * I'll try to use nightcoreconfig or whateverit'scalled as it's bundled
 * 
 * But may end up being stuck with JSON
 * 
 * 
 * 
 * @author jediminer543
 *
 */
public class SerDesConfig {

	/**
	 * Standard, run of the mill filter config
	 * 
	 * TODO: add more filter config parameters; like backup pools etc.
	 * 
	 * @author jediminer543
	 *
	 */

	public static class FilterConfig {
		public FilterConfig() {}

		public FilterConfig(int priority, String name, List<String> whitelist, List<String> blacklist, String pool,
				/*Config*/ Object poolParams) {
			this.priority = priority;
			this.name = name;
			this.whitelist = whitelist;
			this.blacklist = blacklist;
			this.pool = pool;
			//this.poolParams = poolParams;
		}

		int priority;

		String name;

		List<String> whitelist;
		List<String> blacklist;
		String pool;
		//Config poolParams; // nightconfig does not support Maps, use Configs instead

		public int getPriority() {
			return priority;
		}

		public List<String> getWhitelist() {
			return whitelist;
		}

		public List<String> getBlacklist() {
			return blacklist;
		}

		public String getPool() {
			return pool;
		}

		public Map<String, Object> getPoolParams() {
			try {
				return null; //poolParams.valueMap();
			} catch (NullPointerException npe) {
				return new HashMap<String, Object>();
			}
		}
	}

	public static class PoolConfig {
		String clazz;
		String name;
		//Config initParams;
		int priority;

		public String getClazz() {
			return clazz;
		}

		public String getName() {
			return name;
		}

		public Map<String, Object> getInitParams() {
			return null;
		}

		public int getPriority() {
			return priority;
		}

	}

	private static final class ClassListValidator implements Predicate<Object> {
		String validatorRegex = "^[a-z\\_]+(\\.[a-z0-9\\_]+)*((\\.[A-Z][A-Za-z0-9\\_]+($[A-Za-z0-9\\_]+)*)|\\.\\*|\\.\\*\\*)$";
		@Override
		public boolean test(Object t) {
			if (t == null) {
				return true;
			}

			if (t instanceof List<?>) {
				List<?> list = (List<?>) t;
				for (Object s : list) {
					if (!(s instanceof String && ((String)s).matches(validatorRegex))) {
//						System.out.println("Value: " + t.toString() + " | String: " + (s instanceof String) + " | Matches: " +
//								(s instanceof String ? ((String)s).matches(validatorRegex) : "invalid"));

						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	private static final class ClassValidator implements Predicate<Object> {
		String validatorRegex = "^[a-z\\_]+(\\.[a-z0-9\\_]+)*((\\.[A-Za-z0-9\\_]+(\\$[A-Za-z0-9\\_]+)*))?$";
		@Override
		public boolean test(Object s) {
			if ((s instanceof String && ((String)s).matches(validatorRegex))) {
				return true;
			}
			return false;
		}
	}
	
	//static Map<String, FileConfig> configs = new HashMap<>();
	static Map<String, List<FilterConfig>> filters = new HashMap<>();
	static Map<String, List<PoolConfig>> pools = new HashMap<>();
	
	public static List<PoolConfig> getPools() {
		return pools.values().stream()
		.flatMap(List::stream)
		.sorted(Comparator.comparingInt(PoolConfig::getPriority))
		.collect(Collectors.toList());
	}
	
	public static List<FilterConfig> getFilters() {
		return filters.values().stream()
		.flatMap(List::stream)
		.sorted(Comparator.comparingInt(FilterConfig::getPriority))
		.collect(Collectors.toList());
	}
	
	public static void saveConfigs() {
		//TODO
		
	}
}
