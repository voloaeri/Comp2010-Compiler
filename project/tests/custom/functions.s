fdef foo(a : int){
one : bool := true;
two : string := "hello";
return bar(one, two);
}

fdef bar(c : bool, d : string){
print c;
return 5; 
}

main{
z : int := 3;
foo(z);
}
