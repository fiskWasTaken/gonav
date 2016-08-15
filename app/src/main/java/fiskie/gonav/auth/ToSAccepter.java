package fiskie.gonav.auth;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.main.ServerRequest;

import POGOProtos.Enums.TutorialStateOuterClass;
import POGOProtos.Networking.Requests.Messages.MarkTutorialCompleteMessageOuterClass;
import POGOProtos.Networking.Requests.RequestTypeOuterClass;

public class ToSAccepter {
    private PokemonGo pokemonGo;

    public ToSAccepter(PokemonGo pokemonGo) {
        this.pokemonGo = pokemonGo;
    }

    public void accept() throws LoginFailedException, RemoteServerException {
        MarkTutorialCompleteMessageOuterClass.MarkTutorialCompleteMessage.Builder tosBuilder = MarkTutorialCompleteMessageOuterClass
                .MarkTutorialCompleteMessage.newBuilder();
        tosBuilder.addTutorialsCompleted(TutorialStateOuterClass.TutorialState.LEGAL_SCREEN)
                .setSendMarketingEmails(false)
                .setSendPushNotifications(false);
        ServerRequest serverRequest = new ServerRequest(RequestTypeOuterClass.RequestType.MARK_TUTORIAL_COMPLETE, tosBuilder.build());
        pokemonGo.getRequestHandler().sendServerRequests(serverRequest);
    }
}
