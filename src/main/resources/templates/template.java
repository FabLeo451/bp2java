package org.jlogic.program;

import java.util.*;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.DailyRollingFileAppender;
import java.lang.Exception;

import com.lionsoft.jlogic.standard.*;

// Import section
{import}

public class {className} {

  static Logger logger = Logger.getLogger({className}.class.getName());
  static BPContext context = new BPContext();

  // Global section
  {globals}

  // Include section
  {include}

  public {className}(BPContext c) {
    _init();
    context = c;
    _initLog();
  }

  public static void main(String[] argv) {
    int resultCode = 0;

    _init();

    int c;
    LongOpt[] longopts = new LongOpt[6];

    longopts[0] = new LongOpt("warn", LongOpt.NO_ARGUMENT, null, 0);
    longopts[1] = new LongOpt("trace", LongOpt.NO_ARGUMENT, null, 1);
    longopts[2] = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 2);
    longopts[3] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
    longopts[4] = new LongOpt("properties", LongOpt.REQUIRED_ARGUMENT, null, 'p');
    longopts[5] = new LongOpt("logpath", LongOpt.REQUIRED_ARGUMENT, null, 'L');

    Getopt g = new Getopt("{className}", argv, "p:L:h", longopts);
    g.setOpterr(false);

    while ((c = g.getopt()) != -1) {
      switch (c) {
        case 0:
          System.out.println("Warn enabled");
          logger.setLevel(Level.WARN);
          break;

        case 1:
          System.out.println("Trace enabled");
          logger.setLevel(Level.TRACE);
          break;

        case 2:
          System.out.println("DEBUG enabled");
          logger.setLevel(Level.DEBUG);
          break;

        case 'p':
          context.loadProgramProperties(g.getOptarg());
          break;

        case 'L':
          context.setLogPath(g.getOptarg());
          _initLog();
          break;

        case 'h':
          _help();
          System.exit(0);

        case '?':
          System.out.println("The option '" + (char)g.getOptopt() + "' is not valid");
          break;

        default:
          System.out.println("getopt() returned " + c);
          break;
       }
     }

     //System.out.println("argv.length = " + argv.length + "\n");
     //System.out.println("g.getOptind() = " + g.getOptind() + "\n");

    int k=0, n = argv.length - g.getOptind();
    String[] _argv = null;

    if (n > 0) {
     _argv = new String[n];

     for (int i = g.getOptind(); i < argv.length ; i++)
       _argv[k++] = argv[i];
    }

    try {
      _event(EventType.BEGIN, null);
      _main(_argv);
    } catch (ExitException e) {

    } catch (Exception e) {
      logger.error(e);

      try {
        _event(EventType.EXCEPTION, e.getMessage());
      } catch (ExitException ex) { }
    }
    finally {
      try {
        _event(EventType.END, null);
      } catch (ExitException ex) { }
    }
    //System.exit(resultCode);
  }

  public static BPContext _getContext() {
    return (context);
  }

  /**
   * _help()
   */
  public static void _help() {
    System.out.println(System.lineSeparator() +
                       "{programName}" + System.lineSeparator() +
                       System.lineSeparator() +
                       "Options:" + System.lineSeparator() +
                       "  --warn           Set log level to WARN." + System.lineSeparator() +
                       "  --trace          Set log level to TRACE." + System.lineSeparator() +
                       "  --debug          Set log level to DEBUG." + System.lineSeparator() +
                       "  -p, --properties Set properties file." + System.lineSeparator() +
                       "  -L, --logpath    Set log path." + System.lineSeparator() +
                       System.lineSeparator());
  }

  /**
   * _init()
   */
  public static void _init() {
    BasicConfigurator.configure();
    logger.setLevel(Level.INFO);

    if (context == null)
      context = new BPContext();
  }

  /**
   * _initLog()
   */
  public static void _initLog() {
    // https://examples.javacodegeeks.com/enterprise-java/log4j/log4j-rolling-daily-file-example/
    //DailyRollingFileAppender appender = new DailyRollingFileAppender(layout, context.getLogPath()+"test.log", "'.'yyyy-MM-dd");

    PatternLayout patternLayoutObj = new PatternLayout();
    //String conversionPattern = "[%p] %d %c %M - %m%n";
    String conversionPattern = "%d [%p] [%t] %m%n";
    patternLayoutObj.setConversionPattern(conversionPattern);

    // Create Daily Rolling Log File Appender
    DailyRollingFileAppender rollingAppenderObj = new DailyRollingFileAppender();
    rollingAppenderObj.setFile(context.getLogPath()+"/"+context.getLogName()+".log");
    rollingAppenderObj.setDatePattern("'.'yyyy-MM-dd");
    rollingAppenderObj.setLayout(patternLayoutObj);
    rollingAppenderObj.activateOptions();

    //logger.info("Log: "+context.getLogPath()+"/{programName}.log");

    // Configure the Root Logger
    Logger rootLoggerObj = Logger.getRootLogger();
    //System.out.println("rootLoggerObj: "+rootLoggerObj.getName());
    //rootLoggerObj.setLevel(Level.INFO);

    Enumeration<Logger> allLoggers = LogManager.getCurrentLoggers();
    while (allLoggers.hasMoreElements()) {
      Logger l = allLoggers.nextElement();
      //System.out.println("Logger: "+l.getName());

      Enumeration<Appender> allAppenders = l.getAllAppenders();
      while (allAppenders.hasMoreElements()) {
        Appender a = allAppenders.nextElement();
        //System.out.println("Appender: "+a.getName());
        logger.removeAppender(a);
      }
    }

    logger.addAppender(rollingAppenderObj);
  }

  public static void _log(String s) {
    logger.info(s);
  }

  public static void _error(String s) {
    logger.error(s);
  }

  public static int _getCode() {
    return(context.getCode());
  }

  public static void _exit(int code, String message) throws ExitException {
    if (context != null)
      context.setResult(code, message);

    System.err.println(message+" (code: "+code+")");

    throw new ExitException(message);
  }

  // User code

  {user-functions}
};
