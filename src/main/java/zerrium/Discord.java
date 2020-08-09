package zerrium;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.ChannelAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.message.*;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.listener.user.UserStartTypingListener;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Discord {
    public static ArrayList<ZPlayer> zplayer;
    public static long world_size = 0L;
    public static long nether_size = 0L;
    public static long end_size = 0L;
    public static long total_size = 0L;
    private static TextChannel tc;

    public Discord(){
        zplayer = new ArrayList<>();
    }

    public void startDiscord() {
        String botToken = SpigotEvent.fc.getString("bot_token");//"NzA3NTM3NDQ3NTMzMjgxMzUy.XrKPmA.HVzarNFEC3Dx5LOe8PMGk-Zcb3o";
        long channelID = SpigotEvent.fc.getLong("channel_id");//"704643254469001257";
        DiscordApi api = new DiscordApiBuilder().setToken(botToken).login().join();
        api.updateActivity("Reading minecraft statistics");
        api.getChannelById(channelID);

        tc = new TextChannel() {
            @Override
            public CompletableFuture<Void> type() {
                return null;
            }

            @Override
            public CompletableFuture<Void> bulkDelete(long... messageIds) {
                return null;
            }

            @Override
            public CompletableFuture<Message> getMessageById(long id) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getPins() {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessages(int limit) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesUntil(Predicate<Message> condition) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesWhile(Predicate<Message> condition) {
                return null;
            }

            @Override
            public Stream<Message> getMessagesAsStream() {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesBefore(int limit, long before) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition, long before) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition, long before) {
                return null;
            }

            @Override
            public Stream<Message> getMessagesBeforeAsStream(long before) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesAfter(int limit, long after) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition, long after) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition, long after) {
                return null;
            }

            @Override
            public Stream<Message> getMessagesAfterAsStream(long after) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesAround(int limit, long around) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition, long around) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition, long around) {
                return null;
            }

            @Override
            public Stream<Message> getMessagesAroundAsStream(long around) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesBetween(long from, long to) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesBetweenUntil(Predicate<Message> condition, long from, long to) {
                return null;
            }

            @Override
            public CompletableFuture<MessageSet> getMessagesBetweenWhile(Predicate<Message> condition, long from, long to) {
                return null;
            }

            @Override
            public Stream<Message> getMessagesBetweenAsStream(long from, long to) {
                return null;
            }

            @Override
            public MessageCache getMessageCache() {
                return null;
            }

            @Override
            public CompletableFuture<List<Webhook>> getWebhooks() {
                return null;
            }

            @Override
            public ChannelType getType() {
                return ChannelType.SERVER_TEXT_CHANNEL;
            }

            @Override
            public DiscordApi getApi() {
                return api;
            }

            @Override
            public long getId() {
                return channelID;
            }

            @Override
            public ListenerManager<UserStartTypingListener> addUserStartTypingListener(UserStartTypingListener listener) {
                return null;
            }

            @Override
            public List<UserStartTypingListener> getUserStartTypingListeners() {
                return null;
            }

            @Override
            public ListenerManager<ReactionRemoveAllListener> addReactionRemoveAllListener(ReactionRemoveAllListener listener) {
                return null;
            }

            @Override
            public List<ReactionRemoveAllListener> getReactionRemoveAllListeners() {
                return null;
            }

            @Override
            public ListenerManager<ReactionAddListener> addReactionAddListener(ReactionAddListener listener) {
                return null;
            }

            @Override
            public List<ReactionAddListener> getReactionAddListeners() {
                return null;
            }

            @Override
            public ListenerManager<ReactionRemoveListener> addReactionRemoveListener(ReactionRemoveListener listener) {
                return null;
            }

            @Override
            public List<ReactionRemoveListener> getReactionRemoveListeners() {
                return null;
            }

            @Override
            public ListenerManager<MessageEditListener> addMessageEditListener(MessageEditListener listener) {
                return null;
            }

            @Override
            public List<MessageEditListener> getMessageEditListeners() {
                return null;
            }

            @Override
            public ListenerManager<CachedMessageUnpinListener> addCachedMessageUnpinListener(CachedMessageUnpinListener listener) {
                return null;
            }

            @Override
            public List<CachedMessageUnpinListener> getCachedMessageUnpinListeners() {
                return null;
            }

            @Override
            public ListenerManager<ChannelPinsUpdateListener> addChannelPinsUpdateListener(ChannelPinsUpdateListener listener) {
                return null;
            }

            @Override
            public List<ChannelPinsUpdateListener> getChannelPinsUpdateListeners() {
                return null;
            }

            @Override
            public ListenerManager<MessageCreateListener> addMessageCreateListener(MessageCreateListener listener) {
                return null;
            }

            @Override
            public List<MessageCreateListener> getMessageCreateListeners() {
                return null;
            }

            @Override
            public ListenerManager<MessageDeleteListener> addMessageDeleteListener(MessageDeleteListener listener) {
                return null;
            }

            @Override
            public List<MessageDeleteListener> getMessageDeleteListeners() {
                return null;
            }

            @Override
            public ListenerManager<CachedMessagePinListener> addCachedMessagePinListener(CachedMessagePinListener listener) {
                return null;
            }

            @Override
            public List<CachedMessagePinListener> getCachedMessagePinListeners() {
                return null;
            }

            @Override
            public <T extends TextChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<? extends TextChannelAttachableListener>> addTextChannelAttachableListener(T listener) {
                return null;
            }

            @Override
            public <T extends TextChannelAttachableListener & ObjectAttachableListener> void removeTextChannelAttachableListener(T listener) {

            }

            @Override
            public <T extends TextChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getTextChannelAttachableListeners() {
                return null;
            }

            @Override
            public <T extends TextChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {

            }

            @Override
            public <T extends ChannelAttachableListener & ObjectAttachableListener> Collection<ListenerManager<T>> addChannelAttachableListener(T listener) {
                return null;
            }

            @Override
            public <T extends ChannelAttachableListener & ObjectAttachableListener> void removeChannelAttachableListener(T listener) {

            }

            @Override
            public <T extends ChannelAttachableListener & ObjectAttachableListener> Map<T, List<Class<T>>> getChannelAttachableListeners() {
                return null;
            }

            @Override
            public <T extends ChannelAttachableListener & ObjectAttachableListener> void removeListener(Class<T> listenerClass, T listener) {

            }
        };
        new MessageBuilder()
                .append("Look at these ")
                .setEmbed(new EmbedBuilder()
                        .setTitle("WOW")
                        .setDescription("Really cool pictures!")
                        .setColor(Color.ORANGE))
                .send(tc);

        /* Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });
        */
    }
}
