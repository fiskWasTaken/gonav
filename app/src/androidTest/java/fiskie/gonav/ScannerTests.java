package fiskie.gonav;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import fiskie.gonav.scanner.Scanner;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ScannerTests {
    @Test
    public void testSetup() {
        Scanner scanner = new Scanner();
        scanner.scan();
    }
}
