/* $Revision: 361 $ $Date: 2010-10-01 16:11:11 +0200 (Fr, 01 Okt 2010) $ $Author: bkiers $
 *
 * Copyright (C) 2007-2009  National Library of the Netherlands,
 *                          Nationaal Archief of the Netherlands,
 *                          Planets
 *                          KEEP
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 *
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 *
 * Project Title: DIOSCURI
 */

package dioscuri.vnc;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * Class holding static method helper
 * @author Gendo
 */
public class VNCUtil {

    /**
     * Find the IP address of localhost.
     *
     * @return the IP address of localhost.
     */
    public static String getHostIP() {
        try {
            byte[] ipAddr = InetAddress.getLocalHost().getAddress();
            StringBuilder b = new StringBuilder();
            if(ipAddr != null && ipAddr.length > 0) {
                b.append(ipAddr[0]);
                for(int i = 1; i < ipAddr.length; i++) {
                    b.append('.').append(ipAddr[i]);
                }
                return b.toString();
            }
        } catch (UnknownHostException e) {
            // swallow it
        }
        return null;
    }

    /**
     * Find the hostname of localhost.
     *
     * @return the hostname of localhost.
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // swallow it
        }
        return null;
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     * Code from http://mina.apache.org/
     */
    public static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            /* falls through */
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }
        return false;
    }
}
