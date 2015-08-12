package de.nitri.aptimob;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.jasypt.util.text.BasicTextEncryptor;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MessagesFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private int userId;
    private String username;
    private SharedPreferences pref;
    private String restUrl;
    private Gson gson;
    private List<Message> messages;

    private Activity mActivity;
    private BasicTextEncryptor textEncryptor;

    private String password;

    public interface MessagesFragmentCallbacks {

        BasicTextEncryptor getTextEncryptor();

        SharedPreferences getPref();
    }

    // @Override
    // public void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // }
    //
    // @Override
    // public View onCreateView(LayoutInflater inflater, ViewGroup container,
    // Bundle savedInstanceState) {
    // View messagesView = inflater.inflate(R.layout.messages, container,
    // false);
    //
    // return messagesView;
    // }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState
                    .getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MessagesFragmentCallbacks callbacks = (MessagesFragmentCallbacks) mActivity;

        pref = callbacks.getPref();
        restUrl = pref.getString(AptiMob.PREF_REST_URL,
                getString(R.string.rest_url));

        gson = new Gson();

        textEncryptor = callbacks.getTextEncryptor();

        if (pref.getInt(AptiMob.PREF_USER_ID, 0) > 0) {
            userId = pref.getInt(AptiMob.PREF_USER_ID, 0);
            retrieveMessages();
        } else {
            username = pref.getString(AptiMob.PREF_USERNAME, "");
            password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
            String body = "";
            try {
                body = "username=" + URLEncoder.encode(username, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.i("SENDING", body);
            RestTask restTask = new RestTask(getActivity(), "aptimob.userId",
                    username, password);
            restTask.execute(restUrl + "userId", body, "POST");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
   /* public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(
                activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
                        : ListView.CHOICE_MODE_NONE);
    }*/

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void createList() {

        if (messages.size() > 0) {

            Comparator<Message> messageDateComparator = new Comparator<Message>() {
                @Override
                public int compare(Message o1, Message o2) {
                    return (int) o2.getTimestamp() - (int) o1.getTimestamp();
                }
            };

            Collections.sort(messages, messageDateComparator);

            List<String> senders = new ArrayList<>();
            List<String> descriptions = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            ArrayList<Integer> messageIds = new ArrayList<>();
            for (Message message : messages) {
                senders.add(message.getFrom());
                Date dateTime = new Date(message.getTimestamp() * 1000);
                dateTimes.add(dateFormat.format(dateTime) + " " + timeFormat.format(dateTime));
                descriptions.add(message.getSubject());
                messageIds.add(message.getId());
            }

            MessageListAdapter adapter = new MessageListAdapter(getActivity(),
                    senders.toArray(new String[senders.size()]),
                    descriptions.toArray(new String[descriptions.size()]),
                    dateTimes.toArray(new String[dateTimes.size()]));
            setListAdapter(adapter);
        }
    }

    private void retrieveMessages() {
        username = pref.getString(AptiMob.PREF_USERNAME, "");
        password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
        RestTask restTask = new RestTask(getActivity(), "aptimob.messagesIn",
                username, password);
        String body = "";
        try {
            body = "userId="
                    + URLEncoder.encode(Integer.toString(userId), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        restTask.execute(restUrl + "messagesIn", body, "POST");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity().getBaseContext(), MessageDetail.class);
        intent.putExtra("message", messages.get(position));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("aptimob.userId");
        filter.addAction("aptimob.messagesIn");
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("aptimob.userId")) {
                if (intent.getIntExtra(RestTask.HTTP_RESPONSE_CODE, 0) == 200) {
                    JsonParser parser = new JsonParser();
                    JsonArray jResult = parser.parse(
                            intent.getStringExtra(RestTask.HTTP_RESPONSE))
                            .getAsJsonArray();
                    JsonObject joId = jResult.get(0).getAsJsonObject();
                    JsonPrimitive jId = joId.getAsJsonPrimitive("id");
                    userId = jId.getAsInt();
                    pref.edit().putInt(AptiMob.PREF_USER_ID, 0).apply();
                    retrieveMessages();
                }
            }
            if (action.equals("aptimob.messagesIn")) {
                if (intent.getIntExtra(RestTask.HTTP_RESPONSE_CODE, 0) == 200) {
                    Type messageType = new TypeToken<List<Message>>() {
                    }.getType();
                    messages = gson.fromJson(
                            intent.getStringExtra(RestTask.HTTP_RESPONSE),
                            messageType);
                    createList();
                }
            }
        }

    };

    private class MessageListAdapter extends ArrayAdapter<String> {
        Context context;
        String[] senders;
        String[] descriptions;
        String[] dateTimes;

        MessageListAdapter(Context context, String[] senders,
                           String[] descriptions, String[] dateTimes) {
            super(context, R.layout.message_list_item, descriptions);

            this.context = context;
            this.senders = senders;
            this.descriptions = descriptions;
            this.dateTimes = dateTimes;
        }

        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row;
            if (convertView == null)
                row = inflater.inflate(R.layout.message_list_item, parent,
                        false);
            else
                row = convertView;
            TextView tvSender = (TextView) row.findViewById(R.id.from);
            TextView tvDescription = (TextView) row
                    .findViewById(R.id.text_line);
            TextView tvDateTime = (TextView) row.findViewById(R.id.date_time);

            tvSender.setText(senders[pos]);
            tvDescription.setText(descriptions[pos]);
            tvDateTime.setText(dateTimes[pos]);

            return (row);
        }
    }
}
