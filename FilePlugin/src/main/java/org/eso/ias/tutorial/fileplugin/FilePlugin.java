package org.eso.ias.tutorial.fileplugin;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;

import org.eso.ias.plugin.Plugin;
import org.eso.ias.plugin.PluginException;
import org.eso.ias.plugin.Sample;
import org.eso.ias.plugin.config.PluginFileConfig;
import org.eso.ias.plugin.config.PluginConfigFileReader;

public class FilePlugin extends Plugin {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  
  private final String filePath;

  // The identifier of the value (temperature) to send to the IAS
  private final String valueId="Temperature-id";

  /**
	 * The logger
	 */
	private static final  org.slf4j.Logger logger = LoggerFactory.getLogger(FilePlugin.class);

  /**
   * Constructor
   */
  private FilePlugin(PluginFileConfig config, String tempFilePath) {
    super(config);
    this.filePath=tempFilePath;
  }

  public void startReading(long intervalSeconds) {
      Runnable task = () -> {
          try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
              String firstLine = reader.readLine();
              if (firstLine!=null) {
                logger.info("Temperature from file: {}",firstLine);
                Float temperature=null;
                try {
                  temperature = Float.valueOf(firstLine.trim());
                } catch (NumberFormatException nfe) {
                  logger.error("Cannot convert {} to float", firstLine);
                  return;
                }
                Sample s = new Sample(temperature, System.currentTimeMillis());
                try {
                  updateMonitorPointValue(valueId, temperature);
                  logger.info("Sample with ID {} and value {} sent to the Plugin", valueId, temperature);
                } catch (PluginException e) {
                  logger.error("Error sending value {} with id {} to plugin", temperature, valueId, e);
                  e.printStackTrace();
                }
              }              
          } catch (IOException e) {
              logger.error("Error {} reading temperature from file {}" + e.getMessage(), filePath);
          }
      };

      scheduler.scheduleAtFixedRate(task, 0, intervalSeconds, TimeUnit.SECONDS);
  }

  public void stopReading() {
      scheduler.shutdown();
      try {
          if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
              scheduler.shutdownNow();
          }
      } catch (InterruptedException e) {
          scheduler.shutdownNow();
      }
  }

  @Override
  public void close() {
    stopReading();
    super.close();
  }


  /**
   * runs the plugin.
   */
  public static void main(String[] args) throws Exception {
    if (args.length!=2) {
      System.err.println("Wrong number of params given: config file missing!");
      System.out.println("USAGE: FilePlugion <confgFileName.json> tempFileName");
      System.exit(2);
    }
    System.out.println("Starting FilePlugin with config file "+args[0]);

    // Set the log level
    Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    rootLogger.setLevel(Level.INFO);
    

    PluginFileConfig config = null;
    try {
      File file = new File(args[0]);
      PluginConfigFileReader jsonFileReader = new PluginConfigFileReader(file);
      config = jsonFileReader.getPluginConfig().get(1,TimeUnit.MINUTES);
    } catch (Throwable t) {
      System.out.println(t.getMessage());
      t.printStackTrace(System.err);
      System.exit(1);
    }

    FilePlugin plugin = new FilePlugin(config, args[1]);
    plugin.start();
    plugin.startReading(3);


  }
}