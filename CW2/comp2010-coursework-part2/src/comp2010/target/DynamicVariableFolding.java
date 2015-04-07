package comp2010.target;

public class DynamicVariableFolding {
    public int methodOne() {
        int a = 42;
        int b = (a + 764) * 3;
        a = 22;
        return b + 1234 - a;
    }

    public boolean methodTwo() {
        // int x = 12345;
        // int y = 54321;
        // System.out.println(x < y);
        System.out.println(true);
        // y = 0;
        // return x > y;
        return true;
    }

    public int methodThree() {
        int i = 0;
        int j = i + 3; //j = 3
        i = j + 4; //i = 7
        j = i + 6; //j = 13

        while (i < 10) //runs three times
        {
           System.out.println(i+1);
           System.out.println(3+3);
           break;
        }

        //i = 10
        //j = 19
        

        //int z = j + -i;
        //z = -91

        return j-i; // return 6
    }

    // public int methodFour() {
    //     return 1;
    // }

    public int methodFour(int a) throws Exception {
        float b=3.4f;
        try
        {
            a = a+(int)b;
            if (a<(int)b)
                throw new NumberFormatException("Something went wrong");
            else if (a>(int)b)
                throw new Exception();
        } 
        catch(NumberFormatException e)
        {
            System.out.println(e.getLocalizedMessage());
        }

        return a;
    }
}