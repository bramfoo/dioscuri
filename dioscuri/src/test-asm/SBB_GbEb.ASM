; SBB_GbEb (opcode 1A), subtract memory/register (source) + CF from register (destination)
; b8 ff 7f a3 00 00 b8 7f 01 f5 1a 00 40 1a 41 04 45 45 1a 86 00 01
; 18 e0 18 d8 f4
start:
	; Load memory, registers with pre-arranged values
	MOV AX, 0x7FFF
	MOV [0000], AX
	MOV AX, 0x017F

	; SBB mem,reg
	CMC		; Set carry flag
	SBB AL, [BX+SI] ; 7F - (FF + CF), test AF flag

	; SBB mem+8b,reg
	INC AX			; Set AX
	SBB AL, [BX+DI+04]	; 80 - (00 + CF), test OF, SF, AF, PF flags

	; SBB mem+16b,reg
	INC BP			; Set BP to 1
	INC BP			; Set BP to 2
	SBB AL, [BP+0x0100]	; 7F - 7F, test ZF

	; SBB reg, reg
	SBB AL, AH	; 00 - 01, test CF, SBB, overflow
	SBB AL, BL	; FF - (00 + CF), test CF, PF
	HLT