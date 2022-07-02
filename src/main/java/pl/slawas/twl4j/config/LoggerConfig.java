package pl.slawas.twl4j.config;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pl.slawas.entities.NameValuePair;
import pl.slawas.entities.NameValuePairUtils;
import pl.slawas.helpers.Configurations;
import pl.slawas.helpers.ImmutableList;
import pl.slawas.twl4j.appenders.LogAppender;
import pl.slawas.twl4j.logger.LogLevel;

/**
 * 
 * LoggerConfig
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
@SuppressWarnings("java:S106")
public class LoggerConfig implements LoggerConfigConstants {

	private static Map<String, String> logProps = null;
	private static LoggerConfig instance;
	private static final Object instanceLock = new Object();

	private static final String configFileName;
	static {
		if (StringUtils.isNotBlank(System.getProperty(PROP_CONFIG_LOCATION_FILE))) {
			configFileName = System.getProperty(PROP_CONFIG_LOCATION_FILE);
		} else {
			configFileName = CONFIG_FILE_NAME_PATH;
		}
	}

	/**
	 * Domyślny konstruktor pobierający konfigurację z pliku
	 */
	private LoggerConfig() {
		loadPropertiesFromFile();
	}

	/**
	 * Metoda ładująca dodatkowe właściwości z pliku, właściwości domyśle zostaną
	 * nadpisane
	 * 
	 * @throws Exception
	 */
	private void loadPropertiesFromFile() {
		synchronized (instanceLock) {
			System.out.println("Loading configuration from file:" + configFileName + "...");
			if (logProps == null) {
				logProps = new Hashtable<>();
			} else {
				logProps.clear();
			}
			Map<String, String> externalParam = Configurations.loadHashtable(LoggerConfig.class, configFileName);
			logProps.putAll(externalParam);
			System.out.println("[LoggerConfig] logger.level=" + logProps.get(PROP_LOGGER_LEVEL));
		}
	}

	private ImmutableList<NameValuePair> generatePropertyList() {
		synchronized (instanceLock) {
			ImmutableList<NameValuePair> propertyList = new ImmutableList<>();
			StringBuilder sb = new StringBuilder("\n-------- Logger properties --------");
			Set<Entry<String, String>> propEntrySet = logProps.entrySet();
			for (Entry<String, String> propEntry : propEntrySet) {
				String key = propEntry.getKey();
				String value = propEntry.getValue();
				sb.append("\n ").append(key).append(" = ").append(value);
				NameValuePair currValue = NameValuePairUtils.createNewInstance(key, value);
				propertyList.pAdd(currValue);
			}
			if (StringUtils.isNotBlank(logProps.get(PROP_CONFIG_PRINT_PROPERTIES))
					&& Boolean.parseBoolean(logProps.get(PROP_CONFIG_PRINT_PROPERTIES))) {
				sb.append("\n-----------------------------------");
				System.out.println(sb.toString());
			}
			return propertyList;
		}
	}

	/**
	 * Pobranie domyślnej instancji konfiguracji Mercure'ego
	 * {@link #MercuryConfig()}
	 * 
	 * @return singleton obiektu z konfiguracją Mercure'ego
	 */
	public static LoggerConfig getInstance() {
		synchronized (instanceLock) {
			if (instance == null) {
				System.out.println("[LoggerConfig] Get new instance.");
				instance = new LoggerConfig();
			}
			return instance;
		}
	}

	public String get(String propertyCode) {
		return logProps.get(propertyCode);
	}

	public void put(String propertyCode, String value) {
		logProps.put(propertyCode, value);

	}

	public List<NameValuePair> getPropertyList() {
		return generatePropertyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	@SuppressWarnings("java:S1182")
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Klonowanie obiektu zabronione");
	}

	/**
	 * Pobieranie formatu daty długiej.
	 * 
	 * @return
	 */
	public static String getDateLoggerFormat() {
		return (LoggerConfig.getInstance().get(LoggerConfigConstants.DEFAULT_DATE_LOGGER_FORMAT) == null
				? LoggerConfigConstants.DEFAULT_DATE_LOGGER_FORMAT
				: LoggerConfig.getInstance().get(LoggerConfigConstants.PROP_DATE_LOGGER_FORMAT));
	}

	/**
	 * @return poziom logowania
	 */
	public static LogLevel getLogLevel() {
		String logLevelParam = LoggerConfig.getInstance().get(LoggerConfigConstants.PROP_LOGGER_LEVEL);
		LogLevel logLevel = LogLevel.NONE;
		if (StringUtils.isNotBlank(logLevelParam)) {
			logLevel = LogLevel.valueOf(logLevelParam.toUpperCase());
		}
		return logLevel;
	}

	/**
	 * Pobieranie flagi, czy ma być doklejana data wystąpienia zdarzenia podczas
	 * jego logowania.
	 * 
	 * @return
	 */
	public static boolean getLoggerAddDate() {
		return (LoggerConfig.getInstance().get(LoggerConfigConstants.PROP_LOGGER_ADDDATE) == null
				? DEFAULT_LOGGER_ADD_DATE
				: Boolean.parseBoolean(LoggerConfig.getInstance().get(LoggerConfigConstants.PROP_LOGGER_ADDDATE)));
	}

	/**
	 * @return appender (implementacja wyjścia logowania)
	 */
	public static LogAppender getLogAppender() {
		String logAppenderParam = LoggerConfig.getInstance().get(LoggerConfigConstants.PROP_LOGGER_APPENDER);
		LogAppender logAppender = LogAppender.SYSTEMOUT;
		if (StringUtils.isNotBlank(logAppenderParam)) {
			logAppender = LogAppender.valueOf(logAppenderParam.toUpperCase());
		}
		return logAppender;
	}

}
