.pos 0x100

   # STATEMENT 1 : a[i] = a[i+1] + b[i+2];
	ld $i, r0				# r0 = Address of i
	ld 0(r0), r0			# r0 = i
	ld $a, r1				# r1 = Address of a
	inc r0					# r0 = i + 1
	ld (r1,r0,4), r2		# r2 = a[i+1]
	ld $b, r3				# r3 = Address of b
	inc r0					# r0 = i + 2
	ld (r3,r0,4), r3		# r3 = b[i+2]
	add r2, r3				# r3 = a[i+1] + b[i+2]
	dec r0					# r0 = i + 1
	dec r0					# r0 = i
	st r3, (r1,r0,4)		# a[i] = a[i+1] + b[i+2]
    

    # STATEMENT 2 : d[i] = a[i] + b[i];
	ld $i, r0				# r0 = Address of i
	ld 0(r0), r0			# r0 = i
	ld $a, r1				# r1 = Address of a
	ld (r1,r0,4), r1		# r1 = a[i]
	ld $b, r2				# r2 = Address of b
	ld (r2,r0,4), r2		# r2 = b[i]
	add r1, r2				# r2 = a[i] + b[i]
	ld $d, r1				# r1 = Address of d pointer
	ld 0(r1), r1			# r1 = Address of d array
	st r2, (r1,r0,4)		# d[i] = a[i] + b[i]
   

    # STATEMENT 3 : d[i] = a[b[i]] + b[a[i]];
	ld $i, r0				# r0 = address of i
	ld 0(r0), r0			# r0 = i
	ld $b, r1				# r1 = address of b
	ld $a, r2				# r2 = address of a
	ld (r1,r0,4), r3		# r3 = b[i]
	ld (r2,r0,4), r4		# r4 = a[i]
	ld (r2,r3,4), r3		# r3 = a[b[i]]
	ld (r1,r4,4), r4		# r4 = b[a[i]]
	add r3, r4				# r4 = a[b[i]] + b[a[i]]
	ld $d, r1				# r1 = address of d
	ld 0r1, r1			 	# r1 = address of array d[0]
	st r4, (r1,r0,4)		# d[i] = r4 = a[b[i]] + b[a[i]]

	#STATEMENT 4: d[b[i]] = b[a[i & 3] & 3] - a[b[i & 3] & 3] + d[i];
	
	ld $i, r0			# r0 = address of i
	ld 0(r0), r0		# r0 = i
	ld $3, r1			# r1 = 3
	ld $3, r2			# r2 = 3
	and r0, r1			# r1 = i &3
	ld $a, r3			# r3 = address of a
	ld $b, r4			# r4 = address of b
	ld (r3,r1,4), r5	# r5 = a[i & 3]
	and r2, r5			# r5 = a[i & 3] & 3
	ld (r4,r5,4), r5	# r5 = b[a[i & 3] & 3]
	ld (r4,r1,4), r6	# r6 = b[i & 3]
	and r1, r6			# r6 = b[i & 3] & 3
	ld (r3,r6,4), r1	# r1 = a[b[i & 3] & 3]	
	not r1				# r1 = ~a[b[i & 3] & 3]
	inc r1				# r1 = ~a[b[i & 3] & 3] + 1 = - a[b[i & 3] & 3]
	add r6, r1			# r1 = b[a[i & 3] & 3] - a[b[i & 3] & 3]
	ld $d, r2			# r2 = address of d
	ld 0(r2), r2		# r2 = d (address)
	ld (r2,r0,4), r3	# r3 = d[i]
	add r3, r1			# r1 = b[a[i & 3] & 3] - a[b[i & 3] & 3] + d[i]
	ld (r4,r0,4), r3	# r3 = b[i]
	st r1, (r2,r3,4)	# d[b[i]] = r1 = b[a[i & 3] & 3] - a[b[i & 3] & 3] + d[i]
	
	
	
    halt

.pos 0x200
# Data area

a:  .long 0             # a[0]
    .long 0             # a[1]
    .long 0             # a[2]
    .long 0             # a[3]
    .long 0             # a[4]
    .long 0             # a[5]
    .long 0             # a[6]
    .long 0             # a[7]
b:  .long 0             # b[0]
    .long 0             # b[1]
    .long 0             # b[2]
    .long 0             # b[3]
    .long 0             # b[4]
    .long 0             # b[5]
    .long 0             # b[6]
    .long 0             # b[7]
i:  .long 0				# i
d:  .long 0x1000        # d

.pos 0x1000
# The array pointer d is holding the address of this unnamed array
    .long 0
    .long 0
    .long 0
    .long 0
    .long 0
    .long 0
    .long 0
    .long 0
    

	
	