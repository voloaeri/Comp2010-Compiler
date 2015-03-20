package comp2010.target;

public class ConstantVariableFolding
{
    double j = 32889.5-31780.2;
    public int methodOne(){
        int a = 42;
        int b = (a + 764) * 3;
        return b + 1234 - a;
    }

    public double methodTwo(){
        double i = 0.6;
        
        return i + j;
    }

    public boolean methodThree(){
        int x = 32767;
        int y = 32768;
        return x % y == 1;
    }
}