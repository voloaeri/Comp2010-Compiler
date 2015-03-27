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

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LDC_W;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.classfile.Method;
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
	

	enum OperationType 
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

	private Integer cmp(Instruction instr, Object a, Object b)
	{
		if (instr instanceof FCMPG)
			return new Integer((Float)a > (Float)b ? 1 : 0);
		else if (instr instanceof DCMPG)
			return new Integer((Double)a > (Double)b ? 1 : 0);
		else if (instr instanceof FCMPL)
			return new Integer((Float)a < (Float)b ? 1 : 0);
		else if (instr instanceof DCMPL)
			return new Integer((Double)a < (Double)b ? 1 : 0);
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
				return this.sub(((ArithmeticInstruction)instr).getType(cpgen), a,b);
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

	private Object createObject(Type t, Object var) {
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

	private void simpleFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{
		boolean changed = false;
		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();
		//System.out.println(methodCode);
		InstructionList instList = new InstructionList(methodCode.getCode());

		// Initialise a method generator with the original method as the baseline	
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), gen.getClassName(), instList, cpgen);
		
		// get the current constant pool
		ConstantPool cp = cpgen.getConstantPool();
		// get the constants in the pool
		
		Deque<Object> constantStack = new ArrayDeque<Object>();
		ArrayList<InstructionHandle> removeHandles = new ArrayList<InstructionHandle>();
		boolean remove = false;
		for (InstructionHandle handle : instList.getInstructionHandles()) 
		{
			//System.out.println("Current handle: "+handle);
			Instruction instr = handle.getInstruction();

			
			if (!changed && instr instanceof LDC) 
			{
				LDC ldc = (LDC) instr;
				remove = true; // start adding all following instructions to remove list
				
				constantStack.addFirst(ldc.getValue(cpgen));
			}
			else if (!changed && instr instanceof LDC2_W) 
			{
				LDC2_W ldc2w = (LDC2_W) instr;
				remove = true; // start adding all following instructions to remove list
				//removeHandles.add(handle);
				constantStack.addFirst(ldc2w.getValue(cpgen));
			}
			else if (remove && instr instanceof ConversionInstruction) 
			{
				//removeHandles.add(handle);
				//System.out.println("Add to remove list: "+handle);
				Object var = constantStack.pop();
				ConversionInstruction convInstr = (ConversionInstruction) instr;
				var = createObject(convInstr.getType(cpgen), var);
				constantStack.addFirst(var);
			}

			else if (remove && instr instanceof ArithmeticInstruction) 
			{
				removeHandles.add(handle);
				remove = false; // Found an operation ==> stop removing
				//removeHandles.add(handle);
				ArithmeticInstruction arith = (ArithmeticInstruction) instr;

				// Get last two loaded constants from constantStack
				Object a = constantStack.pop();
				

				Object b;
				if (constantStack.isEmpty())
					b = new Object();
				else
					b = constantStack.pop();
				
				// Perform calculation
				Number result = calc(arith, cpgen, a, b);
				
				int index = -1;
				Instruction newInstr;
				switch(arith.getType(cpgen).getType()) 
				{
					case Constants.T_INT:
						index = cpgen.addInteger(((Integer)result).intValue());
						newInstr = new LDC(index);
						break;
					case Constants.T_LONG:
						index = cpgen.addLong(((Long)result).longValue());			
						newInstr = new LDC2_W(index);
						break;
					case Constants.T_FLOAT:
						index = cpgen.addFloat(((Float)result).floatValue());
						newInstr = new LDC(index);
						break;
					case Constants.T_DOUBLE:
						index = cpgen.addDouble(((Double)result).doubleValue());
						newInstr = new LDC_W(index);
						break;
					default:
						newInstr = null;
				}

				// Add result to instruction list
				System.out.println("Insert " + newInstr + " before " + handle);
				instList.insert(handle, newInstr);
				
				changed = true;

				//System.out.println(result);

				//System.out.println(arith.getType(cpgen));
				//System.out.println(getType(arith));
			}
			else 
			{
				if (remove) 
				{
					//removeHandles.add(handle);
					if (instr instanceof DCMPG || instr instanceof DCMPL || instr instanceof FCMPG || instr instanceof FCMPL  || instr instanceof LCMP)
					{
						removeHandles.add(handle);
						remove = false;
						// Get last two loaded constants from constantStack
						Object a = constantStack.pop();
						Object b = constantStack.pop();

						Integer result = (Integer) calc(instr, cpgen, a, b);
						instList.insert(handle, new ICONST(result));

						changed = true;
					}
				}
			}

			if (remove)
			{
				//System.out.println("Add "+handle+" to remove list");
				removeHandles.add(handle);
			}

		}

		// Remove unused instructions
		if (changed)
		{
			for (InstructionHandle h: removeHandles) 
			{
				try
				{
					System.out.println("Delete "+h);
					instList.delete(h);
				}
				catch (TargetLostException e)
				{
					e.printStackTrace();
				}
			}
			// setPositions(true) checks whether jump handles 
			// are all within the current method
			instList.setPositions(true);

			// set max stack/local
			methodGen.setMaxStack();
			methodGen.setMaxLocals();

			// generate the new method with replaced iconst
			Method newMethod = methodGen.getMethod();
			// replace the method in the original class
			gen.replaceMethod(method, newMethod);
			//System.out.println(newMethod.getCode());


			//System.out.println("Simple folding run done");

			// If anything has changed, run the whole stuff again until nothing changes any more
			simpleFolding(gen, cpgen, newMethod);
		}
		
		//System.out.println("Simple folding done");

	}

	private void constantFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{
		boolean changed = false;
		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();
		//System.out.println(methodCode);
		InstructionList instList = new InstructionList(methodCode.getCode());

		// Initialise a method generator with the original method as the baseline	
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), gen.getClassName(), instList, cpgen);
		
		// get the current constant pool
		ConstantPool cp = cpgen.getConstantPool();
		// get the constants in the pool
		
		Deque<Object> constantStack = new ArrayDeque<Object>();
		Deque<InstructionHandle> instructionStack = new ArrayDeque<InstructionHandle>();
		Map<Integer, ArrayList<InstructionHandle>> constantMap = new HashMap<Integer, ArrayList<InstructionHandle>>();
		ArrayList<InstructionHandle> removeHandles = new ArrayList<InstructionHandle>();

		boolean remove = false;
		for (InstructionHandle handle : instList.getInstructionHandles()) 
		{
			//System.out.println("Current handle: "+handle);
			Instruction instr = handle.getInstruction();

			if (!changed && instr instanceof LDC) 
			{
				LDC ldc = (LDC) instr;
				remove = true; // start adding all following instructions to remove list
				
				constantStack.addFirst(ldc.getValue(cpgen));
				instructionStack.addFirst(handle);
			}
			else if (!changed && instr instanceof LDC2_W) 
			{
				LDC2_W ldc2w = (LDC2_W) instr;
				remove = true; // start adding all following instructions to remove list
				//removeHandles.add(handle);
				constantStack.addFirst(ldc2w.getValue(cpgen));
				instructionStack.addFirst(handle);
			}

			else if (remove && instr instanceof ConversionInstruction) 
			{
				//removeHandles.add(handle);
				//System.out.println("Add to remove list: "+handle);
				Object var = constantStack.pop();
				ConversionInstruction convInstr = (ConversionInstruction) instr;
				var = createObject(convInstr.getType(cpgen), var);
				constantStack.addFirst(var);
				instructionStack.addFirst(handle);
			}

			else if (remove && instr instanceof ArithmeticInstruction) 
			{
				removeHandles.add(handle);
				remove = false; // Found an operation ==> stop removing
				//removeHandles.add(handle);
				ArithmeticInstruction arith = (ArithmeticInstruction) instr;

				// Get last two loaded constants from constantStack
				Object a = constantStack.pop();
				

				Object b;
				if (constantStack.isEmpty())
					b = new Object();
				else
					b = constantStack.pop();
				
				// Perform calculation
				Number result = calc(arith, cpgen, a, b);
				
				int index = -1;
				Instruction newInstr;
				switch(arith.getType(cpgen).getType()) 
				{
					case Constants.T_INT:
						index = cpgen.addInteger(((Integer)result).intValue());
						newInstr = new LDC(index);
						break;
					case Constants.T_LONG:
						index = cpgen.addLong(((Long)result).longValue());			
						newInstr = new LDC2_W(index);
						break;
					case Constants.T_FLOAT:
						index = cpgen.addFloat(((Float)result).floatValue());
						newInstr = new LDC(index);
						break;
					case Constants.T_DOUBLE:
						index = cpgen.addDouble(((Double)result).doubleValue());
						newInstr = new LDC_W(index);
						break;
					default:
						newInstr = null;
				}

				// Add result to instruction list
				System.out.println("Insert " + newInstr + " before " + handle);
				instList.insert(handle, newInstr);
		
				changed = true;

				//System.out.println(result);

				//System.out.println(arith.getType(cpgen));
				//System.out.println(getType(arith));
			}

			else if (remove && instr instanceof StoreInstruction) 
			{
				StoreInstruction store = (StoreInstruction) instr;
				ArrayList<InstructionHandle> handleList = new ArrayList<InstructionHandle>();
				handleList.add(handle);
				try
				{
					InstructionHandle h;

					while (true)
					{
						handleList.add(instructionStack.pop());
					}
				}

				catch(Exception e)
				{

				}

				finally
				{
					constantMap.put(store.getIndex(), handleList);
				}
			}
			else 
			{
				if (remove) 
				{
					//removeHandles.add(handle);
					if (instr instanceof DCMPG || instr instanceof DCMPL || instr instanceof FCMPG || instr instanceof FCMPL  || instr instanceof LCMP)
					{
						removeHandles.add(handle);
						remove = false;
						// Get last two loaded constants from constantStack
						Object a = constantStack.pop();
						Object b = constantStack.pop();

						Integer result = (Integer) calc(instr, cpgen, a, b);
						instList.insert(handle, new ICONST(result));

						changed = true;
					}
				}
			}

			if (remove)
			{
				//System.out.println("Add "+handle+" to remove list");
				removeHandles.add(handle);
			}

		}

		// Remove unused instructions
		if (changed)
		{
			for (InstructionHandle h: removeHandles) 
			{
				try
				{
					System.out.println("Delete "+h);
					instList.delete(h);
				}
				catch (TargetLostException e)
				{
					e.printStackTrace();
				}
			}
		}
		

		// setPositions(true) checks whether jump handles 
		// are all within the current method
		instList.setPositions(true);

		// set max stack/local
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// generate the new method with replaced iconst
		Method newMethod = methodGen.getMethod();
		// replace the method in the original class
		gen.replaceMethod(method, newMethod);
		//System.out.println(newMethod.getCode());


		//System.out.println("Simple folding run done");

		//if (changed)
			// If anything has changed, run the whole stuff again until nothing changes any more
			//constantFolding(gen, cpgen, newMethod);

		
		//System.out.println("Simple folding done");

	}


	private void dynamicFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{

	}

	private void optimizeMethod(ClassGen gen, ConstantPoolGen cpgen, Method method)
	{
		simpleFolding(gen, cpgen, method);
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
			optimizeMethod(gen, cpgen, m);
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