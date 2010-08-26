; OR_GbEb (opcode 0B), Logical word-sized OR of register (destination) and memory/register (source)
; b8 aa 55 a3 00 00 a3 02 00 b8 55 aa ba 55 aa 0b 00 40 0b 51 02
; 42 45 0b 96 00 01 09 d8 f4
start:
	; Load memory with pre-arranged values 55AA 55AA
	MOV AX, 0x55AA
	MOV [0000], AX
	MOV [0002], AX
	MOV AX, 0xAA55	; Move AA55 into AX
	MOV DX, 0xAA55	; Move AA55 into DX

	; OR reg,mem
	OR AX, [BX+SI]	; OR reg and mem, store in AX (ffff)
	INC AX			; Reset AX

	; OR reg,mem+8b
	OR DX, [BX+DI+02]	; OR reg and mem+8b, store in DX (ffff)
	INC DX			; Reset DX

	; OR reg,mem+16b
	INC BP			; Set BP to 1
	OR DX, [BP+0x0100]	; OR reg and mem+16b, store in AL (55aa)

	; OR reg, reg
	OR AX, BX	; OR 2 registers (00), check flags
	HLT		; Stop execution