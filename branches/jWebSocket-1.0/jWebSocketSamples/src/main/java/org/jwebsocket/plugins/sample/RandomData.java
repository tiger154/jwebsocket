// TODO: can this class be ultimately removed?
// It's referred to only by SamplePlugIn and does not seem to do anything reasonable
// Please check for v0.11 rollout

package org.jwebsocket.plugins.sample;

public class RandomData {
  private String randomText = "this is random text to display by plugin";
  
  public String getRandomText() {
    return randomText;
  }
}
