/*
 * Slawas.pl Copyright &copy; 2007-2012 
 * http://slawas.pl 
 * All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL SŁAWOMIR CICHY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pl.slawas.twl4j;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pl.slawas.entities.NameValuePair;
import pl.slawas.twl4j.appenders.LogAppender;
import pl.slawas.twl4j.config.LoggerConfig;
import pl.slawas.twl4j.logger.LogLevel;
import pl.slawas.twl4j.logger.LoggerAppender;
import pl.slawas.twl4j.logger.LoggerImplementation;

/**
 * LoggerFactory
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LoggerFactory {

	private static Map<String, Logger> loggers = new HashMap<String, Logger>();
	private static Object loggerLock = new Object();

	public static Logger getLogger(String name) {

		synchronized (loggerLock) {
			Logger logger = loggers.get(name);
			if (logger == null) {
				LogLevel ll;
				String classLevelStr = null;
				String packageLevelStr = null;
				LogLevel classLevel = null;
				LogLevel packageLevel = null;
				int pLevel = 0;
				LogAppender logAppender;
				LoggerAppender appender;
				try {
					ll = LoggerConfig.getLogLevel();
					logAppender = LoggerConfig.getLogAppender();
					/** ustawianie niedomyślnego logowania */
					for (NameValuePair nvp : LoggerConfig.getInstance().getPropertyList()) {
						String propName = nvp.getName();
						if (name.equals(propName)) {
							classLevelStr = nvp.getValue();
							break;
						}
						if (name.startsWith(propName)) {
							int dots = propName.split("\\.").length;
							if (pLevel < dots) {
								packageLevelStr = nvp.getValue();
								pLevel = dots;
							}
						}
					}
					if (StringUtils.isNotBlank(classLevelStr)) {
						try {
							classLevel = LogLevel.valueOf(classLevelStr.toUpperCase());
						} catch (Exception e) {
							System.err.println("[" + name + "] Issue of custom logger inicjalization: \n" + e);

						}
					} else if (StringUtils.isNotBlank(packageLevelStr)) {
						try {
							packageLevel = LogLevel.valueOf(packageLevelStr.toUpperCase());
						} catch (Exception e) {
							System.err
									.println("[" + packageLevelStr + "] Issue of custom logger inicjalization: \n" + e);

						}
					}
					if (classLevel != null) {
						ll = classLevel;
					} else if (packageLevel != null) {
						ll = packageLevel;
					}
				} catch (Exception e) {
					System.err.println("[" + name + "]Issue of logger inicjalization: \n" + e);
					ll = LogLevel.NONE;
					logAppender = LogAppender.SYSTEMOUT;
				}
				appender = logAppender.getAppenderInstance(name, ll);
				logger = new LoggerImplementation(name, appender, LoggerConfig.getDateLoggerFormat(),
						LoggerConfig.getLoggerAddDate());
				loggers.put(name, logger);
			}
			return logger;
		}
	}

	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getSystemLogger(String name, LogLevel ll) {
		LogAppender logAppender = LogAppender.SYSTEMOUT;
		LoggerAppender appender = logAppender.getAppenderInstance(name, ll);
		return new LoggerImplementation(name, appender, LoggerConfig.DEFAULT_DATE_LOGGER_FORMAT,
				LoggerConfig.DEFAULT_LOGGER_ADD_DATE);
	}

	public static Logger getSystemLogger(Class<?> clazz) {
		return getSystemLogger(clazz.getName(), LogLevel.INFO);
	}

	public static Logger getSystemLogger(Class<?> clazz, LogLevel ll) {
		return getSystemLogger(clazz.getName(), ll);
	}
}
