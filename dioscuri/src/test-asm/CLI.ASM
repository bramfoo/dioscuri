; CLI (opcode FA), clear interrupt flag (set to 0)
; fa fa f4
start:
	CLI		; Clear interrupt flag
	CLI		; Clear interrupt flag
	HLT		; Stop execution