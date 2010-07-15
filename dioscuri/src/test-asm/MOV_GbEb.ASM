; MOV_GbEb (opcode 8A), copy contents of register (destination) from memory/register (source)
; b8 bb aa a3 00 00 8a 08 8a 71 01 45 8a 9e 00 01 88 dc f4
start:
	; Load memory with pre-arranged values
	MOV AX, 0xAABB
	MOV [0000], AX

	; MOV mem,reg
	MOV CL, [BX+SI] ; MOV reg and mem, store in memory

	; MOV mem+8b,reg
	MOV DH, [BX+DI+01]	; MOV reg and mem+8b, store in memory

	; MOV mem+16b,reg
	INC BP			; Set BP to 1
	MOV BL, [BP+0x0100]	; MOV reg and mem+16b, store in memory

	; MOV reg, reg
	MOV AH, BL	; MOV 2 registers
	HLT		; Stop execution