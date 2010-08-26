; AND_GvEv (opcode 23), Logical word-sized AND of register (destination) and memory/register (source)
; b8 55 aa a3 00 00 a3 02 00 b8 ff ff b9 ff ff ba ff ff 23 00 23 49 02
; 45 23 96 00 01 21 d8 f4
start:
	; Load memory with pre-arranged values AA55 AA55
	MOV AX, 0xAA55
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0xFFFF	; Move FFFF into AX
	MOV CX, 0xFFFF	; Move FFFF into CX
	MOV DX, 0xFFFF	; Move FFFF into DX

	; AND reg,mem
	AND AX, [BX+SI] ; AND reg and mem, store in AX (AA55)

	; AND reg,mem+8b
	AND CX, [BX+DI+02]	; AND reg and mem+8b, store in DX (aa55)

	; AND reg,mem+16b
	INC BP			; Set BP to 1
	AND DX, [BP+0x0100]	; AND reg and mem+16b, store in DX (55aa)

	; AND reg, reg
	AND AX, BX	; AND 2 registers (00), check flags
	HLT		; Stop execution