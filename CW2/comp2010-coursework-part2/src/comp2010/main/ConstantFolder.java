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

// Import Instruction types
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
import org.apache.bcel.generic.GOTO;

// Import  
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.BranchHandle;

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
	
	// Enum that represents the different operation types we must handle
	private enum OperationType 
	{
		ADD, SUB, DIV, MUL, NEG, REM, AND, OR, SHL, SHR, USHR, XOR, CMP, NONE
	}
	
		
/********* Calculation part *********/

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

	// Perform modulo operation
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

	// Shift left
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

	// Shift right
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

	// Unsigned shift right
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

	// Perform logic operations AND, OR, XOR
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

	// Convert boolean to int (return 1 if b is true, else return 0)
	private int b2i(boolean b)
	{
		return b?1:0;
	}

	
	// Perform comparison operations and return an integer 
	// (0: false, 1: true for every instruction except for
	// the instruction LCMP, which returns -1: less than, 0: equal or 1: greater than)
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

/********* End of Calculation part *********/

	// Return operation type for a given instruction
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
			case Constants.LCMP: //Fall through
			case Constants.IFNE: //Fall through
			case Constants.IFLE: //Fall through
			case Constants.IFLT: //Fall through
			case Constants.IFGE: //Fall through
			case Constants.IFGT: //Fall through
			case Constants.IFEQ: //Fall through
			case Constants.IF_ICMPEQ: //Fall through
			case Constants.IF_ICMPNE: //Fall through
			case Constants.IF_ICMPLT: //Fall through
			case Constants.IF_ICMPGE: //Fall through
			case Constants.IF_ICMPGT: //Fall through
			case Constants.IF_ICMPLE: //Fall through
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

			case Constants.IXOR: //Fall through
			case Constants.LXOR: //Fall through
				return OperationType.XOR;
			default:
				return OperationType.NONE;
		}
	}

	// Determine which operation to perform basd on instr's OperationType
	private Number calc(Instruction instr, ConstantPoolGen cpgen, Object a, Object b) 
	{
		OperationType t = getOpType(instr);
		switch (t) 
		{
			case ADD:
				return this.add(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case SUB:
				return this.sub(((ArithmeticInstruction)instr).getType(cpgen), b,a); // Substraction is performed in the reverse order of stack constants
			case MUL:
				return this.mul(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case DIV:
				return this.div(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case NEG:
				return this.neg(((ArithmeticInstruction)instr).getType(cpgen), a); // Negation only needs one operand (b would be null)
			case REM:
				return this.rem(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case SHL:
				return this.shl(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case SHR:
				return this.shr(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case USHR:
				return this.ushr(((ArithmeticInstruction)instr).getType(cpgen), a,b);
			case AND: // Fall through
			case OR: // Fall through
			case XOR: 
				return this.logic(t, a, b);
			case CMP:
				return this.cmp(instr, b, a);
			default:
				return null;
		}

	}

	// Used by conversion operations.
	// Creates an object of a specific type
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
		// We first check whether the instruction is of a specific type
		// and then cast it into that type in order to be able to invoke methods like getValue() etc
		if (instr instanceof LDC) 
		{			
			LDC ldc = (LDC) instr;
			try 
			{	
				// Get value from constant pool
				return (Number)ldc.getValue(cpgen);
			}
			catch (ClassCastException e)
			{
				System.out.println("Could not cast constant to Number. Skipping "+instr);
				return null;
			}			
		}
		else if (instr instanceof LDC2_W) 
		{
			LDC2_W ldc2w = (LDC2_W) instr;
			try 
			{	
				// Get value from constant pool
				return (Number)ldc2w.getValue(cpgen);
			}
			catch (ClassCastException e)
			{
				System.out.println("Could not cast constant to Number. Skipping "+instr);
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

	// Check whether instr is a comparison (cmp or IfInstruction)
	private boolean isCmpInstruction(Instruction instr)
	{
		return instr instanceof DCMPG 
			|| instr instanceof DCMPL 
			|| instr instanceof FCMPG 
			|| instr instanceof FCMPL  
			|| instr instanceof LCMP
			|| instr instanceof IfInstruction;
	}

	// This method cleans up the instruction list. It is possible that we have pushes and stores in the list
	// with no related load. We ghet rid of them by deleting the particular handles from instList
	private void cleanUpInstructionList(Map<Integer, ArrayList<InstructionHandle>> map, InstructionList instList, InstructionHandle newHandle)
	{
		// This list is needed to delete all unneeded instructions after the following loop.
		// We could delete them in the loop as well, but in that we would tamper the list
		// we are currently iterating over and that could lead to weird side effects.
		ArrayList<Integer> removeEntries = new ArrayList<Integer>();

		// Iterate over all entries in the instructionMap
		for (Map.Entry<Integer, ArrayList<InstructionHandle>> entry : map.entrySet())
		{
			// Get key (the reference to a variable)
			int key = entry.getKey().intValue();
			//System.out.println(entry);
			// This flag indicates whether an instruction is relevant (and therefore gets removed at the end)
			boolean remove = true;

			// Iterate over all instructions in instList and check whether there is a load for
			// the current Key. If not, prepare the entry (indluding the operations related to the current key) to be deleted
			Iterator<InstructionHandle> it = instList.iterator();
			while (it.hasNext()) 
			{
				// Get next handle
				InstructionHandle handle = (InstructionHandle) it.next();
				//System.out.println(handle+" ("+System.identityHashCode(handle)+")");

				// Get instruction from handle
				Instruction instr = handle.getInstruction();
				

				if (instr instanceof LoadInstruction)
				{
					// Instruction is a load instruction...now check the loaded reference
					LoadInstruction load = (LoadInstruction) instr;
					if (key == load.getIndex())
					{
						// Found a load instruction for that key...do not remove the current entry
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
			//System.out.println("Remove block #"+key+":");
			//System.out.println(map.get(key));

			// Iterate over all instructions of that entry
			for (InstructionHandle handle : map.get(key))
			{
				try
				{
					//System.out.println("\tDelete "+handle+" ("+System.identityHashCode(handle)+")");
					//System.out.println("\tDelete "+handle);
					instList.delete(handle);
				}
				catch (TargetLostException e)
				{
					//System.out.println("Clean Up: Target lost: "+handle);
					// Is thrown if one of the deleted handles is still referenced by 
					// a branch instruction.
					InstructionHandle[] targets = e.getTargets();

					for(int i=0; i < targets.length; i++) 
					{
						InstructionTargeter[] targeters = targets[i].getTargeters();
	     
			            if (newHandle != null)
			            {
			            	for(int j=0; j < targeters.length; j++)
			            	{
			            		//System.out.println("\tUpdate targeter "+targeters[j]+" with "+newHandle);
			            		targeters[j].updateTarget(targets[i], newHandle);
			            	}
			            }
			            else // no newInstr (should not happen)
			            {
			            	//System.out.println("\tRemove all targeters of "+targets[i]);
							targets[i].removeAllTargeters();
							//System.out.println("\tdone");
			            }
					}	
				}
			}

			// Finally remove key from map (not necessary but clean)
			map.remove(key);
		}
	}

	// This is the actual reduction step. We get here only if the method can be optimised.
	// It removes unnecessary instructions from intList
	private void performReduction(Deque<InstructionHandle> instructionStack, 
								  InstructionList instList, 
								  Deque<Integer> pushInstrIndexStack, 
								  int instrPointer,
								  InstructionHandle newHandle)
	{
		//System.out.println(pushInstrIndexStack);
		// Get initial push instruction for this block (the last instruction that will be removed)
		int lastPush = pushInstrIndexStack.pop().intValue();
		
		//System.out.println("Reduce first "+(instrPointer-lastPush)+" instructions of instruction stack from instruction list:");
		//System.out.println(instructionStack);

		// Count from count to instrPointer
		int count = lastPush;

		// Delete all instructions between lastPush and instrPointer
		while (count < instrPointer)
		{
			InstructionHandle h = instructionStack.pop();

			try
			{
				//System.out.println("\tDelete "+h);
				instList.delete(h);	
			}
			catch (TargetLostException e)
			{
				//System.out.println("Reduction: Target lost: "+h);
				// Is thrown if one of the deleted handles is still referenced by 
				// a branch instruction.
				InstructionHandle[] targets = e.getTargets();
				for(int i=0; i < targets.length; i++) 
				{
					InstructionTargeter[] targeters = targets[i].getTargeters();
		            if (newHandle != null)
		            {
		            	for(int j=0; j < targeters.length; j++)
		            	{
		            		//System.out.println("\tUpdate targeter "+targeters[j]+" with "+newHandle);
		            		targeters[j].updateTarget(targets[i], newHandle);
		            	}
		            }
		            else // no newInstr (should not happen)
		            {
						//System.out.println("\tRemove all targeters of "+targets[i]);
						targets[i].removeAllTargeters();
						//System.out.println("\tdone");
		            }
				}	
			}

			count++;
		} 
	}

	// Clear all datastructures.
	private void clearDataStructures(Deque<Number> constantStack, 
									 Deque<InstructionHandle> instructionStack, 
									 Deque<Integer> pushInstrIndexStack, 
									 Map<Integer, ArrayList<InstructionHandle>> instructionMap,
									 Map<Integer, Number> constantMap)
	{
		constantStack.clear();
		instructionStack.clear();
		pushInstrIndexStack.clear();
		instructionMap.clear();
		constantMap.clear();
	}

	private Instruction newPushInstruction(ConstantPoolGen cpgen, Number result, int type)
	{
		int index;
		switch(type) 
		{
			case Constants.T_INT: {
				int value = result.intValue();
				// Add result to constant pool
				if (value >= -1 && value <= 5) // -1 <= value <= 5, i.e. value is in [-1;5]
					return new ICONST(result.intValue());
				else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
					return new BIPUSH(result.byteValue());
				else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
					return new SIPUSH(result.shortValue());
				else
				{
					index = cpgen.addInteger(value);
					// New instruction will be an LDC
					return new LDC(index);
				}
			}
			case Constants.T_FLOAT: {
				float value = result.floatValue();
				if (value == 0.0f || value == 1.0f || value == 2.0f)
					return new FCONST(value);
				else
				{
					// Add result to constant pool
					index = cpgen.addFloat(value);
					// New instruction will be an LDC
					return new LDC(index);
				}
			}
			case Constants.T_LONG: {
				long value = result.longValue();
				if (value == 0L || value == 1L)
					return new LCONST(value);
				else 
				{
					// Add result to constant pool
					index = cpgen.addLong(value);			
					// New instruction will be an LDC2_W
					return new LDC2_W(index);
				}
			}
			case Constants.T_DOUBLE: {
				double value = result.doubleValue();
				// Add result to constant pool
				if (value == 1.0 || value == 0.0)
					return new DCONST(value);
				else
				{
					index = cpgen.addDouble(value);
					// New instruction will be an LDC2_W
					return new LDC2_W(index);
				}
			}
			default:
				return null;
		}

	}

	// Perform simple, constant and dynamic folding on method
	// It is being recursively invokes until method could not be further optimised
	private void performFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{
		// Get the Code of the method
		Code methodCode = method.getCode();

		// Get a list of all instructions in the method's code
		InstructionList instList = new InstructionList(methodCode.getCode());

		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), gen.getClassName(), instList, cpgen);
		
		// Define different datastructures:

		// Simulates the constant stack, i.e. always carries the most recent values
		Deque<Number> constantStack = new ArrayDeque<Number>();

		// Contains the actual processed instructions. In case of a store instruction, some instructions will be popped.
		// In the reduction step, the top most instructions will be deleted.
		// (how many they are depends on how many push operations the stack contains)
		Deque<InstructionHandle> instructionStack = new ArrayDeque<InstructionHandle>();

		// This data structure contains indices of all push instructions of the current optimisation step.
		// !The saved index is referring to the actual position inside the instruction stack!
		// In the reduction step, the elements from the top of the instruction stack will be deleted from the instruction list.
		// Deletion process stops at the index of the top element of the pushInstrIndexStack 
		Deque<Integer> pushInstrIndexStack = new ArrayDeque<Integer>();

		// In case of a store operation, some instructions will be removed from our instructionStack.
		// Since we might want to delete tham at a later point, we save these instructions in instructionMap.
		// It is defined as follows:    Integer (variable reference) ----->  ArrayList<InstructionHandle> (the list of instructions that are to be saved)
		Map<Integer, ArrayList<InstructionHandle>> instructionMap = new HashMap<Integer, ArrayList<InstructionHandle>>();

		// In case of a store operation, the constant's value is popped from the constantStack and stored in a local variable table.
		// constantMap simulates that table, so that we alwys have access to the most recent value of a variable
		Map<Integer, Number> constantMap = new HashMap<Integer, Number>();

		// This flag indicates whether an instruction is "interesting" (i.e. might be removed in case of an optimisation)
		boolean remove = false;

		// This flags indicates whether the optimization was successful, i.e. the method's code has changed
		boolean changed = false;
		

		// Counts the amount of instructions on instruction stack 
		// It is only increased if we push a new instruction on the stack.
		// However, in case of a store instruction, it will be decreased by the amount of instructions popped from stack
		int instrPointer = 0;
		
		ConstantPool cp = cpgen.getConstantPool();

		// Initialize new instruction here since it is being used after the loop
		InstructionHandle newHandle = null;

		// Iterate over all instructions in instList
		for (InstructionHandle handle : instList.getInstructionHandles()) 
		{
			//System.out.println("Current handle: "+handle);

			// Get the instruction the handle points to
			Instruction instr = handle.getInstruction();

			// Check whether instruction is a direct push operation
			Number constant = isPushInstruction(instr, cpgen);
			if (constant != null) 
			{
				// Since instr is a push operation, update pushInstrIndexStack with the current position
				pushInstrIndexStack.push(instrPointer++);
				// Add instr's handle to the instructionStack
				instructionStack.push(handle);
				// Since instr is a push operation, it pushes a new constant to the stack
				constantStack.push(constant);
				// Enable the checking of all following instructions by setting remove to true
				remove = true;
			}
			else if (remove)
			{
				if (instr instanceof LoadInstruction) 
				{
					// Instruction is an indirect push operation 
					// It loads a constant from constantMap and pushes it on the constant stack

					LoadInstruction load = (LoadInstruction) instr;
					int index = load.getIndex();
					Number value = constantMap.get(index);

					if (value != null)
					{
						// The loaded value is a number (i.e. numerical constant)

						// Since instr is a push operation, update pushInstrIndexStack with the current position
						pushInstrIndexStack.push(instrPointer++);
						// Add instr's handle to the instructionStack
						instructionStack.push(handle);
						// Since instr is a push operation, it pushes a new constant to the stack
						constantStack.push(value);
					}
					else
					{
						// value is not a number, so the current optimization step seems not
						// match any of our patterns. Reset all data structures and proceed.
						clearDataStructures(constantStack, instructionStack, pushInstrIndexStack, instructionMap, constantMap);
						remove = false;
					}
				}
				else if (instr instanceof ConversionInstruction) 
				{
					// instr is a conversion. We can perform the conversion manually.
					ConversionInstruction convInstr = (ConversionInstruction) instr;

					// Get last value from stack
					Number var = constantStack.pop();

					// Create an object with the appropriate type out of the just loaded value
					var = createObject(convInstr.getType(cpgen), var);

					if (var != null)
					{
						// Creation was successful...
						// Add instr's handle to the instructionStack
						instructionStack.push(handle);
						instrPointer++;				

						// Since instr is a push operation, it pushes a new constant to the stack
						constantStack.push(var);
					}
					else
					{
						// Something's messed up. Reset all data structures and proceed.
						clearDataStructures(constantStack, instructionStack, pushInstrIndexStack, instructionMap, constantMap);
						remove = false;
					}
				}
				else if (instr instanceof StoreInstruction) 
				{
					// instr is a store instruction. So store all related instructions on instructionMap and the constant in constantMap
					// (both the instructions and the constant have the same key in their respective maps).
					StoreInstruction store = (StoreInstruction) instr;

					// Add instr's handle to the instructionStack
					// It will be removes soon anyway, but this makes the following loop a bit prettier
					instructionStack.push(handle);

					// Remporary data structure that is being put / added to the instructionMap
					ArrayList<InstructionHandle> handleList = new ArrayList<InstructionHandle>();

					try {
						//System.out.print(pushInstrIndexStack);
						int lastPush = pushInstrIndexStack.pop().intValue();
						//System.out.println(" ---> "+pushInstrIndexStack);

						// Remove the necessary handles from stack and save them in instructionMap
						// Also decrease instrPointer (since the instructionStack is shrinking)
						while (lastPush <= instrPointer)
						{
							InstructionHandle h = instructionStack.pop();
							instrPointer--;
							//System.out.println("\t"+h);
							handleList.add(h);
						}
						
						// Get reference
						int index = store.getIndex();

						if (instructionMap.containsKey(index))
							// Reference does not yet exist...save the list ad ther reference's value
							instructionMap.get(index).addAll(handleList);
						else
							// Reference exists (--> Dynamic Folding necessary). Add temporary instruction list to existing one
							instructionMap.put(index, handleList);

						// Pop constant from constantStack and save it in constantMap (for later loads)
						Number value = constantStack.pop();

						// Put/update constant in constantMap
						constantMap.put(index, value);
					} catch (Exception e) {
						// Something wrong ==> abort optimisation before something bad happens
						//return;

						// Something's messed up. Reset all data structures and proceed.
						clearDataStructures(constantStack, instructionStack, pushInstrIndexStack, instructionMap, constantMap);
						remove = false;
					}
					//System.out.println("Store block in instruction map:");
				}
				else if (instr instanceof ArithmeticInstruction  && !constantStack.isEmpty()) // Perform calculation
				{
					// instr is an aithmetic instruction. Now the magic is gonna happen.

					ArithmeticInstruction arith = (ArithmeticInstruction) instr;

					//System.out.println(constantStack);

					// Get last two constants from constantStack
					Object a = constantStack.pop();
					
					Object b;
					if (constantStack.isEmpty()) // e.g. for an negation we only need one element on the stack
						b = new Object();
					else 
						b = constantStack.pop();

					// Perform calculation
					Number result = calc(arith, cpgen, a, b);

					// Push result to constantStack
					constantStack.push(result);
					
					// Now we can build up our new instruction and inject it into the existing method code
					Instruction newInstr = newPushInstruction(cpgen, result, arith.getType(cpgen).getType());

					if (newInstr != null)
					{
						// Add result to instruction list
						//System.out.println("Insert " + newInstr + " before " + handle);
						newHandle = instList.insert(handle, newInstr);

						if (getOpType(instr) != OperationType.NEG)
							// Remove index of last push operation (since it will be removed in the reduction step)
							// Only do that if instr is not a negation (since then the related push operation is the top one)
							pushInstrIndexStack.pop();

						// Add instr's handle to the instructionStack
						instructionStack.push(handle);
						instrPointer++;

						changed = true;

						// Only perform one folding at a time. Thus, we can terminate the loop at this point
						break;
					}
					else
					{
						// Something's messed up. Reset all data structures and proceed (i.e. pretend that nothing has happened ;-) )
						clearDataStructures(constantStack, instructionStack, pushInstrIndexStack, instructionMap, constantMap);
						remove = false;
					}

				}
				else if (isCmpInstruction(instr) && !constantStack.isEmpty())
				{
					// instr is a comparison operation. This could either be a simple cmparison that pushes a new value on the stack (cmp)
					// of an IfInstruction which performs a jump depending on the evaluation.

					// Get last two loaded constants from constantStack
					Object a = constantStack.pop();
					Object b;
					if (instr instanceof IFLE || instr instanceof IFLT || instr instanceof IFGE || instr instanceof IFGT || instr instanceof IFEQ || instr instanceof IFNE)
						// Variable a will be evaluated against 0
						b = new Integer(0);
					else if (!constantStack.isEmpty())
						b = constantStack.pop();
					else
					{
						// Something's wrong ==> abort optimisation before something bad happens
						//return;

						// Something's messed up. Reset all data structures and proceed with next handle.
						clearDataStructures(constantStack, instructionStack, pushInstrIndexStack, instructionMap, constantMap);
						remove = false;
						continue;
					}
					
					// Perform comparison
					Integer result = (Integer) calc(instr, cpgen, a, b);

					if (instr instanceof IfInstruction)
					{
						// instruction might perform a jump
						IfInstruction branch = (IfInstruction) instr;
						if (result == 1) // I result == 0: do not insert anything
						{
							// Replace current comparison by a direct GOTO instruction
							BranchInstruction newInstr = new GOTO(branch.getTarget());
							//System.out.println("Insert " + newInstr + " before " + handle);
							newHandle = instList.insert(handle, newInstr);
							System.out.println("New handle: "+newHandle);
						}
						else
						{
							System.out.println("Result = 0");
							newHandle = handle.getNext();
							System.out.println("New handle: "+newHandle);
						}
					}
					else // Just replace current handle with an iconst
					{
						// Just add the result to constantStack
						// iconst can push integer constant of range [-1;5] on the stack
						Instruction newInstr = new ICONST(result);
						//System.out.println("Insert " + newInstr + " before " + handle);
						newHandle = instList.insert(handle, newInstr);
						System.out.println("New handle: "+newHandle);
						constantStack.push(result);
					}

					// Remove index of last push operation (since it will be removed at the end)
					pushInstrIndexStack.pop();

					// Add instr's handle to the instructionStack
					instructionStack.push(handle);
					instrPointer++;

					changed = true;

					// Only perform one folding at a time. Break out of loop
					break;
				}
			}
		}

		
		if (changed)
		{
			// Optimisation is needed for that method.

			// Remove unnecessary instructions from instList.
			performReduction(instructionStack, instList, pushInstrIndexStack, instrPointer, newHandle);
			// Clean up what is left
			cleanUpInstructionList(instructionMap, instList, newHandle);
			
			// Give all instructions their position number (offset in byte stream), i.e., make the list ready to be dumped.
			try 
			{
				instList.setPositions(true);
			}
			catch(Exception e)
			{
				// If this fails, we might not want to overwrite our current method.
				// So just stop here and break out of function.
				System.out.println("Optimisation not successful");
				return;
			}

			// set max stack/local
			methodGen.setMaxStack();
			methodGen.setMaxLocals();

			// generate the new method
			Method newMethod = methodGen.getMethod();
			// replace the method in the original class
			gen.replaceMethod(method, newMethod);

			// Do the optimisation again on the newly generated method (newMethod)
			performFolding(gen, cpgen, newMethod);
		}
	}

	public void optimize()
	{
		//System.out.println("Optimize");
		ClassGen gen = new ClassGen(original);

		ConstantPoolGen cpgen = gen.getConstantPool();
		// get the current constant pool
		ConstantPool cp = cpgen.getConstantPool();

		Method[] methods = gen.getMethods();
		for (Method m : methods)
		{
			// Iterate over every method object
			System.out.println(">>>>>> Optimize method: "+cp.getConstant(m.getNameIndex()));
			performFolding(gen, cpgen, m);
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