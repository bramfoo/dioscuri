; JMP short JB (opcode EB), jump short
; eb 02 90 90 eb 93 f4
start:
	JMP further	; Jump to next label
	NOP
	NOP		; Space filler
further:
	JMP -103	  ; Test short jump range (-128...127)
	HLT

