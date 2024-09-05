# Guify
A Graphical User Interface for SSH. 

Works on any machine able to execute Java code, but is able to target only Linux (or more in general POSIX-compliant) systems.

## Features
- Navigation in the File System;
- Common file operations (cut, copy, rename, delete, paste, create);
- File and folder transfer (to/from);
- Drop files and folders onto the view to transfer them;
- Integrated file editor.

## Alternatives
If you want to achieve similar results you can:
* Use [WinSCP](https://winscp.net/eng/index.php) (Windows only);
* Use a file manager which supports SSH like Nautilus, Konqueror, PCManFM;
* Configure an FTP server and access it through FileZilla;
* Mount a remote partition locally with the SMB protocol;
* Route X11 output to your local machine over SSH;
* ...

## Screenshots
<img src="/Images/Image.jpg" alt="Homescreen">

## Security notice
* This program does not currently allow to verify SSH fingerprints. This implies that the server you intend to connect to could be different
from the server you are actually connecting to;
* This program does not currently allow authentication through SSH keys.

## Support this project
If you would like to support this project you can give it a star. Furthermore, if you want, you can donate by clicking on this button:

<a href='https://ko-fi.com/M4M1JBAO8' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://storage.ko-fi.com/cdn/kofi1.png?v=3' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>
