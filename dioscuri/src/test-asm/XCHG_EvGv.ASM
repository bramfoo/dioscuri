; XCHG_EvGv (opcode 87), exchange contents of memory/register (destination) and register (source)
; b8 bb aa a3 00 00 a3 02 00 b8 dd cc b9 ff ee 87 00 a1 00 00 87 49 02
; a1 02 00 45 87 86 00 01 a1 01 01 93 f4
start:
	; Load memory with pre-arranged values
	MOV AX, 0xAABB
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0xCCDD
	MOV CX, 0xEEFF

	; XCHG mem,reg
	XCHG [BX+SI], AX; XCHG reg and mem, store in memory
	MOV AX, [0000]	; Retrieve result from memory (result = bb)

	; XCHG mem+8b,reg
	XCHG [BX+DI+02], CX	; XCHG reg and mem+8b, store in memory
	MOV AX, [0002]		; Retrieve result from memory (result = aa)

	; XCHG mem+16b,reg
	INC BP			; Set BP to 1
	XCHG [BP+0x0100], AX	; XCHG reg and mem+16b, store in memory
	MOV AX, [0x0101]	; Retrieve result from memory (result = aa) [NOTE: ACCESSING BYTE 2 OF OWN CODE]

	; XCHG reg, reg
	XCHG AX, BX	; XCHG 2 registers (result = 00)
	HLT		; Stop execution