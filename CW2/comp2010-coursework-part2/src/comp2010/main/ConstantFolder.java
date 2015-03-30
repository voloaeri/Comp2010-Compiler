package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LDC_W;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.IFLE;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.IFGE;
import org.apache.bcel.generic.IFGT;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IF_ICMPGE;
import org.apache.bcel.generic.IF_ICMPGT;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.IF_ICMPNE;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.DCONST;
import org.apache.bcel.generic.FCONST;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.LoadInstruction;

public class ConstantFolder
{
	
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	

	private enum OperationType 
	{
		ADD, SUB, DIV, MUL, NEG, REM, AND, OR, SHL, SHR, USHR, XOR, CMP, NONE
	}
	
	private OperationType getOpType(Instruction instr) 
	{
		switch (instr.getOpcode()) 
		{
			case Constants.IADD: //Fall through
			case Constants.LADD: //Fall through
			case Constants.FADD: //Fall through
			case Constants.DADD: 
				return OperationType.ADD;

			case Constants.ISUB: //Fall through
			case Constants.LSUB: //Fall through
			case Constants.FSUB: //Fall through
			case Constants.DSUB: 
				return OperationType.SUB;

			case Constants.IMUL: //Fall through
			case Constants.LMUL: //Fall through
			case Constants.FMUL: //Fall through
			case Constants.DMUL: 
				return OperationType.MUL;

			case Constants.IDIV: //Fall through
			case Constants.LDIV: //Fall through
			case Constants.FDIV: //Fall through
			case Constants.DDIV: 
				return OperationType.DIV;

			case Constants.INEG: //Fall through
			case Constants.LNEG: //Fall through
			case Constants.FNEG: //Fall through
			case Constants.DNEG: 
				return OperationType.NEG;

			case Constants.IREM: //Fall through
			case Constants.LREM: //Fall through
			case Constants.FREM: //Fall through
			case Constants.DREM: 
				return OperationType.REM;

			case Constants.DCMPG: //Fall through
			case Constants.DCMPL: //Fall through
			case Constants.FCMPG: //Fall through
			case Constants.FCMPL: //Fall through
			case Constants.LCMP: 
			case Constants.IFNE:
			case Constants.IFLE:
			case Constants.IFLT:
			case Constants.IFGE:
			case Constants.IFGT:
			case Constants.IFEQ:
			case Constants.IF_ICMPEQ:
			case Constants.IF_ICMPNE:
			case Constants.IF_ICMPLT:
			case Constants.IF_ICMPGE:
			case Constants.IF_ICMPGT:
			case Constants.IF_ICMPLE:
				return OperationType.CMP;

			case Constants.IAND: //Fall through
			case Constants.LAND: //Fall through
				return OperationType.AND;

			case Constants.IOR: //Fall through
			case Constants.LOR: //Fall through
				return OperationType.OR;

			case Constants.ISHL: //Fall through
			case Constants.LSHL: //Fall through
				return OperationType.SHL;

			case Constants.ISHR: //Fall through
			case Constants.LSHR: //Fall through
				return OperationType.SHR;

			case Constants.IUSHR: //Fall through
			case Constants.LUSHR: //Fall through
				return OperationType.USHR;

			case Constants.IXOR:
			case Constants.LXOR:
				return OperationType.XOR;

			default:
				return OperationType.NONE;
		}
	}
	
	private Number add(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()+((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()+((Number)b).longValue());			
			case Constants.T_FLOAT:
				return new Float(((Number)a).floatValue()+((Number)b).floatValue());
			case Constants.T_DOUBLE:
				return new Double(((Number)a).doubleValue()+((Number)b).doubleValue());
			default:
				return null;
		}
	}

	private Number sub(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()-((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()-((Number)b).longValue());			
			case Constants.T_FLOAT:
				return new Float(((Number)a).floatValue()-((Number)b).floatValue());
			case Constants.T_DOUBLE:
				return new Double(((Number)a).doubleValue()-((Number)b).doubleValue());
			default:
				return null;
		}
	}
	private Number mul(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()*((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()*((Number)b).longValue());			
			case Constants.T_FLOAT:
				return new Float(((Number)a).floatValue()*((Number)b).floatValue());
			case Constants.T_DOUBLE:
				return new Double(((Number)a).doubleValue()*((Number)b).doubleValue());
			default:
				return null;
		}
	}
	private Number div(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()/((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()/((Number)b).longValue());			
			case Constants.T_FLOAT:
				return new Float(((Number)a).floatValue()/((Number)b).floatValue());
			case Constants.T_DOUBLE:
				return new Double(((Number)a).doubleValue()/((Number)b).doubleValue());
			default:
				return null;
		}
	}

	private Number neg(Type t, Object a) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(-((Number)a).intValue());
			case Constants.T_LONG:
				return new Long(-((Number)a).longValue());			
			case Constants.T_FLOAT:
				return new Float(-((Number)a).floatValue());
			case Constants.T_DOUBLE:
				return new Double(-((Number)a).doubleValue());
			default:
				return null;
		}
	}

	private Number rem(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()%((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()%((Number)b).longValue());			
			case Constants.T_FLOAT:
				return new Float(((Number)a).floatValue()%((Number)b).floatValue());
			case Constants.T_DOUBLE:
				return new Double(((Number)a).doubleValue()%((Number)b).doubleValue());
			default:
				return null;
		}
	}

	private Number shl(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()<<((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()<<((Number)b).longValue());	
			default:
				return null;
		}
	}

	private Number shr(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()>>((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()>>((Number)b).longValue());	
			default:
				return null;
		}
	}

	private Number ushr(Type t, Object a, Object b) 
	{
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)a).intValue()>>>((Number)b).intValue());
			case Constants.T_LONG:
				return new Long(((Number)a).longValue()>>>((Number)b).longValue());	
			default:
				return null;
		}
	}

	private Integer logic(OperationType t, Object a, Object b)
	{
		switch(t) {
			case AND:
				return new Integer((Boolean)a && (Boolean)b? 1 : 0);
			case OR:
				return new Integer((Boolean)a || (Boolean)b? 1 : 0);
			case XOR:
				return new Integer((Boolean)a ^ (Boolean)b? 1 : 0);
			default:
				return null;
		}
	}

	// Convert boolean to int (return a if b is true, else return 0)
	private int b2i(boolean b)
	{
		return b?1:0;
	}

	private Integer cmp(Instruction instr, Object a, Object b)
	{
		if (instr instanceof FCMPG)
			return new Integer(b2i((Float)a > (Float)b));
		else if (instr instanceof DCMPG)
			return new Integer(b2i((Double)a > (Double)b));
		else if (instr instanceof FCMPL)
			return new Integer(b2i((Float)a < (Float)b));
		else if (instr instanceof DCMPL)
			return new Integer(b2i((Double)a < (Double)b));
		else if (instr instanceof IF_ICMPEQ || instr instanceof IFEQ)
			return new Integer(b2i(((Integer)a).equals((Integer)b)));
		else if (instr instanceof IF_ICMPNE || instr instanceof IFNE)
			return new Integer(b2i(!((Integer)a).equals((Integer)b)));
		else if (instr instanceof IF_ICMPGE || instr instanceof IFGE)
			return new Integer(b2i((Integer)a >= (Integer)b));
		else if (instr instanceof IF_ICMPLE || instr instanceof IFLE)
			return new Integer(b2i((Integer)a <= (Integer)b));
		else if (instr instanceof IF_ICMPGT || instr instanceof IFGT)
			return new Integer(b2i((Integer)a > (Integer)b));
		else if (instr instanceof IF_ICMPLT || instr instanceof IFLT)
			return new Integer(b2i((Integer)a < (Integer)b));
		else if (instr instanceof LCMP)
			return new Integer(Long.compare((Long)a, (Long)b));
		else
			return null;
	}

	private Number calc(Instruction instr, ConstantPoolGen cpgen, Object a, Object b) 
	{
		OperationType t = getOpType(instr);
		switch (t) 
		{
			case ADD:
				return this.add(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case SUB:
				return this.sub(((ArithmeticInstruction)instr).getType(cpgen), b,a);
			case MUL:
				return this.mul(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case DIV:
				return this.div(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case NEG:
				return this.neg(((ArithmeticInstruction)instr).getType(cpgen), a);
			case REM:
				return this.rem(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case SHL:
				return this.shl(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case SHR:
				return this.shr(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case USHR:
				return this.ushr(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case AND:
			case OR:
			case XOR:
				return this.logic(t, a, b);
			case CMP:
				return this.cmp(instr, a, b);

			default:
				return null;
		}

	}

	private Number createObject(Type t, Object var) {
		switch (t.getType()) 
		{
			case Constants.T_INT:
				return new Integer(((Number)var).intValue());
			case Constants.T_LONG:
				return new Long(((Number)var).longValue());			
			case Constants.T_FLOAT:
				return new Float(((Number)var).floatValue());
			case Constants.T_DOUBLE:
				return new Double(((Number)var).doubleValue());
			default:
				return null;
		}
	}

	

	// Checks whether constant is pushed to stack and returns the constant
	// If instruction is not a push instruction, it returns null
	private Number isPushInstruction(Instruction instr, ConstantPoolGen cpgen)
	{
		if (instr instanceof LDC) 
		{			
			LDC ldc = (LDC) instr;
			try 
			{	
				return (Number)ldc.getValue(cpgen);
			}
			catch (ClassCastException e)
			{
				System.out.println("Could not cast constant to Number. Skipping this one");
				return null;
			}
			
		}
		else if (instr instanceof LDC2_W) 
		{
			LDC2_W ldc2w = (LDC2_W) instr;
			try 
			{	
				return (Number)ldc2w.getValue(cpgen);
			}
			catch (ClassCastException e)
			{
				System.out.println("Could not cast constant to Number. Skipping this one");
				return null;
			}
		}
		else if (instr instanceof BIPUSH) 
		{
			BIPUSH bipush = (BIPUSH) instr;
			return bipush.getValue();
		}
		else if (instr instanceof SIPUSH) 
		{
			SIPUSH sipush = (SIPUSH) instr;
			return sipush.getValue();
		}
		else if (instr instanceof ICONST )
		{
			ICONST iconst = (ICONST) instr;
			return iconst.getValue();
		}
		else if (instr instanceof DCONST )
		{
			DCONST dconst = (DCONST) instr;
			return dconst.getValue();
		}
		else if (instr instanceof FCONST )
		{
			FCONST fconst = (FCONST) instr;
			return fconst.getValue();
		}
		else if (instr instanceof LCONST )
		{
			LCONST lconst = (LCONST) instr;
			return lconst.getValue();
		}
		else
			// No push instruction
			return null;
	}

	private boolean isCmpInstruction(Instruction instr)
	{
		return instr instanceof DCMPG 
			|| instr instanceof DCMPL 
			|| instr instanceof FCMPG 
			|| instr instanceof FCMPL  
			|| instr instanceof LCMP
			|| instr instanceof IfInstruction;
	}

	private void cleanUpInstructionList(Map<Integer, ArrayList<InstructionHandle>> map, InstructionList instList)
	{
		//System.out.println(instList);
		ArrayList<Integer> removeEntries = new ArrayList<Integer>();

		// Iterate over all entries in our map
		for (Map.Entry<Integer, ArrayList<InstructionHandle>> entry : map.entrySet())
		{
			int key = entry.getKey().intValue();
			boolean remove = true;

			// Iterate over all instructions in instList and check whether there is a load for
			// the current Key. If not, prepare the entry to be deleted
			Iterator<InstructionHandle> it = instList.iterator();
			while (it.hasNext()) 
			{
				InstructionHandle handle = (InstructionHandle) it.next();
				//System.out.println(handle+" ("+System.identityHashCode(handle)+")");

				Instruction instr = handle.getInstruction();
				
				if (instr instanceof LoadInstruction)
				{
					LoadInstruction load = (LoadInstruction) instr;
					if (key == load.getIndex())
					{
						// Found a load instruction for that key
						remove = false;
						// We found at least one load, so we can finish here
						break;
					}
				}
			}
			if(remove)
				// No load for that key was found.
				// I.e. this entry is to be deleted.
				removeEntries.add(key);
		}
		
		// Remove entries
		for (Integer key : removeEntries)
		{
			System.out.println("Remove block #"+key+":");
			System.out.println(map.get(key));

			// Iterate over all instructions of that entry
			for (InstructionHandle handle : map.get(key))
			{
				try
				{
					System.out.println("\tDelete "+handle+" ("+System.identityHashCode(handle)+")");
					//System.out.println("\tDelete "+handle);
					instList.delete(handle);
				}
				catch (TargetLostException e)
				{
					InstructionHandle[] targets = e.getTargets();
			         for(int i=0; i < targets.length; i++) {
			           InstructionTargeter[] targeters = targets[i].getTargeters();
			     
			           for(int j=0; j < targeters.length; j++)
			             targeters[j].updateTarget(targets[i], null);
       				}
				}
			}

			// Finally remove key from map (not necessary but clean)
			map.remove(key);
		}
	}

	private void performReduction(Deque<InstructionHandle> instructionStack, 
								  InstructionList instList, 
								  Deque<Integer> pushInstructionStack, 
								  int instrPointer)
	{
	
		// Get initial push instruction for this block (the last instruction that will be removed)
		int lastPush = pushInstructionStack.pop().intValue();
		System.out.println("Reduce first "+(instrPointer-lastPush)+" instructions of instruction stack from instruction list:");
		System.out.println(instructionStack);

		int count = lastPush;

		// Delete all instructions between lastPush and instrPointer
		while (count < instrPointer)
		{
			InstructionHandle h = instructionStack.pop();

			try
			{
				System.out.println("\tDelete "+h);
				instList.delete(h);	
			}
			catch (TargetLostException e)
			{
				InstructionHandle[] targets = e.getTargets();
				for(int i=0; i < targets.length; i++) {
					InstructionTargeter[] targeters = targets[i].getTargeters();

					for(int j=0; j < targeters.length; j++)
						targeters[j].updateTarget(targets[i], null);
				}	
			}

			count++;
		} 
	}

	private void constantFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{
		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();
		//System.out.println(methodCode);
		InstructionList instList = new InstructionList(methodCode.getCode());

		// Initialise a method generator with the original method as the baseline	
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), gen.getClassName(), instList, cpgen);
		
		Deque<Number> constantStack = new ArrayDeque<Number>();
		Deque<InstructionHandle> instructionStack = new ArrayDeque<InstructionHandle>();
		Deque<Integer> pushInstructionStack = new ArrayDeque<Integer>();
		Map<Integer, ArrayList<InstructionHandle>> instructionMap = new HashMap<Integer, ArrayList<InstructionHandle>>();
		Map<Integer, Number> constantMap = new HashMap<Integer, Number>();

		boolean remove = false;
		boolean changed = false;

		// Counts the amount of instructions on instruction stack 
		// Always increased, however, when a store is executed, it will be reduces by the mount of instructions popped from stack
		int instrPointer = 0;
		
		ConstantPool cp = cpgen.getConstantPool();

		for (InstructionHandle handle : instList.getInstructionHandles()) 
		{
			//System.out.println("Current handle: "+handle);
			Instruction instr = handle.getInstruction();

			if (!changed)
			{
				// Check whether instruction is a direct push operation
				Number constant = isPushInstruction(instr, cpgen);
				if (constant != null) 
				{
					pushInstructionStack.push(instrPointer++);
					instructionStack.push(handle);
					constantStack.push(constant);
					remove = true;
				}
				else if (remove)
				{
					if (instr instanceof LoadInstruction) 
					{
						// Instruction is an indirect push operation 
						// It loads constant from local variables and pushes it on the stack

						LoadInstruction load = (LoadInstruction) instr;
						instructionStack.push(handle);
						pushInstructionStack.push(instrPointer++);

						int index = load.getIndex();
						Number value = constantMap.get(index);
						constantStack.push(value);
					}
					else if (instr instanceof ConversionInstruction) 
					{
						ConversionInstruction convInstr = (ConversionInstruction) instr;
						Number var = constantStack.pop();
						var = createObject(convInstr.getType(cpgen), var);

						constantStack.push(var);
						instructionStack.push(handle);
						instrPointer++;
					}
					else if (instr instanceof StoreInstruction) 
					{
						StoreInstruction store = (StoreInstruction) instr;
						instructionStack.push(handle);

						ArrayList<InstructionHandle> handleList = new ArrayList<InstructionHandle>();

						int lastPush = pushInstructionStack.pop().intValue();

						System.out.println("Store block in instruction map:");

						// Remove the necessary handles from stack and save them in instructionMap
						// Also decrease
						while (instrPointer >= lastPush)
						{
							InstructionHandle h = instructionStack.pop();
							instrPointer--;
							System.out.println("\t"+h);
							handleList.add(h);
						}
						
						int index = store.getIndex();

						Number value = constantStack.pop();
						
						if (instructionMap.containsKey(index))
							instructionMap.get(index).addAll(handleList);
						else
							instructionMap.put(index, handleList);

						constantMap.put(index, value);
					}
					else if (remove && instr instanceof ArithmeticInstruction) // Perform calculation
					{
						ArithmeticInstruction arith = (ArithmeticInstruction) instr;

						remove = false; // Found an atihmetic operation ==> stop removing

						//System.out.println(constantStack);

						// Get last two loaded constants from constantStack
						Object a = constantStack.pop();
						
						Object b;
						if (constantStack.isEmpty())
							b = new Object();
						else
							b = constantStack.pop();
						
						// Remove index of last push operation (since it will be removed at the end)
						pushInstructionStack.pop();

						// Perform calculation
						Number result = calc(arith, cpgen, a, b);

						constantStack.push(result);
						
						int index;
						Instruction newInstr;
						switch(arith.getType(cpgen).getType()) 
						{
							case Constants.T_INT:
								index = cpgen.addInteger(((Integer)result).intValue());
								newInstr = new LDC(index);
								break;
							case Constants.T_FLOAT:
								index = cpgen.addFloat(((Float)result).floatValue());
								newInstr = new LDC(index);
								break;
							case Constants.T_LONG:
								index = cpgen.addLong(((Long)result).longValue());			
								newInstr = new LDC2_W(index);
								break;
							case Constants.T_DOUBLE:
								index = cpgen.addDouble(((Double)result).doubleValue());
								newInstr = new LDC2_W(index);
								break;
							default:
								index = -1;
								newInstr = null;
						}

						// Add result to instruction list
						System.out.println("Insert " + newInstr + " before " + handle);
						InstructionHandle newHandle = instList.insert(handle, newInstr);

						instructionStack.push(handle);

						//pushInstructionStack.push(instrPointer);

						//System.out.println(instructionStack);				
						instrPointer++;
						changed = true;
						// Only perform one folding at a time. Break out of loop
						break;

					}
					else if (isCmpInstruction(instr))
					{
						// Get last two loaded constants from constantStack
						System.out.println(constantStack);
						Object a = constantStack.pop();
						Object b;
						if (instr instanceof IFLE || instr instanceof IFLT || instr instanceof IFGE || instr instanceof IFGT || instr instanceof IFEQ || instr instanceof IFNE)
							b = new Integer(0);
						else
							b = constantStack.pop();

						Integer result = (Integer) calc(instr, cpgen, a, b);
						int index = cpgen.addInteger(((Integer)result).intValue());
						Instruction newInstr = new LDC(index);

						System.out.println("Insert " + newInstr + " before " + handle);
						
						// TODO: Commented out temporarily!!!
						//InstructionHandle newHandle = instList.insert(handle, newInstr);
						
						//instructionStack.push(handle);
						
						// Remove index of last push operation (since it will be removed at the end)
						pushInstructionStack.pop();

						//instrPointer++;
						changed = true;

						// Only perform one folding at a time. Break out of loop
						break;
					}
				}
			}
			
			//System.out.println("#instructions on stack: "+instrPointer);
		}

		// Perform instruction reduction step
		if (changed)
		{
			performReduction(instructionStack, instList, pushInstructionStack, instrPointer);
			
			cleanUpInstructionList(instructionMap, instList);
			
			// Give all instructions their position number (offset in byte stream), i.e., make the list ready to be dumped.
			instList.setPositions(true);

			// set max stack/local
			methodGen.setMaxStack();
			methodGen.setMaxLocals();

			// generate the new method with replaced iconst
			Method newMethod = methodGen.getMethod();
			// replace the method in the original class
			gen.replaceMethod(method, newMethod);

			constantFolding(gen, cpgen, newMethod);
		}
	}


	private void dynamicFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{

	}

	private void optimizeMethod(ClassGen gen, ConstantPoolGen cpgen, Method method)
	{
		constantFolding(gen, cpgen, method);
		dynamicFolding(gen, cpgen, method);
	}

	public void optimize()
	{
		//System.out.println("Optimize");
		ClassGen gen = new ClassGen(original);

		ConstantPoolGen cpgen = gen.getConstantPool();

		// get the current constant pool
		ConstantPool cp = cpgen.getConstantPool();
		// get the constants in the pool
		
		Constant[] constants = cp.getConstantPool();

		Method[] methods = gen.getMethods();
		for (Method m : methods)
		{
			// Iterate over every method object
			System.out.println(">>>>>> Optimize method: "+cp.getConstant(m.getNameIndex()));
			optimizeMethod(gen, cpgen, m);
			System.out.println("<<<<<< Optimization done: "+cp.getConstant(m.getNameIndex()));
		}

		// Do your optimization here
		this.optimized = gen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();
		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}