package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Arrays;

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
	
	private Object add(Type t, Object a, Object b) 
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

	private Object sub(Type t, Object a, Object b) 
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
	private Object mul(Type t, Object a, Object b) 
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
	private Object div(Type t, Object a, Object b) 
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

	private Object neg(Type t, Object a) 
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

	private Object rem(Type t, Object a, Object b) 
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

	private Object shl(Type t, Object a, Object b) 
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

	private Object shr(Type t, Object a, Object b) 
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

	private Object ushr(Type t, Object a, Object b) 
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

	private Object logic(OperationType t, Object a, Object b)
	{
		switch(t) {
			case AND:
				return new Boolean((Boolean)a && (Boolean)b);
			case OR:
				return new Boolean((Boolean)a || (Boolean)b);
			case XOR:
				return new Boolean((Boolean)a ^ (Boolean)b);
			default:
				return null;
		}
	}

	private Object cmp(Instruction instr, Object a, Object b)
	{
		if (instr instanceof FCMPG)
			return new Boolean((Float)a > (Float)b);
		else if (instr instanceof DCMPG)
			return new Boolean((Double)a > (Double)b);
		else if (instr instanceof FCMPL)
			return new Boolean((Float)a < (Float)b);
		else if (instr instanceof DCMPL)
			return new Boolean((Double)a < (Double)b);
		else if (instr instanceof LCMP)
			return new Integer(Long.compare((Long)a, (Long)b));
		else
			return null;
	}

	private Object calc(Instruction instr, ConstantPoolGen cpgen, Object a, Object b) 
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

			if (instr instanceof LDC) 
			{
				LDC ldc = (LDC) instr;
				remove = true; // start adding all following instructions to remove list
				removeHandles.add(handle);
				constantStack.addFirst(ldc.getValue(cpgen));
			}
			else if (instr instanceof LDC2_W) 
			{
				LDC2_W ldc2w = (LDC2_W) instr;
				remove = true; // start adding all following instructions to remove list
				removeHandles.add(handle);
				//System.out.println("Add to remove list: "+handle);

				constantStack.addFirst(ldc2w.getValue(cpgen));
			}
			else if (remove && instr instanceof ConversionInstruction) 
			{
				removeHandles.add(handle);
				//System.out.println("Add to remove list: "+handle);
				Object var = constantStack.pop();
				ConversionInstruction convInstr = (ConversionInstruction) instr;
				var = createObject(convInstr.getType(cpgen), var);
				constantStack.addFirst(var);
			}

			else if (remove && instr instanceof ArithmeticInstruction) 
			{
				remove = false; // Found an operation ==> stop removing

				ArithmeticInstruction arith = (ArithmeticInstruction) instr;

				// Get last two loaded constants from constantStack
				Object a = constantStack.pop();
				Object b = constantStack.pop();
				
				// Perform calculation
				Object result = calc(arith, cpgen, a, b);
				
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
				System.out.println("Replace "+handle+ " by "+newInstr);
				instList.insert(handle, newInstr);
				
				//System.out.println(result);

				//System.out.println(arith.getType(cpgen));
				//System.out.println(getType(arith));
			}
			else 
			{
				if (remove) 
				{
					if (instr instanceof DCMPG || instr instanceof DCMPL || instr instanceof FCMPG || instr instanceof FCMPL  || instr instanceof LCMP)
					{
						// Get last two loaded constants from constantStack
						Object a = constantStack.pop();
						Object b = constantStack.pop();

						// Perform calculation
						if (instr instanceof LCMP)
						{
							Integer result = (Integer) calc(instr, cpgen, a, b);
							instList.insert(handle, new ICONST(result));
						}
						else
						{
							Boolean result = (Boolean) calc(instr, cpgen, a, b);
							instList.insert(handle, new ICONST(result?1:0));
						}
					}
					//System.out.println("Add to remove list: "+handle);
					removeHandles.add(handle);
				}
			}
		}
		// Remove unused instructions
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


	}

	private void constantFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) 
	{

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

		// Do your optimization here
		Method[] methods = gen.getMethods();
		for (Method m : methods)
		{
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