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

class Comic {
    public String title;
    public String titleList;
    public String imgurl;
    public View.OnClickListener onClick;

    public Comic(String title, String parent, String imgurl ,View.OnClickListener click) {
        this.title = title;
        this.titleList = parent;
        this.imgurl = imgurl;
        this.onClick = click;
    }
}

public class ComicAdaptater extends ArrayAdapter<Comic> {

    //tweets est la liste des models à afficher
    public ComicAdaptater(Context context, List<Comic> f) {
        super(context, 0, f);
    }

    private class ComicViewHolder{
        private TextView title;
        private TextView titleList;
        private ImageView img;
        private ConstraintLayout layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_comic,parent, false);
        }

        ComicViewHolder viewHolder = (ComicViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new ComicViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.rowComicTitle);
            viewHolder.titleList = (TextView) convertView.findViewById(R.id.rowComicListTitle);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.rowComicImage);
            viewHolder.layout = (ConstraintLayout) convertView.findViewById(R.id.relativeLayout);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<News> tweets
        Comic ep = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.title.setText(ep.title);
        viewHolder.titleList.setText(ep.titleList);
        Picasso.get().load(ep.imgurl).fit().centerCrop().into(viewHolder.img);

        viewHolder.title.setOnClickListener(ep.onClick);
        viewHolder.titleList.setOnClickListener(ep.onClick);
        viewHolder.img.setOnClickListener(ep.onClick);
        viewHolder.layout.setOnClickListener(ep.onClick);

        return convertView;
    }

}
