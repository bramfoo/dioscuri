; XOR_ALIb (opcode 34), logical XOR of immediate byte and AL
; 34 ff 34 aa 34 55 f4
start:
	XOR al, 0xFF	; 0000 0000 | 1111 1111
	XOR al, 0xAA	; 1111 1111 | 1010 1010
	XOR al, 0x55	; 0101 0101 | 0101 0101
	HLT