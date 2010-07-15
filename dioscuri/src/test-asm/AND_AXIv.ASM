; AND_AXIv (opcode 25), logical AND of immediate word and AX
; 48 25 55 55 25 aa aa 48 25 01 01 f4
start:
	DEC ax
	AND ax, 0x5555	 ; 1111 1111 1111 1111 | 0101 0101 0101 0101
	AND ax, 0xAAAA	 ; 0101 0101 0101 0101 | 1010 1010 1010 1010
	DEC ax		 ; Reset ax to 0
	AND ax, 0x0101	 ; 1111 1111 1111 1111 | 0000 0001 0001 0001
	HLT