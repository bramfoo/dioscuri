; CMP_GvEv (opcode 3B), word-sized comparison of register with memory/register
; b4 80 a3 00 00 b8 02 01 a3 02 00 b9 01 81 ba 02 81 b8 00 00 3b 00
; 3b 49 02 bd 0c 00 3b 8e 00 01 3b 96 00 01 b9 01 7f ba 01 ff 39 d1 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AH, 0x80
	MOV [0000], AX
	MOV AX, 0x0102
	MOV [0002], AX
	MOV CX, 0x8101
	MOV DX, 0x8102
	MOV AX, 0x0000

	; CMP mem,reg
	CMP AX, [BX+SI] ; 0000 - 8000, test OF, SF, CF

	; CMP mem+8b,reg
	CMP CX, [BX+DI+02]	; 8101 - 0102, test OF, SF, AF, PF flags

	; CMP mem+16b,reg
	MOV BP, 0x000C		; Set BP to C
	CMP CX, [BP+0x0100]	; 8101 - 8101, test ZF
	CMP DX, [BP+0x0100]	; 8102 - 8101, test AF, CF, overflow

	; CMP reg, reg
	MOV CX, 0x7F01
	MOV DX, 0xFF01
	CMP CX, DX	; 7F01 - FF01, test OF, CF
	HLT