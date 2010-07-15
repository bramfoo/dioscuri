; CMP_EvGv (opcode 39), word-sized comparison of memory/register with register
; a3 00 00 b8 01 81 a3 02 00 b8 00 80 b9 02 01 ba 02 81 39 00 39 49 02
; b8 01 81 bd 04 00 39 86 00 01 39 96 00 01 b9 01 7f ba 01 ff 39 d1 f4
start:
	; Load memory, registers with pre-arranged values
	MOV [0000], AX
	MOV AX, 0x8101
	MOV [0002], AX
	MOV AX, 0x8000
	MOV CX, 0x0102
	MOV DX, 0x8102

	; CMP mem,reg
	CMP [BX+SI], AX ; 0000 - 8000, test OF, SF, CF

	; CMP mem+8b,reg
	CMP [BX+DI+02], CX	; 8101 - 0102, test OF, SF, AF, PF flags

	; CMP mem+16b,reg
	MOV AX, 0x8101
	MOV BP, 0x0004		; Set BP to 4
	CMP [BP+0x0100], AX	; 8101 - 8101, test ZF
	CMP [BP+0x0100], DX	; Test AF, CF, overflow

	; CMP reg, reg
	MOV CX, 0x7F01
	MOV DX, 0xFF01
	CMP CX, DX	; 7F01 - FF01, test OF, CF
	HLT