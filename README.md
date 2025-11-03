# VBHelper

Application to interact with the Vital series, VB, VH, VBC and VBBE.

## Current state of the project

Right now the project is still under development, and until further notice, any database updates will result in having to erase application data.

This document will be updated once the application does not need any more database resets.

## Features

As of now, the project allows you to read characters, view characters stats, and send them back to your watch. 

You can also apply items to the characters read, such as special missions, or change timers, and store characters in the storage section.

You also earn new items every time an item such as a special mission (VB only) or a character completes an in-app adventure mission.

App also comes with a dex that will update every time a new character is added, and allows you to see evolution requirements and current adventure stage in the watch.

## How to set up

1. Download the latest version for VB Arena APK from a trustworthy source. If your download is a standalone APK, continue to step 2. Otherwise, if your download is an XAPK, do the following:

    1. Using your phone file manager, rename the XAPK file to ZIP, and extract its contents. You can also do this with any other device, such as Windows, macOS and Linux. 

    2. Once the files are extracted, look for an APK called `com.bandai.vitalbraceletarena.apk`. Copy it somewhere else, you will need it.

2. Install an APK release for VB Arena. You will find the releases [here](http://github.com/nacabaro/vbhelper/releases). Download the latest release and install its APK.

    Note, in the current stage of the project, you will have to delete the old application from your device. If the app keeps crashing after installing, clear application data and storage.

3. Import secrets in the app. These secrets will allow the app to talk to the watch. On the main screen, click on the gear icon, then `Import secrets`.
    
    You will be prompted to choose a file. Choose the APK file that was previously obtained.

4. Import cards. Due to copyright laws, we cannot offer the characters and sprites themselves in the application. In order to import the cards do the following.

    1. Using your own DiM/BEm cards, dump the cards to your device. You can get an in-depth tutorial in [here](http://mrblinky.net/digimon/vb/dimcardtool/dimcardtool.html). You can download the dump tool from [here](http://mrblinky.net/digimon/vb/dimcardtool/)

    2. Once installed the tool and drivers, open the tool, connect your DiM/BEm reader hardware to yout computer and click on Read card.

    3. Transfer the resulting file to your mobile device. You can put them anywhere, as long as they are accessible. My recommendation is to put them under a folder called `Cards` in your `Internal storage` or `SD Card`

    4. In the app, click on import card. Next choose the BIN file corresponding to the card you want to import.

    **Note: if you do not import the card, whenever you attempt to read a character from th watch, the character you read will get deleted.** 

5. App will now be ready to be used.

## Planned features

- Online battles, undegoing development by `lightheel`.

- VitalWear compatibility, undergoing development by `cfogrady`.

- Support for multiple languages, not yet started.

- Database backup/restore.

## Credits

- `cyanic` for helping us understand more about the VB connection protocol.

- `cfogrady` for making both [`VB-DIM-Reader`](https://github.com/cfogrady/VB-DIM-Reader) and [`lib-vb-nfc`](https://github.com/cfogrady/lib-vb-nfc)

- `lightheel` for working on the online component in the application, both server and battle client.

- `shvstrz` for the app icon.