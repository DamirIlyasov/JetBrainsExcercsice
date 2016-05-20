import junit.framework.TestCase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Created by hp on 20.05.2016.
 */
public class Tests extends TestCase {


    public void testMaskTransform(){

        Main junit = new Main();            //Проверяем, правильно ли наш метод преобразовывает маску

        String mask = "*ForCopy*";
        mask = Main.maskTransform(mask);
        assertEquals(".*ForCopy.*",mask);

    }

    public void testSetParameters(){        // Проверяем метод setParameters. Верно- ли он задаёт переменные

        Main junit = new Main();
        String inputAll = " –input \"C:\\Users\\hp\\Desktop\\JBdir\" –output \"C:\\Users\\hp\\Desktop\\JBdirSecond\" –mask \"*ForCopy*\" –waitInterval 5000 –includeSubfolders true –autoDelete false";
        Main.setParameters(inputAll);

        assertEquals(Main.dirIn,"C:\\Users\\hp\\Desktop\\JBdir");
        assertEquals(Main.dirOut,"C:\\Users\\hp\\Desktop\\JBdirSecond\\");
        assertEquals(Main.mask,".*ForCopy.*");
        assertEquals(Main.waitInterval, 5000);
        assertEquals(Main.includeSubfolders, true);
        assertEquals(Main.autoDelete, false);
    }

}
