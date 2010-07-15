; MOV_GvEv (opcode 8B), copy contents of register (destination) from memory/register (source)
; b8 bb aa a3 00 00 a3 02 00 8b 08 8b 51 02 45 8b 9e 00 01 89 dd f4
start:
	; Load memory with pre-arranged values
	MOV AX, 0xAABB
	MOV [0000], AX
	MOV [0002], AX

	; MOV mem,reg
	MOV CX, [BX+SI] ; MOV reg and mem, store in memory

	; MOV mem+8b,reg
	MOV DX, [BX+DI+02]	; MOV reg and mem+8b, store in memory

	; MOV mem+16b,reg
	INC BP			; Set BP to 1
	MOV BX, [BP+0x0100]	; MOV reg and mem+16b, store in memory

	; MOV reg, reg
	MOV BP, BX	; MOV 2 registers
	HLT		; Stop execution