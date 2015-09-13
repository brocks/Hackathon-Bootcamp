package com.hacks.ram.ramhacks;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by adel on 9/12/15.
 */
public class TweetAdapter extends ArrayAdapter<Tweet> {

    private Context context;
    private ArrayList<Tweet> tweets;

    public TweetAdapter(Context context, int viewResourceId, ArrayList<Tweet> tweets) {
        super(context, viewResourceId, tweets);
        this.context = context;
        this.tweets= tweets;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.tweet_adapter, parent, false);
        }

        Tweet tweet = tweets.get(position);

        if (tweet != null) {

            TextView name = (TextView) view.findViewById(R.id.name);
//            TextView usuario = (TextView) view.findViewById(R.id.usuario);
//            ImageView imagem = (ImageView) view.findViewById(R.id.imagem_perfil);
            TextView message = (TextView) view.findViewById(R.id.message);
            TextView data = (TextView) view.findViewById(R.id.data);

            name.setText("[ "+tweet.getNome()+" ]");
//            usuario.setText("@" + tweet.getUsuario());
//            BitmapManager.getInstance().loadBitmap(tweet.getUrlImagemPerfil(), imagem);

//            Get message links
            String messageText = tweet.getMensagem().replaceAll("(\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])","<a href=\"$1\">$1</a>");
            message.setText(Html.fromHtml(messageText));
            message.setMovementMethod(LinkMovementMethod.getInstance());

            data.setText(tweet.getData());
        }



        return view;
    }
}
