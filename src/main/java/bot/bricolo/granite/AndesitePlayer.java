package bot.bricolo.granite;

import bot.bricolo.granite.entities.events.*;
import bot.bricolo.granite.entities.Track;
import bot.bricolo.granite.entities.payload.Pause;
import bot.bricolo.granite.entities.payload.Play;
import bot.bricolo.granite.entities.payload.Stop;
import bot.bricolo.granite.entities.payload.VoiceServerUpdate;
import bot.bricolo.granite.exceptions.AudioTrackEncodingException;
import bot.bricolo.granite.exceptions.NoNodeAvailableException;
import bot.bricolo.granite.exceptions.RemoteTrackException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AndesitePlayer {
    private final Granite granite;
    private final String guildId;
    private AndesiteNode node;
    private List<IEventListener> listeners = new ArrayList<>();

    AndesitePlayer(Granite granite, String guildId) {
        this.granite = granite;
        this.guildId = guildId;
        this.assignNode();
    }

    //******************//
    // Player functions //
    //******************//
    public void play(Track track) throws NoNodeAvailableException {
        play(track.getTrack());
    }

    public void play(AudioTrack track) throws NoNodeAvailableException, AudioTrackEncodingException {
        play(Utils.toMessage(track));
    }

    private void play(String track) throws NoNodeAvailableException {
        if (node == null || !node.isOpen()) {
            throw new NoNodeAvailableException();
        }

        node.send(new Play(guildId, track, true));
    }

    public void pause(Boolean pause) throws NoNodeAvailableException {
        if (node == null || !node.isOpen()) {
            throw new NoNodeAvailableException();
        }

        node.send(new Pause(pause));
    }

    public void stop() throws NoNodeAvailableException {
        if (node == null || !node.isOpen()) {
            throw new NoNodeAvailableException();
        }

        node.send(new Stop());
    }

    //****************//
    // Event handling //
    //****************//
    public void addEventListener(EventAdapter listener) {
        listeners.add(listener);
    }

    void handleEvent(JSONObject payload) throws AudioTrackEncodingException {
        PlayerEvent event = null;

        switch (payload.getString("type")) {
            case "TrackEndEvent":
                event = new TrackEndEvent(this,
                        Utils.toAudioTrack(payload.getString("track")),
                        AudioTrackEndReason.valueOf(payload.getString("reason"))
                );
                break;
            case "TrackExceptionEvent":
                event = new TrackExceptionEvent(this,
                        Utils.toAudioTrack(payload.getString("track")),
                        new RemoteTrackException(payload.getString("error"))
                );
                break;
            case "TrackStuckEvent":
                event = new TrackStuckEvent(this,
                        Utils.toAudioTrack(payload.getString("track")),
                        payload.getLong("thresholdMs")
                );
                break;
            case "WebSocketClosedEvent":
                break;
            default:
                granite.LOG.warn("Unexpected event type: " + payload.getString("type"));
                break;
        }

        PlayerEvent finalEvent = event;
        listeners.forEach(listener -> listener.onEvent(finalEvent));
    }

    //****************//
    // Voice handling //
    //****************//
    void onVoiceServerUpdate(@Nonnull VoiceDispatchInterceptor.VoiceServerUpdate update) {
        node.send(new VoiceServerUpdate(update));
    }

    //***********//
    // Internals //
    //***********//
    private void assignNode() {
        List<AndesiteNode> nodes = granite.getAvailableNodes();
        if (nodes.size() == 0) {
            granite.LOG.warn("No Andesite node available right now (Guild " + guildId + "). Trying to assign a node again in 5 seconds");
            Utils.setTimeout(this::assignNode, 5000);
            return;
        }
        int index = ThreadLocalRandom.current().nextInt(nodes.size());
        this.node = nodes.get(index);
    }
}
