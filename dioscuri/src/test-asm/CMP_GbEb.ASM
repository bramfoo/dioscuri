; CMP_GbEb (opcode 3A), byte-sized comparison of register with memory/register
; b8 80 06 a3 00 00 b8 85 00 b9 7f ff ba 86 85 3a 20 3a 41 01
; bd 07 00 3a b6 00 01 38 d6 38 e9 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x0680
	MOV [0000], AX
	MOV AX, 0x0085
	MOV CX, 0xFF7F
	MOV DX, 0x8586

	; CMP mem,reg
	CMP AH, [BX+SI] ; 00 - 80, test OF, SF, CF

	; CMP mem+8b,reg
	CMP AL, [BX+DI+01]	; 85 - 06, test OF, SF, AF, PF flags

	; CMP mem+16b,reg
	MOV BP, 0x0007		; Set BP to 7
	CMP DH, [BP+0x0100]	; 85 - 85, test ZF

	; CMP reg, reg
	CMP DH, DL	; 85 - 86, test AF, CF, overflow
	CMP CL, CH	; 7F - FF, test OF, CF
	HLT
