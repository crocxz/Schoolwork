.pos 0x100

#c = 5	
ld $c, r0 		#r0 = address of c	
ld $5, r1	#r1 = 5
st r1, 0(r0)	#c = 5

#b = c+10
ld $c, r0 		#r0 = address of c	
ld 0x0(r0), r0	#r0 = (value of) c
ld $10, r1	#r1 = 10
ld $b, r2 		#r2 = address of b	
ld 0x0(r2), r2	#r2 = (value of) b
add r1, r0	#r0 = c + 10
st r0, 0x0(r2)	#r2 = r0 = c+ 10

#a[8] = 8
ld $8, r0	#r0 = 8
ld $a, r1	#r1 = address of a
st r0, (r1,r0,4)	#a[8] = 8

#a[4] = a[4] + 4;
ld $a, r0	#r0 = address of a
ld $4, r1	#r1 = 4
ld 16(r0), r2	#r2 = a[4]
add r1, r2	#r2 = a[4] + 4
st r2, (r0,r1,4) #r2 = r1 = a[4] + 4

# a[c] = a[8] + b + a[b & 0x7]
ld $c, r0			#r0 = address of c
ld 0(r0), r0		#r0 = c
ld $a, r1			#r1 = address of a
ld 32(r1), r2		#r2 = value of a[8]
ld $b, r3			#r3 = address of b
ld 0(r3), r3		#r3 = b
ld $0x7, r4			#r4 = 0x7
and r3, r4			#r4 = b & 0x7
ld (r1,r4,4), r4	#r4 = a[b & 7]
add r2, r3			# r3 = b + a[8]
add r3, r4			#r4 = a[8] + b + a[b &7]
st r4, (r1,r0,4)	#a[c] = r4 = a[8] + b + a[b &7]

halt

.pos 0x200

#data area

b: .long 0 #b
c: .long 0 #c
a: .long 0 #a0
.long 0 #a1
.long 0 #a2
.long 0 #a3
.long 0 #a4
.long 0 #a5
.long 0 #a6
.long 0 #a7
.long 0 #a8
.long 0 #a9
