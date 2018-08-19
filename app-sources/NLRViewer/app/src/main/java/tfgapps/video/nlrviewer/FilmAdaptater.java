package tfgapps.video.nlrviewer;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

class Film {
    public String imageURL;
    public String videoTITLE;
    public View.OnClickListener onclick;

    public Film(String imageURL, String videoTITLE) {
        this.imageURL = imageURL;
        this.videoTITLE = videoTITLE;
        this.onclick = new View.OnClickListener() {  @Override public void onClick(View view) { } };
    }
}

public class FilmAdaptater extends ArrayAdapter<Film> {

    //tweets est la liste des models à afficher
    public FilmAdaptater(Context context, List<Film> f) {
        super(context, 0, f);
    }

    private class FilmViewHolder{
        private ImageView image;
        private TextView videoTITLE;
        private ConstraintLayout layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_films,parent, false);
        }

        FilmViewHolder viewHolder = (FilmViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new FilmViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.rowAllyImage);
            viewHolder.videoTITLE = (TextView) convertView.findViewById(R.id.rowComicListTitle);
            viewHolder.layout = (ConstraintLayout) convertView.findViewById(R.id.relativeLayout);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        Film ep = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.videoTITLE.setText(ep.videoTITLE);
        Picasso.get().load(ep.imageURL).fit().into(viewHolder.image);

        //viewHolder.layout.setOnClickListener(ep.onclick);
        //viewHolder.image.setOnClickListener(ep.onclick);
        //viewHolder.videoTITLE.setOnClickListener(ep.onclick);
        //viewHolder.image.setImageDrawable(new ColorDrawable(tweet.getColor()));

        return convertView;
    }

}
