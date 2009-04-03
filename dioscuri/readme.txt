Dioscuri user instructions - Modular emulator for Digital Preservation
============================================================
Date             : 2 April 2009
Date last update : 2 April 2009
Organisations    : Koninklijke Bibliotheek,
                   Nationaal Archief of the Netherlands

Command line arguments
------------------------------------------------------------

The following arguments are allowed (in any particular order):
 -c "<CONFIGPATH_FILE>" : uses given config.xml file instead of default. If not available, default will be used
 -h                     : hide GUI
 autorun                : emulator will directly start emulation process
 autoshutdown           : emulator will shutdown automatically when emulation process is finished

Example: java -jar Dioscuri.jar -c "c:\emulators\configs\dioscuri_config.xml" autorun
