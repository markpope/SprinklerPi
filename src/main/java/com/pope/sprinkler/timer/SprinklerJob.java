/*
 * Copyright © 2016 Mark Pope
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons 
 * to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.pope.sprinkler.timer;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pope.sprinkler.googleapi.GoogleSpreadSheetWriter;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When the TimerTask fires it turns on the GPIO pin associated to the sprinkler
 * by number. It sleeps for the duration of the Calendar event, turning off the 
 * GPIO pin.
 */
public class SprinklerJob extends TimerTask {

  private final Event event;
  private static final GpioController gpio = GpioFactory.getInstance();

  //Avoiding if-else or switch
  private final static GpioPinDigitalOutput[] pins = {null,
    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Sprinker 1", PinState.LOW), //12
    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Sprinker 2", PinState.LOW), //13
    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "Sprinker 3", PinState.LOW), //15
    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Sprinker 4", PinState.LOW), //16
    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "Sprinker 5", PinState.LOW), //18
    gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "Sprinker 6", PinState.LOW)}; //22

  public SprinklerJob(Event event) {
    this.event = event;
  }

  @Override
  public void run() {
	  int sprinkler = 0;
    try {
	    System.out.println("Sprinkler " + event.getSummary());
	    System.out.println("Description " + event.getDescription());
      sprinkler = Integer.parseInt(event.getDescription());
      GoogleSpreadSheetWriter.getInstance().log(event.getSummary() + "[" + event.getDescription() + "]", "Starting");

      //Turn off any running sprinklers remember [0] is null
      for (int i = 1; i < pins.length; i++) {
        pins[i].low();
      }

      //Start sprinkler
      pins[sprinkler].high();

      DateTime start = event.getStart().getDateTime();
      DateTime end = event.getEnd().getDateTime();

      //Water time
      Thread.sleep(end.getValue() - start.getValue());

      //Stop sprinkler
      pins[sprinkler].low();

      GoogleSpreadSheetWriter.getInstance().log(event.getSummary() + "[" + event.getDescription() + "]", "Stopping");
    } catch (NumberFormatException | InterruptedException e) {
      Logger.getLogger(SprinklerJob.class.getName()).log(Level.SEVERE, null, e);
    }
    finally {
	    pins[sprinkler].low();
    }
  }

  /**
   * Force stop
   */
  public void stop() {
    try {
      int sprinkler = Integer.parseInt(event.getDescription());
      pins[sprinkler].low();
      GoogleSpreadSheetWriter.getInstance().log(event.getSummary() + "[" + event.getDescription() + "]", "Stopping");
    } catch (Exception e) {
      Logger.getLogger(SprinklerJob.class.getName()).log(Level.SEVERE, null, e);
    }
  }

}
