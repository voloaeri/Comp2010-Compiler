fdef reverse (inseq : seq<top>) { 
outseq : seq<top> := [];
i : int := 0;
while (i < len(inseq)) do
   outseq := inseq[i] :: outseq;
i := i + 1;
od
return outseq; } : seq<top> ;

main{
#print "hello, world";
s : seq<top> := [ 1, 1/2, 3.14, [ 'f', 'o', 'u', 'r'] ];
print reverse(s);
}
