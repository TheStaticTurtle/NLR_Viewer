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

class Ally {
    public String imageURL;
    public String TITLE;
    public View.OnClickListener click;

    public Ally(String imageURL, String TITLE , View.OnClickListener click) {
        this.imageURL = imageURL;
        this.TITLE = TITLE;
        this.click = click;
    }
}

public class AllyAdaptater extends ArrayAdapter<Ally> {

    //tweets est la liste des models à afficher
    public AllyAdaptater(Context context, List<Ally> f) {
        super(context, 0, f);
    }

    private class AllyViewHolder{
        private ConstraintLayout layout;
        private ImageView image;
        private TextView TITLE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_ally,parent, false);
        }

        AllyViewHolder viewHolder = (AllyViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new AllyViewHolder();
            viewHolder.image = (ImageView) convertView.findViewById(R.id.rowAllyImage);
            viewHolder.TITLE = (TextView) convertView.findViewById(R.id.rowComicListTitle);
            viewHolder.layout = (ConstraintLayout) convertView.findViewById(R.id.relativeLayout22);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<Tweet> tweets
        Ally ep = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.TITLE.setText(ep.TITLE);
        Picasso.get().load(ep.imageURL).fit().centerCrop().into(viewHolder.image);
        viewHolder.TITLE.setOnClickListener(ep.click);
        viewHolder.image.setOnClickListener(ep.click);
        viewHolder.layout.setOnClickListener(ep.click);
        //viewHolder.image.getLayoutParams().height = img.fit().get().getHeight();
        //viewHolder.image.setImageDrawable(new ColorDrawable(tweet.getColor()));

        return convertView;
    }

}
