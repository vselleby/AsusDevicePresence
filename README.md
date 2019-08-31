# AsusDevicePresence

## What is AsusDevicePresence

This project has been created as a means to monitor devices connected to WLAN without having to use the Asus web interface. It works by connecting to the router via SSH through [Jsch](http://www.jcraft.com/jsch/) and reading the /tmp/clientlist.json file. I have found that it works better for triggering on devices connecting to the WLAN rather than disconnecting. When I have tested this empirically, it seems new connectections are usually discovered within 10 seconds while devices disconnecting can take much longer to notice. I have also noticed that it works better for discovering devices that have been connected previously.

## How to make it work

For this to work SSH has to be enabled and a public RSA key has to be added to Authorized Keys. This can be done from the settings found under Administration->System. See [UsageExample](https://github.com/vselleby/AsusDevicePresence/blob/master/src/main/java/UsageExample.java)
