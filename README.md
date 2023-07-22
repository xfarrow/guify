# Guify
Guify creates a Graphical User Interface for SSH. 

Works on any machine able to execute Java code, but is able to target only Linux (or more in general POSIX-compliant) systems.

## Features:
- Navigation in the File System;
- Common file operations (cut, copy, rename, delete, paste, create);
- File and folder transfer (to/from);
- Integrated file editor.

## Alternatives
If you want to achieve similar results you can:
* Use [WinSCP](https://winscp.net/eng/index.php) (Windows only);
* Use a file manager like Nautilus, Konqueror, PCManFM (Linux examples)
* Configure an FTP server and accessing it through FileZilla;
* Mount a remote partition locally with the SMB protocol;
* Route X11 output to your local machine over SSH;
* ...

## Screenshots
<img src="/Images/Image.jpg" alt="Homescreen">

## Security
* Due to a lack of extensive tests and documentation, this software may engage in unwanted behavior, including corrupting data.
* This software does not currently allow to verify SSH fingerprints. This implies that the server you intend to connect to could be different
from the server you are actually connecting to.
* This software does not currently allow authentication through SSH keys.

Hence, **use is not recommended in environments where data security and integrity is important.**
