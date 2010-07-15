; AND_ALIb (opcode 24), logical AND of immediate byte and AL
; 48 24 55 24 aa 48 24 00 f4
start:
	DEC ax		; Set AL to FF
	AND al, 0x55	; 1111 1111 | 0101 0101
	AND al, 0xAA	; 1010 1010 | 1010 1010
	DEC ax		; Reset al to FF
	AND al, 0x00	; 1111 1111 | 0000 0000
	HLT