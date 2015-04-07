package comp2010.target;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class DynamicVariableFoldingTest
{
    DynamicVariableFolding dvf = new DynamicVariableFolding();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams()
    {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void cleanUpStreams()
    {
        System.setOut(null);
    }

    @Test
    public void testMethodOne()
    {
        assertEquals(3630, dvf.methodOne());
    }

    @Test
    public void testMethodTwoOut()
    {
        dvf.methodTwo();
        assertEquals("true\n", outContent.toString());
    }

    @Test
    public void testMethodTwoReturn()
    {
        assertEquals(true, dvf.methodTwo());
    }

    @Test
    public void testMethodThree()
    {
        assertEquals(6, dvf.methodThree());
    }

    @Test
    public void testMethodThreeOut()
    {
        dvf.methodThree();
        assertEquals("8\n6\n", outContent.toString());
    }

    @Test
    public void testMethodFour()  throws Exception
    {
        assertEquals(3, dvf.methodFour(0));
    }

    @Test(expected=Exception.class)
    public void testMethodFourErr()  throws Exception
    {
        dvf.methodFour(4);
    }

    @Test
    public void testMethodFourOut()  throws Exception
    {
        dvf.methodFour(-3);
        assertEquals("Something went wrong\n", outContent.toString());
    }


}