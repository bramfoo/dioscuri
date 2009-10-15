/*
 * $Revision: 163 $ $Date: 2009-08-17 15:12:57 +0000 (ma, 17 aug 2009) $ $Author: blohman $
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */
package dioscuri.module.video;

public class TextTranslation
{

	// Attributes
	
    // ASCII index to Unicode character table
	// Note: the output device using the translated character must support unicode, 
	// else some characters will be displayed incorrectly.
	// Behind some characters "Unicode difference" is written. These ASCII characters have 
	// different indices in unicode compared to ASCII.
    protected String[] asciiToUnicode = new String[] {
    					// Regular ASCII set (0 -127), from 0 to 31 are also commands
    						/* 000 */ "",	// Empty
    						/* 001 */ "?",
    						/* 002 */ "?",
    						/* 003 */ "?",
    						/* 004 */ "?",
    						/* 005 */ "?",
    						/* 006 */ "?",
    						/* 007 */ "?",
    						/* 008 */ "?",
    						/* 009 */ "?",
    						/* 010 */ "",	// Empty
    						/* 011 */ "?",
    						/* 012 */ "?",
    						/* 013 */ "",	// Empty
    						/* 014 */ "?",
    						/* 015 */ "?",
    						/* 016 */ "?",
    						/* 017 */ "?",
    						/* 018 */ "?",
    						/* 019 */ "?",
    						/* 020 */ "¶",
    						/* 021 */ "§",
    						/* 022 */ "",	// ?
    						/* 023 */ "",
    						/* 024 */ "?",
    						/* 025 */ "?",
    						/* 026 */ "?",
    						/* 027 */ "?",
    						/* 028 */ "",	// ?
    						/* 029 */ "?",
    						/* 030 */ "?",
    						/* 031 */ "?",
    						/* 032 */ " ",
    						/* 033 */ "!",
    						/* 034 */ "\"",
    						/* 035 */ "#",
    						/* 036 */ "$",
    						/* 037 */ "%",
    						/* 038 */ "&",
    						/* 039 */ "'",
    						/* 040 */ "(",
    						/* 041 */ ")",
    						/* 042 */ "*",
    						/* 043 */ "+",
    						/* 044 */ ",",
    						/* 045 */ "-",
    						/* 046 */ ".",
    						/* 047 */ "/",
    						/* 048 */ "0",
    						/* 049 */ "1",
    						/* 050 */ "2",
    						/* 051 */ "3",
    						/* 052 */ "4",
    						/* 053 */ "5",
    						/* 054 */ "6",
    						/* 055 */ "7",
    						/* 056 */ "8",
    						/* 057 */ "9",
    						/* 058 */ ":",
    						/* 059 */ ";",
    						/* 060 */ "<",
    						/* 061 */ "=",
    						/* 062 */ ">",
    						/* 063 */ "?",
    						/* 064 */ "@",
    						/* 065 */ "A",
    						/* 066 */ "B",
    						/* 067 */ "C",
    						/* 068 */ "D",
    						/* 069 */ "E",
    						/* 070 */ "F",
    						/* 071 */ "G",
    						/* 072 */ "H",
    						/* 073 */ "I",
    						/* 074 */ "J",
    						/* 075 */ "K",
    						/* 076 */ "L",
    						/* 077 */ "M",
    						/* 078 */ "N",
    						/* 079 */ "O",
    						/* 080 */ "P",
    						/* 081 */ "Q",
    						/* 082 */ "R",
    						/* 083 */ "S",
    						/* 084 */ "T",
    						/* 085 */ "U",
    						/* 086 */ "V",
    						/* 087 */ "W",
    						/* 088 */ "X",
    						/* 089 */ "Y",
    						/* 090 */ "Z",
    						/* 091 */ "[",
    						/* 092 */ "\\",
    						/* 093 */ "]",
    						/* 094 */ "^",
    						/* 095 */ "_",
    						/* 096 */ "`",
    						/* 097 */ "a",
    						/* 098 */ "b",
    						/* 099 */ "c",
    						/* 100 */ "d",
    						/* 101 */ "e",
    						/* 102 */ "f",
    						/* 103 */ "g",
    						/* 104 */ "h",
    						/* 105 */ "i",
    						/* 106 */ "j",
    						/* 107 */ "k",
    						/* 108 */ "l",
    						/* 109 */ "m",
    						/* 110 */ "n",
    						/* 111 */ "o",
    						/* 112 */ "p",
    						/* 113 */ "q",
    						/* 114 */ "r",
    						/* 115 */ "s",
    						/* 116 */ "t",
    						/* 117 */ "u",
    						/* 118 */ "v",
    						/* 119 */ "w",
    						/* 120 */ "x",
    						/* 121 */ "y",
    						/* 122 */ "z",
    						/* 123 */ "{",
    						/* 124 */ "|",
    						/* 125 */ "}",
    						/* 126 */ "~",
    						/* 127 */ "?",
    						
    				    // Extended ASCII set (128 - 255)
    						/* 128 */ "Ç",
    						/* 129 */ "ü",
    						/* 130 */ "é",
    						/* 131 */ "â",
    						/* 132 */ "ä",
    						/* 133 */ "à",
    						/* 134 */ "å",
    						/* 135 */ "ç",
    						/* 136 */ "ê",
    						/* 137 */ "ë",
    						/* 138 */ "è",
    						/* 139 */ "ï",
    						/* 140 */ "î",
    						/* 141 */ "ì",
    						/* 142 */ "Ä",
    						/* 143 */ "Å",
    						/* 144 */ "É",
    						/* 145 */ "æ",
    						/* 146 */ "Æ",
    						/* 147 */ "ô",
    						/* 148 */ "ö",
    						/* 149 */ "ò",
    						/* 150 */ "û",
    						/* 151 */ "ù",
    						/* 152 */ "ÿ",
    						/* 153 */ "Ö",
    						/* 154 */ "Ü",
    						/* 155 */ "ø",
    						/* 156 */ "£",
    						/* 157 */ "Ø",
    						/* 158 */ "×",
    						/* 159 */ "ƒ",
    						/* 160 */ "á",
    						/* 161 */ "í",
    						/* 162 */ "ó",
    						/* 163 */ "ú",
    						/* 164 */ "ñ",
    						/* 165 */ "Ñ",
    						/* 166 */ "",	// ?
    						/* 167 */ "",	// ?
    						/* 168 */ "¿",
    						/* 169 */ "®",
    						/* 170 */ "¬",
    						/* 171 */ "½",
    						/* 172 */ "¼",
    						/* 173 */ "¡",
    						/* 174 */ "«",
    						/* 175 */ "»",
    						/* 176 */ "?",	// Unicode difference
    						/* 177 */ "?",	// Unicode difference
    						/* 178 */ "?",	// Unicode difference
    						/* 179 */ "?",	// Unicode difference
    						/* 180 */ "?",	// Unicode difference
    						/* 181 */ "�?",
    						/* 182 */ "Â",
    						/* 183 */ "À",
    						/* 184 */ "©",
    						/* 185 */ "?",	// Unicode difference
    						/* 186 */ "?",	// Unicode difference
    						/* 187 */ "?",	// Unicode difference
    						/* 188 */ "?",	// Unicode difference
    						/* 189 */ "",	// ?
    						/* 190 */ "¥",
    						/* 191 */ "?",	// Unicode difference
    						/* 192 */ "?",	// Unicode difference
    						/* 193 */ "?",	// Unicode difference
    						/* 194 */ "?",	// Unicode difference
    						/* 195 */ "?",	// Unicode difference
    						/* 196 */ "?",	// Unicode difference
    						/* 197 */ "?",	// Unicode difference
    						/* 198 */ "ã",
    						/* 199 */ "Ã",
    						/* 200 */ "?",	// Unicode difference
    						/* 201 */ "?",	// Unicode difference
    						/* 202 */ "?",	// Unicode difference
    						/* 203 */ "?",	// Unicode difference
    						/* 204 */ "?",	// Unicode difference
    						/* 205 */ "?",	// Unicode difference
    						/* 206 */ "?",	// Unicode difference
    						/* 207 */ "¤",
    						/* 208 */ "ð",
    						/* 209 */ "�?",
    						/* 210 */ "Ê",
    						/* 211 */ "Ë",
    						/* 212 */ "È",
    						/* 213 */ "¹",	// ?
    						/* 214 */ "�?",
    						/* 215 */ "Î",
    						/* 216 */ "�?",
    						/* 217 */ "?",	// Unicode difference
    						/* 218 */ "?",	// Unicode difference
    						/* 219 */ "?",	// Unicode difference
    						/* 220 */ "?",	// Unicode difference
    						/* 221 */ "¦",
    						/* 222 */ "Ì",
    						/* 223 */ "?",	// Unicode difference
    						/* 224 */ "Ó",
    						/* 225 */ "ß",
    						/* 226 */ "Ô",
    						/* 227 */ "Ò",
    						/* 228 */ "õ",
    						/* 229 */ "Õ",
    						/* 230 */ "µ",
    						/* 231 */ "þ",
    						/* 232 */ "Þ",
    						/* 233 */ "Ú",
    						/* 234 */ "Û",
    						/* 235 */ "Ù",
    						/* 236 */ "ý",
    						/* 237 */ "�?",
    						/* 238 */ "¯",
    						/* 239 */ "´",
    						/* 240 */ "–",
    						/* 241 */ "±",
    						/* 242 */ "",	// ?
    						/* 243 */ "¾",
    						/* 244 */ "¶",
    						/* 245 */ "§",
    						/* 246 */ "÷",
    						/* 247 */ "¸",
    						/* 248 */ "º",
    						/* 249 */ "¨",
    						/* 250 */ "•",
    						/* 251 */ "¹",
    						/* 252 */ "³",
    						/* 253 */ "²",
    						/* 254 */ "?",	// Unicode difference
    						/* 255 */ ""	// ?
    					};
	
	
}
