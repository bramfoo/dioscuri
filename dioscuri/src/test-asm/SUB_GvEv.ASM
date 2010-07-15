; SUB_GvEv (opcode 2B), Subtract word in register (destination) from memory/register (source)
; b8 00 80 a3 00 00 b8 03 10 a3 02 00 b8 fd 6f a3 04 00 b8 00 00 2b 00
; 2b 40 02 2b 41 04 bd 07 00 2b 86 00 01 b9 00 7f ba 00 ff 29 d1 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x8000
	MOV [0x0000], AX
	MOV AX, 0x1003
	MOV [0x0002], AX
	MOV AX, 0x6FFD
	MOV [0x0004], AX
	MOV AX, 0x0000

	; SUB mem,reg
	SUB AX, [BX+SI] 	; 0000 - 8000, test OF, SF, CF flags

	; SUB mem+8b,reg
	SUB AX, [BX+SI+02]	; 8000 - 1003, test OF, SF, AF, PF flags
	SUB AX, [BX+DI+04]	; 6ffd - 6ffd, test ZF flag

	; SUB mem+16b,reg
	MOV BP, 0x0007		; Set BP
	SUB AX, [BP+0x0100]	; 0000 - 1003, test AF, CF, overflow

	; SUB reg, reg
	MOV CX, 0x7F00
	MOV DX, 0xFF00
	SUB CX, DX	; 7F00 - FF00, test OF, CF
	HLT