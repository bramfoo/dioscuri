Dioscuri user instructions - Modular emulator for Digital Preservation
======================================================================
Date             : 23 September 2010
Organisations    : Koninklijke Bibliotheek,
                   Nationaal Archief of the Netherlands
                   Tessella Support Services plc.
Projects         : Dioscuri project, Planets, KEEP

Command line arguments
----------------------------------------------------------------------
 
usage: java -jar Dioscuri.jar [OPTIONS]

 -?,--help                           print this message
 -a,--architecture <'16'|'32'>       sets the cpu's architecture
 -b,--boot <'floppy'|'harddisk'>     sets the boot drive
 -c,--config <file>                  loads a custom config xml file
 -d1,--harddisk1 <file>              loads a custom first hard disk image
 -d2,--harddisk2 <file>              loads a custom second hard disk image
 -e,--exit                           used for testing purposes, will cause
                                     Dioscuri to exit immediately
 -f,--floppy <file>                  loads a custom floppy image
 -h,--hide                           hides the GUI
 -m,--mouse <'enabled'|'disabled'>   enables or disables the mouse
 -r,--autorun                        emulator will directly start
                                     emulation process
 -s,--autoshutdown                   emulator will shutdown automatically
                                     when emulation process is finished