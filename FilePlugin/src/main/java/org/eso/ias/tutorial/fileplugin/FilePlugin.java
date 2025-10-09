package org.eso.ias.tutorial.fileplugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eso.ias.plugin.Plugin;
import org.eso.ias.plugin.config.PluginConfigException;
import org.eso.ias.plugin.config.PluginFileConfig;
import org.eso.ias.plugin.config.PluginConfigFileReader;

public class FilePlugin extends Plugin {

  /**
   * Constructor
   */
  private FilePlugin(PluginFileConfig config) {
    super(config);
  }

  /**
   * runs the plugin.
   */
  public static void main(String[] args) throws Exception {
    if (args.length!=1) {
      System.err.println("Wrong number of params given: config file missing!");  
      System.exit(2);
    }
    System.out.println("Starting FilePlugin with config file "+args[0]);

    try {
      File file = new File(args[0]);
      PluginConfigFileReader jsonFileReader = new PluginConfigFileReader(file);
      PluginFileConfig config = jsonFileReader.getPluginConfig().get(1,TimeUnit.MINUTES);
    } catch (Throwable t) {
      System.out.println(t.getMessage());
      t.printStackTrace(System.err);
      System.exit(1);
    }
  }
}