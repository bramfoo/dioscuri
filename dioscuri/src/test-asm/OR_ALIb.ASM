; OR_ALIb (opcode 0C), logical OR of immediate byte and AL
; 0c 00 0c aa 0c 55 40 0c ff f4
start:
	OR al, 0x00    ; 0000 0000 | 0000 0000
	OR al, 0xAA    ; 0000 0000 | 1010 1010
	OR al, 0x55    ; 1010 1010 | 0101 0101
	INC ax	       ; Reset ax to 0
	OR al, 0xFF    ; 0000 0000 | 1111 1111
	HLT