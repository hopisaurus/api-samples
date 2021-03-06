package com.mindlinksoft.foundationapi.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mindlinksoft.foundationapi.demo.searchcriteria.MessageSearchCriteria;

/**
 * Agent for accessing the "simple" (non-streaming) collaboration methods.
 */
public class SimpleCollaborationAgent extends AuthenticatingAgent {

    /**
     * Creates a new {@link SimpleCollaborationAgent}.
     *
     * @param baseUrl The base address for the Foundation API. The agent will
     * append method names automatically. For example, a base URL of
     * <code>http://api.company.com</code> will result in URLs constructed such
     * as <code>http://api.company.com/Authentication/v1/Tokens</code>.
     * @param username The username to give to the API when authenticating
     * @param password The username to give to the API when authenticating
     * @param agent The ID of the agent to use.
     */
    public SimpleCollaborationAgent(final String baseUrl, final String username,
            final String password, final String agent) {
        super(baseUrl, username, password, agent);
    }

    /**
     * Sends a message to the specified channel.
     *
     * @param channelId The ID of the channel to send a message to
     * @param message The content of the message to send
     * @throws IOException If the request can't be constructed or transmitted
     */
    public void sendMessage(final String channelId, final String message)
            throws IOException {
        sendMessage(channelId, null, message, false);
    }

    /**
     * Sends an alert message to the specified channel.
     *
     * @param channelId The ID of the channel to send a message to
     * @param message The content of the message to send
     * @throws IOException If the request can't be constructed or transmitted
     */
    public void sendAlert(final String channelId, final String message)
            throws IOException {
        sendMessage(channelId, null, message, true);
    }

    /**
     * Sends a story message to the specified channel.
     *
     * @param channelId The ID of the channel to send a message to
     * @param subject The subject of the message
     * @param message The content of the message to send
     * @throws IOException If the request can't be constructed or transmitted
     */
    public void sendStory(final String channelId, final String subject,
            final String body) throws IOException {
        sendMessage(channelId, subject, body, false);
    }

    /**
     * Sends a message to the specified channel.
     *
     * @param channelId The ID of the channel to send a message to
     * @param subject The subject of the story or <code>null</code> for a non-
     * story message
     * @param message The content of the message to send
     * @param alert Whether to send the message as an alert or not
     * @throws IOException If the request can't be constructed or transmitted
     */
    protected void sendMessage(final String channelId,
            final String subject, final String message,
            final boolean alert) throws IOException {
        try {
            final JSONObject payload = new JSONObject();
            payload.put("IsAlert", alert);
            payload.put("Subject", subject);
            payload.put("Text", message);

            getResponse("/Collaboration/v1/Channels/" + channelId + "/Messages",
                    "POST", payload.toString());
        } catch (JSONException ex) {
            throw new IOException("Unable to construct JSON payload", ex);
        }
    }

    /**
     * Retrieves the metadata associated with the agent.
     *
     * @return A map containing arbitrary metadata assigned to the agent.
     * @throws IOException If the request could not be completed or the response
     * not parsed
     */
    public Map<String, String> getMetadata() throws IOException {
        try {
            final JSONArray response = new JSONArray(
                    getResponse("/Collaboration/v1/MetaData", "GET", null));
            return getMap(response);
        } catch (JSONException ex) {
            throw new IOException("Unable to deserialise JSON response", ex);
        }
    }

    /**
     * Retrieves the set of all channels that the current agent is provisioned
     * for.
     *
     * @throws IOException If the request could not be completed or the response
     * not parsed
     * @returns A collection of {@link Channel} instances corresponding to the
     * agent's provisioned channels
     */
    public Collection<Channel> getChannels() throws IOException {
        final List<Channel> channels
                = new ArrayList<Channel>();

        try {
            final JSONArray response = new JSONArray(getResponse(
                    "/Collaboration/v1/Channels",
                    "GET", null));

            for (int i = 0; i < response.length(); i++) {
                channels.add(getChannelInformation(
                        (JSONObject) response.get(i)));
            }
        } catch (JSONException ex) {
            throw new IOException("Unable to deserialise JSON response", ex);
        }

        return channels;
    }

    /**
     * Searches a set of channels for messages matching the search criteria.
     *
     * @param criteria A {@link MessageSearchCriteria} implementation describing
     * the search criteria.
     * @throws IOException If the request could not be completed or the response
     * not parsed
     * @returns A collection of {@link SearchResultSet} instances corresponding
     * to the search results
     */
    public Collection<SearchResultSet> searchChannels(
            final MessageSearchCriteria criteria) throws IOException {
        final List<SearchResultSet> entries = new ArrayList<SearchResultSet>();

        try {
            final JSONObject object = new JSONObject();
            criteria.putData(object);
            final JSONArray response = new JSONArray(
                    getResponse("/Collaboration/v1/Channels/Search", "POST",
                    object.toString()));

            for (int i = 0; i < response.length(); i++) {
                entries.add(getSearchResultSet((JSONObject) response.get(i)));
            }
        } catch (JSONException ex) {
            throw new IOException("Unable to construct JSON payload or "
                    + "parse response", ex);
        }

        return entries;
    }

    /**
     * Retrieves information for the channel with the channel ID. Information
     * may only be retrieved about channels that the agent is provisioned for.
     *
     * @param channelId The ID of the channel to retrieve information for
     * @return A {@link Channel} object describing the channel
     * @see #getChannels()
     * @throws IOException If the request could not be completed or the response
     * not parsed
     */
    public Channel getChannelInformation(final String channelId)
            throws IOException {
        try {
            final JSONObject response = new JSONObject(getResponse(
                    "/Collaboration/v1/Channels/" + channelId,
                    "GET", null));

            return getChannelInformation(response);
        } catch (JSONException ex) {
            throw new IOException("Unable to deserialise JSON response", ex);
        }
    }

    /**
     * Retrieves the current state of the specified channel.
     *
     * @param channelId The ID of the channel to retrieve state for
     * @return A {@link ChannelState} object describing the channel
     * @throws IOException If the request could not be completed or the response
     * not parsed
     */
    public ChannelState getChannelState(final String channelId)
            throws IOException {
        try {
            final JSONObject response = new JSONObject(getResponse(
                    "/Collaboration/v1/Channels/" + channelId + "/State",
                    "GET", null));

            return getChannelState(response);
        } catch (JSONException ex) {
            throw new IOException("Unable to deserialise JSON response", ex);
        }
    }

    /**
     * Retrieves the most recent history of the specified channel.
     *
     * @param channelId The ID of the channel to retrieve history for
     * @param number The maximum number of messages to retrieve
     * @return A list of most recent {@link Message}s in the channel
     * @throws IOException If the request could not be completed or the response
     * not parsed
     */
    public List<Message> getChannelHistory(final String channelId,
            final int number) throws IOException {
        try {
            final JSONArray response = new JSONArray(getResponse(
                    "/Collaboration/v1/Channels/" + channelId
                    + "/Messages?take=" + number,
                    "GET", null));
            return getMessages(response);
        } catch (JSONException ex) {
            throw new IOException("Unable to deserialise JSON response", ex);
        }
    }

    /**
     * Constructs a collection of {@link Message}s from the given JSON array.
     *
     * @see #getMessage(org.json.JSONObject)
     * @param array The JSON array to be converted
     * @return A collection of corresponding Message instances
     * @throws JSONException If the object fails to contain required properties
     */
    protected List<Message> getMessages(final JSONArray array) throws JSONException {
        final List<Message> messages = new ArrayList<Message>();
        for (int i = 0; i < array.length(); i++) {
            messages.add(getMessage((JSONObject) array.get(i)));
        }
        return messages;
    }

    /**
     * Constructs a {@link SearchResultSet} object from the given JSON object.
     *
     * @param object The JSON object to be converted
     * @return A corresponding Message instance
     * @throws JSONException If the object fails to contain required properties
     */
    protected SearchResultSet getSearchResultSet(final JSONObject object) throws JSONException {
        return new SearchResultSet(
                object.getString("ChannelId"),
                object.getInt("Count"),
                object.getString("MaxMessageId"),
                getMessages(object.getJSONArray("Messages")),
                object.getString("MinMessageId")
                );
    }

    /**
     * Constructs a {@link Channel} object from the given JSON object.
     *
     * @param object The JSON object to be converted
     * @return A corresponding Channel instance
     * @throws JSONException If the object fails to contain required properties
     */
    protected Channel getChannelInformation(final JSONObject object) throws JSONException {
        return new Channel(
                object.optBoolean("CanAcceptFiles"),
                object.optString("Description"),
                object.getString("DisplayName"),
                object.optString("EmailAddress"),
                object.getString("Id"),
                object.optBoolean("IsReadOnly"),
                getMap(object.optJSONArray("MetaData")),
                object.optString("Subject"));
    }

    /**
     * Constructs a {@link Message} object from the given JSON object.
     *
     * @param object The JSON object to be converted
     * @return A corresponding Message instance
     * @throws JSONException If the object fails to contain required properties
     */
    protected Message getMessage(final JSONObject object) throws JSONException {
        return new Message(
                object.getString("Id"),
                object.optBoolean("IsAlert"),
                object.getString("SenderId"),
                object.optString("ChannelId"),
                object.optString("Subject"),
                object.getString("Text"),
                object.getLong("Timestamp"));
    }

    /**
     * Constructs a {@link ChannelState} object from the given JSON object.
     *
     * @param object The JSON object to be converted
     * @return A corresponding ChannelState instance
     * @throws JSONException If the object fails to contain required properties
     */
    protected ChannelState getChannelState(final JSONObject object) throws JSONException {
        return new ChannelState(
                object.optString("Subject"),
                PresenceState.forValue(object.getInt("Presence")),
                object.optString("PresenceText"));
    }

}
