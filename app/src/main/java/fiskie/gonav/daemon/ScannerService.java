package fiskie.gonav.daemon;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import fiskie.gonav.scanner.Scanner;

/**
 * Created by fiskie on 27/07/2016.
 */
public class ScannerService extends Handler {
    Scanner scanner;
    boolean running;

    public ScannerService(Looper looper, Scanner scanner) {
        super(looper);
        this.scanner = scanner;
    }

    @Override
    public void handleMessage(Message msg) {
        // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
        // this method will be called.

        // Add your cpu-blocking activity here

        running = msg.arg1 == 1;
        this.loop();
    }

    void loop() {
        final ScannerService self = this;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    scanner.scan();
                    self.loop();
                }
            }
        }, 2000);
    }
}
