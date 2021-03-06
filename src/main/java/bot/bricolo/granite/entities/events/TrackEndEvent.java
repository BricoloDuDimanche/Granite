package bot.bricolo.granite.entities.events;

import bot.bricolo.granite.andesite.Player;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

@SuppressWarnings("WeakerAccess")
public class TrackEndEvent extends PlayerEvent {

    private AudioTrack track;
    private AudioTrackEndReason reason;

    public TrackEndEvent(Player player, AudioTrack track, AudioTrackEndReason reason) {
        super(player);
        this.track = track;
        this.reason = reason;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public AudioTrackEndReason getReason() {
        return reason;
    }
}
