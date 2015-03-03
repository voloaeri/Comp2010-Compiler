main{
person := "Shin", "Yoo", 30, fibonacci;
x : dict<int,int> := { }; 
x : dict<int,int> := { }; 
print person.fibonacci(5);
#x : dict<int,int> := { }; 
}

fdef fibonacci( pos : int ) { 
	if (pos = -1) then
		return 0;
	fi
	if (pos = 0) then
		return 1;
	fi	
	return fibonacci(pos-1) + fibonacci(pos-2);
} ;
