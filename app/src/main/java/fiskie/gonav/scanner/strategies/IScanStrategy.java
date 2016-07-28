package fiskie.gonav.scanner.strategies;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import fiskie.gonav.scanner.EncounterCallback;

/**
 * a ScanStrategy implementer is responsible for fetching scan data.
 */
public interface IScanStrategy {
    void doScan(EncounterCallback callback) throws LoginFailedException, RemoteServerException, InterruptedException;
}
