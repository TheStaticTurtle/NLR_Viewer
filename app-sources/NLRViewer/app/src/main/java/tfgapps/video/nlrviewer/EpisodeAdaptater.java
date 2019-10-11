package tfgapps.video.nlrviewer;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Samuel on 26/07/2018.
 */

class Episode {
    public String imageURL;
    public String videoTITLE;
    public String videoANDseason;
    public Integer bgcolor;
    public View.OnClickListener onclick;

    public Episode( String imageURL, String videoTITLE, String videoANDseason, Integer bgcolor) {
        this.imageURL = imageURL;
        this.videoTITLE = videoTITLE;
        this.videoANDseason = videoANDseason;
        this.bgcolor = bgcolor;
        this.onclick = new View.OnClickListener() {  @Override public void onClick(View view) { } };
    }
}

public class EpisodeAdaptater extends ArrayAdapter<Episode> {

        //tweets est la liste des models à afficher
        public EpisodeAdaptater(Context context, List<Episode> tweets) {
            super(context, 0, tweets);
        }

        private class EpisodeViewHolder{
            private ImageView image;
            private TextView videoTITLE;
            private TextView videoANDseason;
            private ConstraintLayout layout;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_episode,parent, false);
            }

            EpisodeViewHolder viewHolder = (EpisodeViewHolder) convertView.getTag();
            if(viewHolder == null){
                viewHolder = new EpisodeViewHolder();
                viewHolder.layout = (ConstraintLayout)  convertView.findViewById(R.id.relativeLayout);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.rowComicImage);
                viewHolder.videoTITLE = (TextView) convertView.findViewById(R.id.rowEpisodeTitle);
                viewHolder.videoANDseason = (TextView) convertView.findViewById(R.id.rowEpisodeSeason);
                convertView.setTag(viewHolder);
            }

            //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
            Episode ep = getItem(position);

            //il ne reste plus qu'à remplir notre vue
            viewHolder.videoTITLE.setText(ep.videoTITLE);
            viewHolder.videoANDseason.setText(ep.videoANDseason);
            Picasso.get().load(ep.imageURL).fit().into(viewHolder.image);
            viewHolder.layout.setBackgroundColor(ep.bgcolor);

            //viewHolder.layout.setOnClickListener(ep.onclick);
            //viewHolder.image.setOnClickListener(ep.onclick);
            //viewHolder.videoTITLE.setOnClickListener(ep.onclick);
            //viewHolder.videoANDseason.setOnClickListener(ep.onclick);

            return convertView;
        }

}
