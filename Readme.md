# Yet Another Sprinkler Pi

SprinklerPi is a Raspberry Pi Irrigation Control System that offers a web/mobile interface and uses Google Calendar and Spreadsheets to manage sprinkler run-times and logging, so the system can be controlled and monitored from anywhere.

## Environment

This project tests the processing capacity of rPi. It is written in Java and runs on PayaraMicro, the JEE microprofile compliant JEE server. It uses JAX-RS enpoints, EJBs, and Service Timers. Reboots require ~5 minutes to become fully operational. With a mounted fan the rPi cannot disapate heat in the garage duing the summer a shuts down. Another rPi running Python monitoring beer fermentation temerature has no issues with the summer heat in the garage.

Google APIs for Calendar and Spreadsheets were selected to avoid developing an complex UI and exposing an external port for remote access. You must generate your own API keys and create the 'Sprinklers' calendar in Google.

The Google Calendar, Sprinklers, is read every 15 minutes for updates. Internal sprinkler schedules are updated. Failure to contact Google and get new changes will let the last schedules to run.

This project supports 6 valves. Create a separate calendar entry for each sprinkler. The duration of the event determines the sprinkler runtime. Add a single digit(1-6) to the calendar event message body that determines which sprinkler is targeted. Check the Sprinkler spreadsheet for logs.

The logs below shows that the rpi read the Google Sprinklers calendar and found an event for Garden with '4' in the event message body, starting at 7:00am.

|TimeStamp | Sprinkler |Description|
|----------|-----------|-----------|
|Sat May 07 20:10:25 PDT 2021| |Resetting scheduled timers|
|Sat May 07 20:10:27 PDT 2021||Reading Sprinkler Calendar|
|Sat May 07 20:10:28 PDT 2021|Garden[4]|Scheduled to start at 2021-05-07T21:00:00.000-07:00|
|Sat May 07 20:15:00 PDT 2021| |Reading Sprinkler Calendar|
|Sat May 07 20:15:02 PDT 2021|Garden[4]|Scheduled to start at 2021-05-07T21:00:00.000-07:00|
|Sat May 07 20:15:08 PDT 2021| |Reading Sprinkler Calendar|
|Sat May 07 20:15:09 PDT 2021|Garden[4]|Scheduled to start at 2021-05-07T21:00:00.000-07:00|
|Sat May 07 20:57:52 PDT 2021| |Resetting scheduled timers|
|Sat May 07 20:57:53 PDT 2021| |Reading Sprinkler Calendar|