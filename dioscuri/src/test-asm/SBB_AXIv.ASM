; SBB_AXIv (opcode 1D), subtract (immediate word + CF) from AX
; 1d 00 80 1d 03 10 1d fc 6f 1d 01 10 1d 01 10 1d fd 60 1d 00 ff f4
start:
	SBB ax, 0x8000	; Test OF, SF, CF
	SBB ax, 0x1003	; Test OF, SF, AF, PF flags
	SBB ax, 0x6FFC	; Test ZF
	SBB ax, 0x1001	; Test AF, CF, overflow
	SBB ax, 0x1001	; Test CF, PF, SBB
	SUB ax, 0x60FD	; Prepare special case
	SUB ax, 0xFF00	; Test OF, CF
	HLT