package bot.bricolo.granite.entities.events;

import bot.bricolo.granite.andesite.Player;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackStartEvent extends PlayerEvent {

    private AudioTrack track;

    public TrackStartEvent(Player player, AudioTrack track) {
        super(player);
        this.track = track;
    }

    public AudioTrack getTrack() {
        return track;
    }
}
