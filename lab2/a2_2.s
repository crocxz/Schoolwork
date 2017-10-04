#int a;
#int b;
#int c;
#int d[10];

#. . . 

#d[a] = a - b;
#d[a+1] = a | c;
#d[b+2] = (2*a + 4*c) - b;
#d[d[4]+3] = (a << 2) & (c << 3) & ~b & 0xff;



.pos 0x100

	# d[a] = a - b
#	ld $a, r0				# r0 = Address of a
#	ld 0(r0), r0			# r0 = a
#	ld $b, r1				# r1 = Address of b
#	ld 0(r1), r1			# r1 = b
#	not r1					# r1 = ~b
#	inc r1					# r1 = ~b + 1   ==   r1 = -b
#	add r0, r1				# r1 = a - b
#	ld $d, r2				# r2 = Address of d
#	st r1, (r2,r0,4)		# d[a] = a - b
	
	# d[a+1] = a | c
#	ld $a, r0				# r0 = Address of a
#	ld 0(r0), r0			# r0 = a
#	ld $c, r1				# r1 = Address of c
#	ld 0(r1), r1			# r1 = c
#	not r0					# r0 = ~a
#	not r1					# r1 = ~c
#	and r0, r1				# r1 = ~a & ~c   ==    r1 = ~(a | c)
#	not r1					# r1 = 
#	not r0					# r0 = a
#	inc r0					# r0 = a + 1
#	ld $d, r2				# r2 = Address of d
#	st r1, (r2,r0,4)		# d[a] = a | c
	
	# d[b+2] = (2*a + 4*c) - b;
#	ld $a, r0				# r0 = Address of a
#	ld 0(r0), r0			# r0 = a
#	shl $1, r0				# a << 1   ==   a = 2*a
#	ld $c, r1				# r1 = Address of c
#	ld 0(r1), r1			# r1 = c
#	shl $2, r1				# c << 2   ==   c = 4*c
#	add r0, r1				# r1 = (2*a + 4*c)
#	ld $b, r2				# r2 = Address of b
#	ld 0(r2), r2			# r2 = b
#	not r2					# r2 = ~b
#	inc r2					# r2 = ~b + 1   ==   r2 = -b
#	add r2, r1				# r1 = (2*a + 4*c) - b
#	dec r2					# r2 = ~b
#	not r2					# r2 = b
#	inc r2					# r2 = b + 1
#	inc r2 					# r2 = b + 2
#	ld $d, r3				# r3 = Address of d
#	st r1, (r3,r2,4)		# d[b+2] = (2*c + 4*c) - b
	
	# d[d[4]+3] = (a << 2) & (c << 3) & ~b & 0xff
	ld $a, r0				# r0 = Address of a
	ld 0(r0), r0			# r0 = a
	shl $2, r0				# r0 = a << 2
	ld $c, r1				# r1 = Address of c
	ld 0(r1), r1			# r1 = c
	shl $3, r1				# r1 = c << 3
	and r1, r0				# r0 = (a << 2) & (c << 3)
	ld $b, r1				# r1 = Address of b
	ld 0(r1), r1			# r1 = b
	not r1					# r1 = ~b
	and r1, r0				# r0 = (a << 2) & (c << 3) & ~b
	ld $0xff, r1			# r1 = 0xff
	and r1, r0				# r0 = (a << 2) & (c << 3) & ~b & 0xff
	ld $d, r1				# r1 = Address of d
	ld $4, r2				# r2 = Address of 4
	ld 0(r2), r2			# r2 = 4
	ld (r1,r2,4), r2		# r2 = d[4]
	inc r2					# r2 = d[4] + 1
	inc r2					# r2 = d[4] + 2
	inc r2					# r2 = d[4] + 3
	st r0, (r1,r2,4)		# d[d[4]+3] = (a << 2) & (c << 3) & ~b & 0xff

	halt
	
	
.pos 0x200
# Data area

a:  .long 5             # a
b:  .long 5             # b
c:  .long 3             # c
d:  .long 1             # d[0]
    .long 1             # d[1]
    .long 1             # d[2]
    .long 1             # d[3]
    .long 1             # d[4]
    .long 1             # d[5]
    .long 1             # d[6]
    .long 1             # d[7]
	.long 1				# d[8]
	.long 1				# d[9]
	
	
	
	
	


