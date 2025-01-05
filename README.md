# VBHelper

## Developer Setup

1. Clone vb-nfc-reader (https://github.com/cfogrady/lib-vb-nfc)
2. Run vb-nfc-reader/publishToMavenLocal gradle task in the lib-vb-nfc project.
3. Clone vb-dim-reader (https://github.com/cfogrady/vb-dim-reader)
4. Run publishToMavenLocal gradle task in the vb-dim-reader project.
5. Create res/values/keys.xml within the app module.
6. Populate with:
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="password1">beHmacKey1</string>
    <string name="password2">beHmacKey2</string>
    <string name="decryptionKey">aesKey</string>
    <integer-array name="substitutionArray">
        <item>0</item>
        <item>1</item>
        <item>2</item>
        <item>3</item>
        <item>4</item>
        <item>5</item>
        <item>6</item>
        <item>7</item>
        <item>8</item>
        <item>9</item>
        <item>10</item>
        <item>11</item>
        <item>12</item>
        <item>13</item>
        <item>14</item>
        <item>15</item>
    </integer-array>
</resources>
```
7. Replace the values in the keys.xml file with those extracted from the original app.
8. Run