; CMP_EbGb (opcode 38), byte-sized comparison of memory/register with register
; b8 00 85 a3 00 00 b8 06 80 b9 7f ff ba 86 85 38 20 38 41 01
; 45 45 38 B6 00 01 38 d6 38 e9 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x8500
	MOV [0000], AX
	MOV AX, 0x8006
	MOV CX, 0xFF7F
	MOV DX, 0x8586

	; CMP mem,reg
	CMP [BX+SI], AH ; 00 - 80, test OF, SF, CF

	; CMP mem+8b,reg
	CMP [BX+DI+01], AL	; 85 - 06, test OF, SF, AF, PF flags

	; CMP mem+16b,reg
	INC BP			; Set BP to 1
	INC BP			; Set BP to 2
	CMP [BP+0x0100], DH	; 85 - 85, test ZF

	; CMP reg, reg
	CMP DH, DL	; 85 - 86, test AF, CF, overflow
	CMP CL, CH	; 7F - FF, test OF, CF
	HLT
