; SBB_EvGv (opcode 19), Subtract word in register (source) + CF from memory/register (destination)
; a3 00 00 b8 03 10 a3 02 00 b8 00 80 b9 03 10 19 00 a1 00 00 19 18 a1 00 00
; 19 49 02 a1 02 00 45 f5 19 9e 00 01 a1 01 01 b9 00 7f ba 00 ff 19 d1 f4
start:
	; Load memory, registers with pre-arranged values
	MOV [0x0000], AX
	MOV AX, 0x1003
	MOV [0x0002], AX
	MOV AX, 0x8000
	MOV CX, 0x1003

	; SBB mem,reg
	SBB [BX+SI], AX ; 0000 - 8000, test OF, SF, CF flags
	MOV AX, [0x0000]  ; Display result
	SBB [BX+SI], BX ; 8000 - (0000 + CF), test OF, SF, AF, PF flags
	MOV AX, [0x0000]  ; Display result

	; SBB mem+8b,reg
	SBB [BX+DI+02], CX	; 1003 - 1003, test ZF flag
	MOV AX, [0x0002]  ; Display result

	; SBB mem+16b,reg
	INC BP
	CMC			; Set carry flag
	SBB [BP+0x0100], BX	; 0000 - (0000 + CF), test AF, CF, overflow
	MOV AX, [0x0101]	; Display result

	; SBB reg, reg
	MOV CX, 0x7F00
	MOV DX, 0xFF00
	SBB CX, DX	; 7F00 - FF00, test OF, CF
	HLT