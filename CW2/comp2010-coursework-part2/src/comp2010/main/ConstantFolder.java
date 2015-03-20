package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
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
	
	enum OperationType {
		ADD, SUB, DIV, MUL, NEG, REM, AND, OR, SHL, SHR, USHR, XOR, NONE
	}
	
	private OperationType getType(Instruction instr) {
		switch (instr.getOpcode()) {
			case Constants.IADD: //Fall through
			case Constants.LADD: //Fall through
			case Constants.FADD: //Fall through
			case Constants.DADD: 
				return OperationType.ADD;
			case Constants.IXOR:
			case Constants.LXOR:
				return OperationType.XOR;
			default:
				break;
		};
		return OperationType.NONE;
	}
	
	private void simpleFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) {
		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();
		//System.out.println(methodCode.toString());
		InstructionList instList = new InstructionList(methodCode.getCode());

		// Initialise a method generator with the original method as the baseline	
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), gen.getClassName(), instList, cpgen);
		
		// get the current constant pool
		ConstantPool cp = cpgen.getConstantPool();
		// get the constants in the pool
		
		Constant[] constants = cp.getConstantPool();
		Deque<Object> stack = new ArrayDeque<Object>();
		for (InstructionHandle handle : instList.getInstructionHandles()) 
		{
			Instruction instr = handle.getInstruction();

			if (instr instanceof LDC) {
				LDC ldc = (LDC) instr;
				stack.addFirst(ldc.getValue(cpgen));
				System.out.println(ldc.getOpcode());
			}
			if (instr instanceof ArithmeticInstruction) {
				
				System.out.println(instr.toString());
				ArithmeticInstruction arith = (ArithmeticInstruction) instr;
				System.out.println(arith.getType(cpgen));
				System.out.println(getType(arith));
				System.out.println(arith.produceStack(cpgen));
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
	}

	private void constantFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) {

	}

	private void dynamicFolding(ClassGen gen, ConstantPoolGen cpgen, Method method) {

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




		for (int i = 0; i < constants.length; i++)
		{
			// string constants take two entries in the pool
			// the first one is of ConstantString, which contains
			// an index to the second entry, which is ConstantUtf8
			// (displayed Asciz when disassembled by javap)
			//
			// ConstantUtf8 (Asciz) entries are used to store method names, etc
			// whereas we are only interested in String constants
			// So we first look for ConstantString entry, 
			// then retrieve the index of ConstantUtf8 entry, which we then replace
			

			if (constants[i] instanceof ConstantString)
			{
				ConstantString cs = (ConstantString) constants[i];
				cp.setConstant(cs.getStringIndex(), 
								new ConstantUtf8("Bwahaha!"));
			}
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