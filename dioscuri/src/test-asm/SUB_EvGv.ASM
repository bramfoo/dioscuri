; SUB_EvGv (opcode 29), Subtract word in memory/register (destination) from register (source)
; a3 00 00 b8 03 10 a3 02 00 b8 00 80 b9 03 10 29 00 a1 00 00 29 08 a1 00 00
; 29 49 02 a1 02 00 45 29 8e 00 01 a1 01 01 b9 00 7f ba 00 ff 29 d1 f4
start:
	; Load memory, registers with pre-arranged values
	MOV [0x0000], AX
	MOV AX, 0x1003
	MOV [0x0002], AX
	MOV AX, 0x8000
	MOV CX, 0x1003

	; SUB mem,reg
	SUB [BX+SI], AX ; 0000 - 8000, test OF, SF, CF flags
	MOV AX, [0x0000]  ; Display result
	SUB [BX+SI], CX ; 8000 - 1003, test OF, SF, AF, PF flags
	MOV AX, [0x0000]  ; Display result

	; CMP mem+8b,reg
	SUB [BX+DI+02], CX	; 1003 - 1003, test ZF flag
	MOV AX, [0x0002]  ; Display result

	; CMP mem+16b,reg
	INC BP
	SUB [BP+0x0100], CX	 ; 0000 - 1003, test AF, CF, overflow
	MOV AX, [0x0101]  ; Display result

	; CMP reg, reg
	MOV CX, 0x7F00
	MOV DX, 0xFF00
	SUB CX, DX	; 7F00 - FF00, test OF, CF
	HLT