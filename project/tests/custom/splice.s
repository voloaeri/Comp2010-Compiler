a : seq<int> := [1, 2, 3];

main{
b : seq<init> := [];
c : init := 5;
if(len(s) < 1) then
b := c :: a[:2];
else 
b := c :: a[1:];
fi

}
