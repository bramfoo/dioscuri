; SBB_GvEv (opcode 1B), subtract word in memory/register (source) + CF from register (destination)
; b8 00 80 a3 00 00 b8 03 10 a3 04 00 b8 00 00 a3 02 00 b9 03 10 1b 00
; 1b 41 02 1b 49 04 bd 07 00 f5 1b 8e 00 01 b9 00 7f ba 00 ff 19 d1 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x8000
	MOV [0x0000], AX
	MOV AX, 0x1003
	MOV [0x0004], AX
	MOV AX, 0x0000
	MOV [0x0002], AX
	MOV CX, 0x1003

	; SBB mem,reg
	SBB AX, [BX+SI] ; 0000 - 8000, test OF, SF, CF flags

	; SBB mem+8b,reg
	SBB AX, [BX+DI+02]	; 8000 - (0000 + CF), test OF, SF, AF, PF flags
	SBB CX, [BX+DI+04]	; 1003 - 1003, test ZF flag

	; SBB mem+16b,reg
	MOV BP, 0x04
	CMC			; Set carry flag
	SBB CX, [BP+0x0100]	; 0000 - (0000 + CF), test AF, CF, overflow

	; SBB reg, reg
	MOV CX, 0x7F00
	MOV DX, 0xFF00
	SBB CX, DX	; 7F00 - FF00, test OF, CF
	HLT