; OR_GbEb (opcode 0A), Logical byte-sized OR of register (destination) and memory/register (source)
; b8 aa 55 a3 00 00 b8 55 aa 0a 00 0a 61 01 40 45 0a 86 00 01 08 fa f4
start:
	; Load memory with pre-arranged values 55AA
	MOV AX, 0x55AA
	MOV [0000], AX
	MOV AX, 0xAA55	; Move AA55 into AX

	; OR reg,mem
	OR AL, [BX+SI]	; OR reg and mem, store in AL (ff)

	; OR reg,mem+8b
	OR AH, [BX+DI+01]	; OR reg and mem+8b, store in AH (ff)
	INC AX			; Reset AX to 00

	; OR reg,mem+16b
	INC BP			; Set BP to 1
	OR AL, [BP+0x0100]	; OR reg and mem+16b, store in AL (aa)

	; OR reg, reg
	OR DL, BH	; OR 2 registers (00), check flags
	HLT		; Stop execution