package org.eso.ias.tutorial.fileplugin;

import java.io.IOException;

import org.eso.ias.plugin.Plugin;
import org.eso.ias.plugin.config.PluginFileConfig;

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
  public static void main(String[] args) throws IOException {
    System.err.println("Starting FilePlugin...");
  }
}