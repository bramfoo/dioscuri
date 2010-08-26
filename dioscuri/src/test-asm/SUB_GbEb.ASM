; SUB_GbEb (opcode 2A), subtract register (destination) from memory/register (source)
; b8 05 7b a3 00 00 b8 85 02 2a 00 2a 00 2a 40 01 45 2a 86 00 01 28 e0 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x7B05
	MOV [0000], AX
	MOV AX, 0x0285

	; SUB mem,reg
	SUB AL, [BX+SI] ; 85 - 05, test SF, PF flags
	SUB AL, [BX+SI] ; 80 - 05, test OF, SF, AF, PF flags

	; SUB mem+8b,reg
	SUB AL, [BX+SI+01]	; 7B - 7B, test ZF

	; SUB mem+16b,reg
	INC BP			; Set BP to 1
	SUB AL, [BP+0x0100]	; 00 - 05, test CF, overflow

	; CMP reg, reg
	SUB AL, AH	; FB - 02, test CF, PF
	HLT