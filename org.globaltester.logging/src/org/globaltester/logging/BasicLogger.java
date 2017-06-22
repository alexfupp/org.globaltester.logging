package org.globaltester.logging;

import org.globaltester.logging.tags.LogLevel;
import org.globaltester.logging.tags.LogTag;
import org.osgi.service.log.LogService;
/**
 * This class is a Logger with basic functionalities.
 * 
 * @author amay
 * 
 */
public final class BasicLogger {

	public static final String ORIGIN_CLASS_TAG_ID = "Originating class";
	public static final String SOURCE_TAG_ID = "Source";
	public static final String EXCEPTION_STACK_TAG_ID = "Exception stack trace";
	public static final String ORIGIN_THREAD_GROUP_TAG_ID = "Originating thread group";
	public static final String LOG_LEVEL_TAG_ID = "Logging level";
	public static final String UI_TAG_ID = "User interface message";
	
	private static final LogLevel LOGLEVEL_DFLT = LogLevel.DEBUG;

	
	//XXX should be removed in favor of direct access to LogLevel
	public static final LogLevel TRACE = LogLevel.TRACE;
	public static final LogLevel DEBUG = LogLevel.DEBUG;
	public static final LogLevel INFO = LogLevel.INFO;
	public static final LogLevel WARN = LogLevel.WARN;
	public static final LogLevel ERROR = LogLevel.ERROR;
	public static final LogLevel FATAL = LogLevel.FATAL;
	
	/**
	 * Ensure that this type can not be instantiated
	 */
	private BasicLogger() {
	}
	
	public static void log(String messageContent, LogLevel level , LogTag... logTags) {
		Message newMessage = new Message(messageContent, logTags);
		newMessage.addLogTag(new LogTag(ORIGIN_CLASS_TAG_ID, getOriginClass()));
		newMessage.addLogTag(new LogTag(ORIGIN_THREAD_GROUP_TAG_ID, Thread.currentThread().getThreadGroup().getName()));
		newMessage.addLogTag(new LogTag(LOG_LEVEL_TAG_ID, level.name()));
		String encodedMessage = MessageCoderJson.encode(newMessage);
		
		logPlain(encodedMessage, convertLogLevelToOsgi(level));
	}

	/**
	 * This converts {@link LogLevel} to the best fitting OSGi log level. This
	 * is a lossy operation since OSGi only specifies 4 levels.
	 * 
	 * @param gtLevel
	 * @return
	 */
	public static int convertLogLevelToOsgi(LogLevel gtLevel) {
		switch (gtLevel) {
		case WARN:
			return LogService.LOG_WARNING;
		case DEBUG:
			return LogService.LOG_DEBUG;
		case FATAL:
		case ERROR:
			return LogService.LOG_ERROR;
		case TRACE:
		case INFO:
		default:
			return LogService.LOG_INFO;
		}
	}


	/**
	 * This converts OSGi log level to {@link LogLevel}.
	 * 
	 * @param osgiLogLevel
	 * @return the fitting
	 */
	public static LogLevel convertOsgiToLogLevel(int osgiLogLevel){
		switch (osgiLogLevel) {
		case LogService.LOG_WARNING:
			return LogLevel.WARN;
		case LogService.LOG_DEBUG:
			return LogLevel.DEBUG;
		case LogService.LOG_ERROR:
			return LogLevel.ERROR;
		case LogService.LOG_INFO:
			return LogLevel.INFO;
		default:
			return LogLevel.TRACE;
		}
	}
	
	/**
	 * Find the first external class where the call to the logging methods
	 * occured or return the calling class to this method if no external
	 * could be found.
	 * 
	 * @return the originating class name
	 */
	private static String getOriginClass(){
		StackTraceElement [] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement curElem : stack){
			if (curElem.getClassName().startsWith("java.")){
				continue;
			}
			
			if (curElem.getClassName().startsWith("org.globaltester.logging.")){
				continue;
			}
			
			return curElem.getClassName();
		}
		return "unknown origin class";
	}
	
	/**
	 * Write message to the log, including origin of that message.
	 * 
	 * This method uses LOGLEVEL_DLFT as LogLevel.
	 * 
	 * @param source
	 *            origin of this log message
	 * @param message
	 *            the message to be logged
	 */
	public static void log(InfoSource source, String message) {
		log(source, message, LOGLEVEL_DFLT);
	}

	/**
	 * Write message to the log, including origin of that message.
	 * 
	 * @param source
	 *            origin of this log message
	 * @param message
	 *            the message to be logged
	 * @param logLevel
	 *            log level on which the message is shown
	 */
	public static void log(InfoSource source, String message, LogLevel logLevel) {
		log(source.getIDString(), message, logLevel);
	}
	
	/**
	 * Write message to the log, including originating class of that message.
	 * 
	 * @param className
	 *            originating class of this log message
	 * @param message
	 *            the message to be logged
	 * @param logLevel
	 *            log level on which the message is shown
	 */
	public static void log(Class<?> className, String message, LogLevel logLevel) {
		log(className.getCanonicalName(), message, logLevel);
	}
	
	/**
	 * Write message to the log, including originating class of that message.
	 * 
	 * This method uses LOGLEVEL_DLFT as LogLevel.
	 * 
	 * @param className
	 *            originating class of this log message
	 * @param message
	 *            the message to be logged
	 */
	public static void log(Class<?> className, String message) {
		log(className, message, LOGLEVEL_DFLT);
	}

	/*--------------------------------------------------------------------------------*/

	/**
	 * Write exception to the log, including origin of that message.
	 * 
	 * This method uses LOGLEVEL_DLFT as LogLevel.
	 * 
	 * @param source
	 *            origin of this log message
	 * @param e
	 *            the Exception to be logged
	 */
	public static void logException(InfoSource source, Exception e) {
		logException(source, e, LOGLEVEL_DFLT);
	}

	/**
	 * Write exception to the log, including origin of that message.
	 * 
	 * @param source
	 *            origin of this log message
	 * @param e
	 *            the Exception to be logged
	 * @param logLevel
	 *            log level on which the exception is shown
	 */
	public static void logException(InfoSource source, Exception e, LogLevel logLevel) {
		logException(source.getIDString(), e.getMessage(), e, logLevel);
	}
	
	/**
	 * Write exception to the log, including origin of that message.
	 * 
	 * @param className
	 *            originating class of this log message
	 * @param e
	 *            the Exception to be logged
	 * @param logLevel
	 *            log level on which the exception is shown
	 */
	public static void logException(Class<?> className, Exception e, LogLevel logLevel) {
		logException(className.getCanonicalName(), e.getMessage(), e, logLevel);
	}
	
	/**
	 * Write exception to the log, including origin of that message.
	 * 
	 * @param message
	 *            the message to be logged
	 * @param e
	 *            the Exception to be logged
	 * @param logLevel
	 *            log level on which the exception is shown
	 */
	public static void logException(String message, Exception e, LogLevel logLevel) {
		logException(getOriginClass(), message, e, logLevel);
	}
	
	/**
	 * Write message to the log, formatted including origin of that message.
	 * 
	 * @param source
	 *            originating origin of this log message
	 * @param message
	 *            the message to be logged
	 * @param logLevel
	 *            log level on which the message is shown
	 */
	private static void log(String source, String message, LogLevel logLevel) {
		log(message, logLevel, new LogTag(SOURCE_TAG_ID, source));
	}
	
	/**
	 * Transform an Exception into user readable form and write it to the log,
	 * including origin of that message.
	 * 
	 * @param source
	 *            origin of this log message
	 * @param message
	 *            the message to be logged
	 * @param e
	 *            Exception to be logged
	 * @param logLevel
	 *            log level on which the message is shown
	 */
	private static void logException(String source, String message, Exception e, LogLevel logLevel) {
		StringBuilder sb;

		sb = new StringBuilder();

		sb.append("encountered the following exception: ");
		sb.append(e.getClass().getCanonicalName());
		sb.append(" at");
		
		StackTraceElement[] stackTrace = e.getStackTrace();
		
		for(StackTraceElement elem : stackTrace) {
			sb.append("\n" + elem.toString());
		}
		
		log(message, logLevel, new LogTag(SOURCE_TAG_ID, source), new LogTag(EXCEPTION_STACK_TAG_ID, sb.toString()));
		
	}
	
	/**
	 * Write exception to the log, including origin of that message.
	 * 
	 * This method uses LOGLEVEL_DLFT as LogLevel.
	 * 
	 * @param className
	 *            originating class of this log message
	 * @param e
	 *            the Exception to be logged
	 */
	public static void logException(Class<?> className, Exception e) {
		logException(className, e, LOGLEVEL_DFLT);
	}
	
	/**
	 * This message provides direct unprocessed write access to the log.
	 * Use this method only if this is exactly what you want and you know what
	 * you are doing. Otherwise try any other log method provided by this class,
	 * e.g. {@link #log(InfoSource, String, byte)}
	 * 
	 * @param message
	 *            the message to be logged
	 * @param logLevel
	 *            log level on which the message is shown
	 */
	private static void logPlain(String message, int logLevel) {		
		LogService logService = Activator.getLogservice();
		if (logService != null){
			logService.log(logLevel, message);
		} else {
			System.out.println(message); //NOSONAR System.out is valid fallback within logger
		}
		
	}
}
