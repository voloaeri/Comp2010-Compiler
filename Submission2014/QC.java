
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tom
 */
public class QC {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        FileInputStream input = null;
        try {
            input = new FileInputStream(args[0]);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QC.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Lexer qLex = new Lexer(input);
        parser qParser = new parser(qLex);
        qParser.setLexer(qLex);
        try {
            qParser.parse();
        } catch (Exception ex) {
            Logger.getLogger(QC.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
